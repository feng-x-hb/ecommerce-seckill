package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.*;
import com.example.mall.mapper.*;
import com.example.mall.service.OrderService;
import com.example.mall.vo.OrderDetailVO;
import com.example.mall.vo.OrderItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类（OrderServiceImpl）
 *
 * 核心知识点（面试高频）：
 *   1. 防超卖：UPDATE sku SET stock=stock-? WHERE id=? AND stock>=?
 *      如果库存不足，affected rows = 0，此时抛异常回滚整个事务。
 *   2. 事务一致性：@Transactional 保证"扣库存 + 建订单 + 建明细"要么全成功，要么全回滚。
 *   3. 快照机制：下单时把商品名/图/规格/单价复制到 order_item，
 *      商品表后续改动不影响历史订单。
 *   4. 订单号生成：时间戳 + 6位随机数，不暴露业务量、不可猜测。
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponTemplateMapper couponTemplateMapper;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            SkuMapper skuMapper, ProductMapper productMapper,
                            UserCouponMapper userCouponMapper, CouponTemplateMapper couponTemplateMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.skuMapper = skuMapper;
        this.productMapper = productMapper;
        this.userCouponMapper = userCouponMapper;
        this.couponTemplateMapper = couponTemplateMapper;
    }

    @Override
    @Transactional
    public String createOrder(Long userId, List<Map<String, Object>> skuItems,
                              String receiverName, String receiverPhone, String receiverAddress,
                              Long couponId) {
        if (skuItems == null || skuItems.isEmpty()) {
            throw new BusinessException("请选择要结算的商品");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String, Object> item : skuItems) {
            Long skuId = Long.valueOf(item.get("skuId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());

            // 1. 查 SKU 信息
            Sku sku = skuMapper.selectById(skuId);
            if (sku == null || sku.getStatus() != 1) {
                throw new BusinessException("商品规格不存在或已停用，SKU:" + skuId);
            }

            // 2. 防超卖：扣库存（affected rows = 0 说明库存不足）
            int affected = skuMapper.decrStock(skuId, quantity);
            if (affected == 0) {
                throw new BusinessException("库存不足：" + sku.getSpecs());
            }

            // 3. 查商品信息（用于快照）
            Product product = productMapper.selectById(sku.getProductId());

            // 4. 计算小计
            BigDecimal subTotal = sku.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(subTotal);

            // 5. 组装订单明细（快照）
            OrderItem orderItem = new OrderItem();
            orderItem.setSkuId(skuId);
            orderItem.setProductName(product != null ? product.getTitle() : "");
            orderItem.setProductImage(product != null ? product.getMainImage() : null);
            orderItem.setSpecDesc(sku.getSpecs());
            orderItem.setPrice(sku.getPrice());
            orderItem.setQuantity(quantity);
            orderItem.setSubTotal(subTotal);
            orderItems.add(orderItem);
        }

        // 6. 生成订单号：时间戳 + 6位随机数
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", new Random().nextInt(999999));

        // 7. 处理优惠券抵扣
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponId != null) {
            UserCoupon userCoupon = userCouponMapper.selectById(couponId);
            if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
                throw new BusinessException("优惠券不存在");
            }
            if (userCoupon.getStatus() != 0) {
                throw new BusinessException("该优惠券已使用或已过期");
            }
            CouponTemplate template = couponTemplateMapper.selectById(userCoupon.getTemplateId());
            if (template == null || template.getStatus() != 1) {
                throw new BusinessException("优惠券模板已失效");
            }
            // 检查有效期
            java.time.LocalDate today = java.time.LocalDate.now();
            if (today.isBefore(template.getStartDate()) || today.isAfter(template.getEndDate())) {
                throw new BusinessException("优惠券不在有效期内");
            }
            // 检查最低消费
            if (totalAmount.compareTo(template.getMinAmount()) < 0) {
                throw new BusinessException("未达到优惠券最低消费 ¥" + template.getMinAmount());
            }
            // 计算折扣（不超过订单总额）
            discountAmount = template.getDiscount().min(totalAmount);
            // 标记优惠券已使用
            userCoupon.setStatus(1);
            userCoupon.setUsedAt(LocalDateTime.now());
            userCouponMapper.updateById(userCoupon);
        }

        // 8. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(0); // 待支付
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(totalAmount.subtract(discountAmount));
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        orderMapper.insert(order);

        // 8. 创建订单明细
        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        return orderNo;
    }

    @Override
    @Transactional
    public void pay(Long userId, String orderNo) {
        Order order = getOrderOrThrow(userId, orderNo);
        if (order.getStatus() != 0) {
            throw new BusinessException("当前订单状态不可支付");
        }
        order.setStatus(1); // 已支付
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancel(Long userId, String orderNo) {
        Order order = getOrderOrThrow(userId, orderNo);
        if (order.getStatus() != 0) {
            throw new BusinessException("只有待支付订单可以取消");
        }

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            skuMapper.incrStock(item.getSkuId(), item.getQuantity());
        }

        order.setStatus(4); // 已取消
        orderMapper.updateById(order);
    }

    @Override
    public Map<String, Object> list(Long userId, int page, int size, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        Page<Order> pageResult = orderMapper.selectPage(new Page<>(page, size), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("list", pageResult.getRecords());
        return result;
    }

    @Override
    public OrderDetailVO detail(Long userId, String orderNo) {
        Order order = getOrderOrThrow(userId, orderNo);

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));

        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO vo = new OrderItemVO();
            vo.setSkuId(item.getSkuId());
            vo.setProductName(item.getProductName());
            vo.setProductImage(item.getProductImage());
            vo.setSpecDesc(item.getSpecDesc());
            vo.setPrice(item.getPrice());
            vo.setQuantity(item.getQuantity());
            vo.setSubTotal(item.getSubTotal());
            return vo;
        }).collect(Collectors.toList());

        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setPayTime(order.getPayTime());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setItems(itemVOs);
        return vo;
    }

    /** 查订单，不存在或不属于当前用户则报错 */
    private Order getOrderOrThrow(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }
}
