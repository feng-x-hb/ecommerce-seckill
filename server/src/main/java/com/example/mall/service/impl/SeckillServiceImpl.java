package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.*;
import com.example.mall.mapper.*;
import com.example.mall.service.SeckillService;
import com.example.mall.vo.SeckillItemVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 秒杀服务实现类（SeckillServiceImpl）
 *
 * 核心知识点（面试高频）：
 *   1. Redis 缓存库存：秒杀库存放 Redis，避免高并发打爆数据库
 *   2. Redis 原子扣减：decr 返回值 < 0 说明抢完了，不走数据库
 *   3. 限购：用 Redis Set 记录用户已购商品，SISMEMBER 判断是否超限
 *   4. 最终一致性：Redis 扣成功后，异步/同步扣数据库库存
 *   5. 防超卖：数据库层 WHERE seckill_stock >= quantity 兜底
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private final SeckillActivityMapper activityMapper;
    private final SeckillItemMapper seckillItemMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String STOCK_KEY = "seckill:stock:";   // seckill:stock:{itemId} → 库存数
    private static final String BOUGHT_KEY = "seckill:bought:"; // seckill:bought:{itemId}:{userId} → 已购数量

    public SeckillServiceImpl(SeckillActivityMapper activityMapper,
                              SeckillItemMapper seckillItemMapper,
                              SkuMapper skuMapper,
                              ProductMapper productMapper,
                              OrderMapper orderMapper,
                              OrderItemMapper orderItemMapper,
                              StringRedisTemplate redisTemplate) {
        this.activityMapper = activityMapper;
        this.seckillItemMapper = seckillItemMapper;
        this.skuMapper = skuMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<SeckillItemVO> listSeckillItems(Long activityId) {
        // 查活动
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        // 查秒杀商品
        List<SeckillItem> items = seckillItemMapper.selectList(
                new LambdaQueryWrapper<SeckillItem>()
                        .eq(SeckillItem::getActivityId, activityId));

        if (items.isEmpty()) {
            return List.of();
        }

        // 批量查 SKU（避免 N+1）
        List<Sku> skuList = skuMapper.selectBatchIds(
                items.stream().map(SeckillItem::getSkuId).collect(Collectors.toSet()));
        var skuMap = skuList.stream().collect(Collectors.toMap(Sku::getId, s -> s));

        // 批量查商品（避免 N+1）
        List<Product> productList = productMapper.selectBatchIds(
                skuList.stream().map(Sku::getProductId).collect(Collectors.toSet()));
        var productMap = productList.stream().collect(Collectors.toMap(Product::getId, p -> p));

        // 初始化 Redis 缓存（如果不存在）
        for (SeckillItem item : items) {
            String key = STOCK_KEY + item.getId();
            if (Boolean.TRUE.equals(!redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().set(key, String.valueOf(item.getSeckillStock()));
            }
        }

        // 组装 VO
        return items.stream().map(item -> {
            SeckillItemVO vo = new SeckillItemVO();
            vo.setSeckillItemId(item.getId());
            vo.setActivityId(activityId);
            vo.setActivityTitle(activity.getTitle());
            vo.setSkuId(item.getSkuId());
            vo.setSeckillPrice(item.getSeckillPrice());
            vo.setNormalPrice(item.getNormalPrice());
            vo.setPurchaseLimit(item.getPurchaseLimit());
            vo.setStartTime(activity.getStartTime());
            vo.setEndTime(activity.getEndTime());
            vo.setActivityStatus(activity.getStatus());

            // 从 Redis 读实时库存
            String stockStr = redisTemplate.opsForValue().get(STOCK_KEY + item.getId());
            vo.setSeckillStock(stockStr != null ? Integer.parseInt(stockStr) : item.getSeckillStock());

            Sku sku = skuMap.get(item.getSkuId());
            if (sku != null) {
                vo.setSpecs(sku.getSpecs());
                Product product = productMap.get(sku.getProductId());
                if (product != null) {
                    vo.setProductId(product.getId());
                    vo.setProductName(product.getTitle());
                    vo.setProductImage(product.getMainImage());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String seckillBuy(Long userId, Long seckillItemId) {
        // 1. 查秒杀商品
        SeckillItem item = seckillItemMapper.selectById(seckillItemId);
        if (item == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        // 2. 检查活动状态
        SeckillActivity activity = activityMapper.selectById(item.getActivityId());
        if (activity == null || activity.getStatus() != 1) {
            throw new BusinessException("活动未开始或已结束");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException("活动未在有效期内");
        }

        // 3. 限购检查（Redis Set）
        String boughtKey = BOUGHT_KEY + seckillItemId + ":" + userId;
        String boughtStr = redisTemplate.opsForValue().get(boughtKey);
        int boughtCount = boughtStr != null ? Integer.parseInt(boughtStr) : 0;
        if (boughtCount >= item.getPurchaseLimit()) {
            throw new BusinessException("已达限购数量，每人限购 " + item.getPurchaseLimit() + " 件");
        }

        // 4. Redis 扣库存（原子操作）
        String stockKey = STOCK_KEY + seckillItemId;
        Long stock = redisTemplate.opsForValue().decrement(stockKey);
        if (stock == null || stock < 0) {
            // 库存不足，回补
            redisTemplate.opsForValue().increment(stockKey);
            throw new BusinessException("秒杀已抢完");
        }

        // 5. 记录已购数量
        redisTemplate.opsForValue().increment(boughtKey);
        redisTemplate.expire(boughtKey, 24, TimeUnit.HOURS);

        // 6. 数据库扣库存（兜底防超卖）
        int affected = seckillItemMapper.decrStock(seckillItemId, 1);
        if (affected == 0) {
            // 数据库扣失败，回补 Redis
            redisTemplate.opsForValue().increment(stockKey);
            redisTemplate.opsForValue().decrement(boughtKey);
            throw new BusinessException("秒杀已抢完");
        }

        // 7. 创建订单（秒杀订单）
        Sku sku = skuMapper.selectById(item.getSkuId());
        Product product = productMapper.selectById(sku.getProductId());

        String orderNo = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", new java.util.Random().nextInt(999999));

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(0);
        order.setTotalAmount(item.getSeckillPrice());
        order.setDiscountAmount(java.math.BigDecimal.ZERO);
        order.setPayAmount(item.getSeckillPrice());
        order.setReceiverName("待填写");
        order.setReceiverPhone("待填写");
        order.setReceiverAddress("待填写");

        // 注入 OrderMapper（通过构造器）
        orderMapper.insert(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setSkuId(item.getSkuId());
        orderItem.setProductName(product != null ? product.getTitle() : "");
        orderItem.setProductImage(product != null ? product.getMainImage() : null);
        orderItem.setSpecDesc(sku.getSpecs());
        orderItem.setPrice(item.getSeckillPrice());
        orderItem.setQuantity(1);
        orderItem.setSubTotal(item.getSeckillPrice());
        orderItemMapper.insert(orderItem);

        return orderNo;
    }
}
