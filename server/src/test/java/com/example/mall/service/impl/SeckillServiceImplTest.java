package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.*;
import com.example.mall.mapper.*;
import com.example.mall.service.SeckillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SeckillServiceImpl 单元测试（重点：防超卖边界场景）
 *
 * 测试策略：Mock 所有 Mapper 和 RedisTemplate，只验证业务逻辑。
 *   - 正常抢购：库存充足 → 扣减成功 → 返回订单号
 *   - 库存为0：Redis返回<0 → 回补 → 抛异常
 *   - 数据库层防超卖：Redis扣成功但数据库affected=0 → 回补Redis → 抛异常
 *   - 限购：用户已购达限购数量 → 拒绝
 *   - 活动未开始/已结束：时间不在范围内 → 拒绝
 */
@ExtendWith(MockitoExtension.class)
class SeckillServiceImplTest {

    @InjectMocks
    private SeckillServiceImpl seckillService;

    @Mock private SeckillActivityMapper activityMapper;
    @Mock private SeckillItemMapper seckillItemMapper;
    @Mock private SkuMapper skuMapper;
    @Mock private ProductMapper productMapper;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private Long userId = 10001L;
    private Long seckillItemId = 1L;

    @BeforeEach
    void setUp() {
        // 默认 mock Redis ValueOperations
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(orderMapper.insert((Order) any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        });
        lenient().when(orderItemMapper.insert((OrderItem) any(OrderItem.class))).thenReturn(1);
    }

    /** 构建秒杀商品 */
    private SeckillItem buildSeckillItem(Long id, Long activityId, Long skuId,
                                          BigDecimal seckillPrice, BigDecimal normalPrice,
                                          int purchaseLimit, int seckillStock) {
        SeckillItem item = new SeckillItem();
        item.setId(id);
        item.setActivityId(activityId);
        item.setSkuId(skuId);
        item.setSeckillPrice(seckillPrice);
        item.setNormalPrice(normalPrice);
        item.setPurchaseLimit(purchaseLimit);
        item.setSeckillStock(seckillStock);
        return item;
    }

    /** 构建秒杀活动 */
    private SeckillActivity buildActivity(Long id, int status,
                                           LocalDateTime start, LocalDateTime end) {
        SeckillActivity activity = new SeckillActivity();
        activity.setId(id);
        activity.setStatus(status);
        activity.setStartTime(start);
        activity.setEndTime(end);
        return activity;
    }

    // ========== 正常抢购 ==========

    @Test
    void seckillBuy_success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = buildActivity(1L, 1,
                now.minusHours(1), now.plusHours(1));
        SeckillItem item = buildSeckillItem(1L, 1L, 10L,
                new BigDecimal("99"), new BigDecimal("199"), 2, 10);
        Sku sku = new Sku();
        sku.setId(10L);
        sku.setProductId(20L);
        sku.setSpecs("默认规格");
        Product product = new Product();
        product.setId(20L);
        product.setTitle("秒杀商品");

        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(seckillItemMapper.selectById(1L)).thenReturn(item);
        when(valueOperations.get("seckill:bought:1:10001")).thenReturn(null);
        when(valueOperations.decrement("seckill:stock:1")).thenReturn(9L);
        when(seckillItemMapper.decrStock(1L, 1)).thenReturn(1);
        when(skuMapper.selectById(10L)).thenReturn(sku);
        when(productMapper.selectById(20L)).thenReturn(product);

        // Act
        String orderNo = seckillService.seckillBuy(userId, seckillItemId);

        // Assert
        assertNotNull(orderNo);
        verify(orderMapper).insert((Order) any(Order.class));
        verify(valueOperations).decrement("seckill:stock:1");
        verify(valueOperations).increment("seckill:bought:1:10001");
    }

    // ========== 库存不足（Redis 层） ==========

    @Test
    void seckillBuy_stockExhausted_redis() {
        // Arrange：Redis 返回 -1（库存为0）
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = buildActivity(1L, 1,
                now.minusHours(1), now.plusHours(1));
        SeckillItem item = buildSeckillItem(1L, 1L, 10L,
                new BigDecimal("99"), new BigDecimal("199"), 2, 0);

        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(seckillItemMapper.selectById(1L)).thenReturn(item);
        when(valueOperations.get("seckill:bought:1:10001")).thenReturn(null);
        when(valueOperations.decrement("seckill:stock:1")).thenReturn(-1L);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> seckillService.seckillBuy(userId, seckillItemId));
        assertTrue(ex.getMessage().contains("抢完"));
        // 验证库存回补
        verify(valueOperations).increment("seckill:stock:1");
    }

    @Test
    void seckillBuy_stockExhausted_redisZero() {
        // Arrange：Redis 返回 0（恰好抢完）
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = buildActivity(1L, 1,
                now.minusHours(1), now.plusHours(1));
        SeckillItem item = buildSeckillItem(1L, 1L, 10L,
                new BigDecimal("99"), new BigDecimal("199"), 2, 0);

        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(seckillItemMapper.selectById(1L)).thenReturn(item);
        when(valueOperations.get("seckill:bought:1:10001")).thenReturn(null);
        when(valueOperations.decrement("seckill:stock:1")).thenReturn(0L);

        // Act & Assert：返回0时也视为库存不足
        assertThrows(BusinessException.class,
                () -> seckillService.seckillBuy(userId, seckillItemId));
    }

    // ========== 数据库层防超卖（Redis 扣成功但 DB 扣失败） ==========

    @Test
    void seckillBuy_dbStockMismatch_rollbackRedis() {
        // Arrange：Redis 扣成功（返回0），但数据库 affected=0
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = buildActivity(1L, 1,
                now.minusHours(1), now.plusHours(1));
        SeckillItem item = buildSeckillItem(1L, 1L, 10L,
                new BigDecimal("99"), new BigDecimal("199"), 2, 1);

        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(seckillItemMapper.selectById(1L)).thenReturn(item);
        when(valueOperations.get("seckill:bought:1:10001")).thenReturn(null);
        when(valueOperations.decrement("seckill:stock:1")).thenReturn(0L);
        when(seckillItemMapper.decrStock(1L, 1)).thenReturn(0); // DB扣失败

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> seckillService.seckillBuy(userId, seckillItemId));
        assertTrue(ex.getMessage().contains("抢完"));
        // 验证 Redis 回补
        verify(valueOperations).increment("seckill:stock:1");
        verify(valueOperations).decrement("seckill:bought:1:10001");
    }

    // ========== 限购检查 ==========

    @Test
    void seckillBuy_purchaseLimitExceeded() {
        // Arrange：已购2件，限购也是2件
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = buildActivity(1L, 1,
                now.minusHours(1), now.plusHours(1));
        SeckillItem item = buildSeckillItem(1L, 1L, 10L,
                new BigDecimal("99"), new BigDecimal("199"), 2, 10);

        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(seckillItemMapper.selectById(1L)).thenReturn(item);
        when(valueOperations.get("seckill:bought:1:10001")).thenReturn("2");

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> seckillService.seckillBuy(userId, seckillItemId));
        assertTrue(ex.getMessage().contains("限购"));
        // 不应调用 Redis 扣库存
        verify(valueOperations, never()).decrement(anyString());
    }

    @Test
    void seckillBuy_withinPurchaseLimit() {
        // Arrange：已购1件，限购2件，还能买1件
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = buildActivity(1L, 1,
                now.minusHours(1), now.plusHours(1));
        SeckillItem item = buildSeckillItem(1L, 1L, 10L,
                new BigDecimal("99"), new BigDecimal("199"), 2, 10);
        Sku sku = new Sku();
        sku.setId(10L);
        sku.setProductId(20L);
        sku.setSpecs("默认规格");
        Product product = new Product();
        product.setId(20L);
        product.setTitle("秒杀商品");

        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(seckillItemMapper.selectById(1L)).thenReturn(item);
        when(valueOperations.get("seckill:bought:1:10001")).thenReturn("1");
        when(valueOperations.decrement("seckill:stock:1")).thenReturn(8L);
        when(seckillItemMapper.decrStock(1L, 1)).thenReturn(1);
        when(skuMapper.selectById(10L)).thenReturn(sku);
        when(productMapper.selectById(20L)).thenReturn(product);

        // Act
        String orderNo = seckillService.seckillBuy(userId, seckillItemId);

        // Assert
        assertNotNull(orderNo);
    }

    // ========== 活动状态检查 ==========

    @Test
    void seckillBuy_activityNotStarted() {
        // Arrange：活动未开始
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = buildActivity(1L, 0, // status=0 未开始
                now.plusHours(1), now.plusHours(3));
        SeckillItem item = buildSeckillItem(1L, 1L, 10L,
                new BigDecimal("99"), new BigDecimal("199"), 2, 10);

        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(seckillItemMapper.selectById(1L)).thenReturn(item);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> seckillService.seckillBuy(userId, seckillItemId));
        assertTrue(ex.getMessage().contains("未开始"));
    }

    @Test
    void seckillBuy_activityEnded() {
        // Arrange：活动已结束（status=2）
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = buildActivity(1L, 2,
                now.minusHours(3), now.minusHours(1));
        SeckillItem item = buildSeckillItem(1L, 1L, 10L,
                new BigDecimal("99"), new BigDecimal("199"), 2, 10);

        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(seckillItemMapper.selectById(1L)).thenReturn(item);

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> seckillService.seckillBuy(userId, seckillItemId));
    }

    @Test
    void seckillBuy_activityOutsideTimeRange() {
        // Arrange：status=1但时间不在范围内
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = buildActivity(1L, 1,
                now.minusHours(3), now.minusHours(1)); // 已过期
        SeckillItem item = buildSeckillItem(1L, 1L, 10L,
                new BigDecimal("99"), new BigDecimal("199"), 2, 10);

        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(seckillItemMapper.selectById(1L)).thenReturn(item);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> seckillService.seckillBuy(userId, seckillItemId));
        assertTrue(ex.getMessage().contains("有效期"));
    }

    // ========== 秒杀商品不存在 ==========

    @Test
    void seckillBuy_itemNotFound() {
        // Arrange
        when(seckillItemMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> seckillService.seckillBuy(userId, 999L));
    }
}
