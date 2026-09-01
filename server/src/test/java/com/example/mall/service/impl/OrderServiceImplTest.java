package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.*;
import com.example.mall.mapper.*;
import com.example.mall.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderServiceImpl 单元测试（重点：优惠券抵扣边界场景）
 *
 * 测试策略：Mock 所有 Mapper，只验证 createOrder 的业务逻辑。
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private SkuMapper skuMapper;
    @Mock private ProductMapper productMapper;
    @Mock private UserCouponMapper userCouponMapper;
    @Mock private CouponTemplateMapper couponTemplateMapper;

    private Long userId = 10001L;

    // ========== 辅助方法 ==========

    /** 构建一个正常的 SKU */
    private Sku buildSku(Long id, Long productId, BigDecimal price) {
        Sku sku = new Sku();
        sku.setId(id);
        sku.setProductId(productId);
        sku.setPrice(price);
        sku.setSpecs("默认规格");
        sku.setStatus(1);
        return sku;
    }

    /** 构建一个正常的商品 */
    private Product buildProduct(Long id, String title) {
        Product p = new Product();
        p.setId(id);
        p.setTitle(title);
        p.setMainImage("http://example.com/img.jpg");
        return p;
    }

    /** 构建一个优惠券模板 */
    private CouponTemplate buildTemplate(Long id, BigDecimal minAmount, BigDecimal discount,
                                          int status, LocalDate start, LocalDate end) {
        CouponTemplate t = new CouponTemplate();
        t.setId(id);
        t.setMinAmount(minAmount);
        t.setDiscount(discount);
        t.setStatus(status);
        t.setStartDate(start);
        t.setEndDate(end);
        return t;
    }

    /** 构建一个用户优惠券 */
    private UserCoupon buildUserCoupon(Long id, Long userId, Long templateId, int status) {
        UserCoupon uc = new UserCoupon();
        uc.setId(id);
        uc.setUserId(userId);
        uc.setTemplateId(templateId);
        uc.setStatus(status);
        return uc;
    }

    /** 构建结算项 */
    private Map<String, Object> buildItem(Long skuId, int quantity) {
        Map<String, Object> item = new HashMap<>();
        item.put("skuId", skuId);
        item.put("quantity", quantity);
        return item;
    }

    @BeforeEach
    void setUp() {
        // 默认 mock：库存扣减成功
        lenient().when(skuMapper.decrStock(anyLong(), anyInt())).thenReturn(1);
        lenient().when(skuMapper.incrStock(anyLong(), anyInt())).thenReturn(1);
        lenient().when(orderMapper.insert((Order) any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        });
        lenient().when(orderItemMapper.insert((OrderItem) any(OrderItem.class))).thenReturn(1);
    }

    // ========== 优惠券抵扣正常场景 ==========

    @Test
    void createOrder_withCoupon_discountApplied() {
        // Arrange：满1000减100，订单刚好1000元
        Sku sku = buildSku(1L, 10L, new BigDecimal("1000"));
        Product product = buildProduct(10L, "测试商品");
        CouponTemplate template = buildTemplate(1L,
                new BigDecimal("1000"), new BigDecimal("100"), 1,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        UserCoupon userCoupon = buildUserCoupon(100L, userId, 1L, 0);

        when(skuMapper.selectById(1L)).thenReturn(sku);
        when(productMapper.selectById(10L)).thenReturn(product);
        when(userCouponMapper.selectById(100L)).thenReturn(userCoupon);
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        // Act
        Map<Long, Long> itemCoupons = Map.of(1L, 100L);
        String orderNo = orderService.createOrder(userId,
                List.of(buildItem(1L, 1)),
                "张三", "13800001111", "北京", itemCoupons);

        // Assert
        assertNotNull(orderNo);
        verify(orderMapper).insert(argThat((Order order) ->
                order.getDiscountAmount().compareTo(new BigDecimal("100")) == 0
                        && order.getPayAmount().compareTo(new BigDecimal("900")) == 0
        ));
        assertEquals(1, userCoupon.getStatus(), "优惠券应标记为已使用");
    }

    @Test
    void createOrder_couponDiscountCappedAtSubTotal() {
        // Arrange：满100减200，但商品总价只有100，最多减100
        Sku sku = buildSku(1L, 10L, new BigDecimal("100"));
        Product product = buildProduct(10L, "测试商品");
        CouponTemplate template = buildTemplate(1L,
                new BigDecimal("100"), new BigDecimal("200"), 1,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        UserCoupon userCoupon = buildUserCoupon(100L, userId, 1L, 0);

        when(skuMapper.selectById(1L)).thenReturn(sku);
        when(productMapper.selectById(10L)).thenReturn(product);
        when(userCouponMapper.selectById(100L)).thenReturn(userCoupon);
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        // Act
        String orderNo = orderService.createOrder(userId,
                List.of(buildItem(1L, 1)),
                "张三", "13800001111", "北京",
                Map.of(1L, 100L));

        // Assert：折扣不超过商品小计
        verify(orderMapper).insert(argThat((Order order) ->
                order.getDiscountAmount().compareTo(new BigDecimal("100")) == 0
        ));
    }

    // ========== 优惠券抵扣异常场景 ==========

    @Test
    void createOrder_couponAmountNotMet() {
        // Arrange：满1000减100，但订单只有500元
        Sku sku = buildSku(1L, 10L, new BigDecimal("500"));
        Product product = buildProduct(10L, "测试商品");
        CouponTemplate template = buildTemplate(1L,
                new BigDecimal("1000"), new BigDecimal("100"), 1,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        UserCoupon userCoupon = buildUserCoupon(100L, userId, 1L, 0);

        when(skuMapper.selectById(1L)).thenReturn(sku);
        when(productMapper.selectById(10L)).thenReturn(product);
        when(userCouponMapper.selectById(100L)).thenReturn(userCoupon);
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(userId,
                        List.of(buildItem(1L, 1)),
                        "张三", "13800001111", "北京",
                        Map.of(1L, 100L)));
        assertTrue(ex.getMessage().contains("最低消费"));
    }

    @Test
    void createOrder_couponExpired() {
        // Arrange：优惠券已过期
        Sku sku = buildSku(1L, 10L, new BigDecimal("1000"));
        Product product = buildProduct(10L, "测试商品");
        CouponTemplate template = buildTemplate(1L,
                new BigDecimal("1000"), new BigDecimal("100"), 1,
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(5)); // 已过期
        UserCoupon userCoupon = buildUserCoupon(100L, userId, 1L, 0);

        when(skuMapper.selectById(1L)).thenReturn(sku);
        when(productMapper.selectById(10L)).thenReturn(product);
        when(userCouponMapper.selectById(100L)).thenReturn(userCoupon);
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(userId,
                        List.of(buildItem(1L, 1)),
                        "张三", "13800001111", "北京",
                        Map.of(1L, 100L)));
        assertTrue(ex.getMessage().contains("有效期"));
    }

    @Test
    void createOrder_couponAlreadyUsed() {
        // Arrange：优惠券已被使用
        Sku sku = buildSku(1L, 10L, new BigDecimal("1000"));
        Product product = buildProduct(10L, "测试商品");
        CouponTemplate template = buildTemplate(1L,
                new BigDecimal("1000"), new BigDecimal("100"), 1,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        UserCoupon userCoupon = buildUserCoupon(100L, userId, 1L, 1); // status=1 已使用

        when(skuMapper.selectById(1L)).thenReturn(sku);
        when(productMapper.selectById(10L)).thenReturn(product);
        when(userCouponMapper.selectById(100L)).thenReturn(userCoupon);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(userId,
                        List.of(buildItem(1L, 1)),
                        "张三", "13800001111", "北京",
                        Map.of(1L, 100L)));
        assertTrue(ex.getMessage().contains("已使用"));
    }

    @Test
    void createOrder_couponNotOwned() {
        // Arrange：优惠券不属于当前用户
        Sku sku = buildSku(1L, 10L, new BigDecimal("1000"));
        Product product = buildProduct(10L, "测试商品");
        UserCoupon userCoupon = buildUserCoupon(100L, 99999L, 1L, 0); // 不同的 userId

        when(skuMapper.selectById(1L)).thenReturn(sku);
        when(productMapper.selectById(10L)).thenReturn(product);
        when(userCouponMapper.selectById(100L)).thenReturn(userCoupon);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(userId,
                        List.of(buildItem(1L, 1)),
                        "张三", "13800001111", "北京",
                        Map.of(1L, 100L)));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    void createOrder_couponTemplateInactive() {
        // Arrange：优惠券模板已下架
        Sku sku = buildSku(1L, 10L, new BigDecimal("1000"));
        Product product = buildProduct(10L, "测试商品");
        CouponTemplate template = buildTemplate(1L,
                new BigDecimal("1000"), new BigDecimal("100"), 0, // status=0 已下架
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        UserCoupon userCoupon = buildUserCoupon(100L, userId, 1L, 0);

        when(skuMapper.selectById(1L)).thenReturn(sku);
        when(productMapper.selectById(10L)).thenReturn(product);
        when(userCouponMapper.selectById(100L)).thenReturn(userCoupon);
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(userId,
                        List.of(buildItem(1L, 1)),
                        "张三", "13800001111", "北京",
                        Map.of(1L, 100L)));
        assertTrue(ex.getMessage().contains("失效"));
    }

    // ========== 库存不足 ==========

    @Test
    void createOrder_insufficientStock() {
        // Arrange：库存不足
        Sku sku = buildSku(1L, 10L, new BigDecimal("1000"));
        when(skuMapper.selectById(1L)).thenReturn(sku);
        when(skuMapper.decrStock(1L, 1)).thenReturn(0); // 扣减失败

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(userId,
                        List.of(buildItem(1L, 1)),
                        "张三", "13800001111", "北京", null));
        assertTrue(ex.getMessage().contains("库存不足"));
    }

    // ========== 空购物车 ==========

    @Test
    void createOrder_emptyCart() {
        // Act & Assert
        assertThrows(BusinessException.class,
                () -> orderService.createOrder(userId,
                        List.of(),
                        "张三", "13800001111", "北京", null));
    }

    // ========== 不使用优惠券 ==========

    @Test
    void createOrder_noCoupon_fullAmount() {
        // Arrange：不使用优惠券，全额支付
        Sku sku = buildSku(1L, 10L, new BigDecimal("500"));
        Product product = buildProduct(10L, "测试商品");

        when(skuMapper.selectById(1L)).thenReturn(sku);
        when(productMapper.selectById(10L)).thenReturn(product);

        // Act
        String orderNo = orderService.createOrder(userId,
                List.of(buildItem(1L, 2)),
                "张三", "13800001111", "北京", null);

        // Assert：无折扣，全额=500*2=1000
        assertNotNull(orderNo);
        verify(orderMapper).insert(argThat((Order order) ->
                order.getDiscountAmount().compareTo(BigDecimal.ZERO) == 0
                        && order.getPayAmount().compareTo(new BigDecimal("1000")) == 0
        ));
    }

    // ========== 多商品 + 不同优惠券 ==========

    @Test
    void createOrder_multipleItems_differentCoupons() {
        // Arrange：2个商品各用不同的券
        Sku sku1 = buildSku(1L, 10L, new BigDecimal("600"));
        Sku sku2 = buildSku(2L, 20L, new BigDecimal("800"));
        Product product1 = buildProduct(10L, "商品A");
        Product product2 = buildProduct(20L, "商品B");

        CouponTemplate t1 = buildTemplate(1L,
                new BigDecimal("500"), new BigDecimal("50"), 1,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        CouponTemplate t2 = buildTemplate(2L,
                new BigDecimal("500"), new BigDecimal("80"), 1,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        UserCoupon uc1 = buildUserCoupon(100L, userId, 1L, 0);
        UserCoupon uc2 = buildUserCoupon(101L, userId, 2L, 0);

        when(skuMapper.selectById(1L)).thenReturn(sku1);
        when(skuMapper.selectById(2L)).thenReturn(sku2);
        when(productMapper.selectById(10L)).thenReturn(product1);
        when(productMapper.selectById(20L)).thenReturn(product2);
        when(userCouponMapper.selectById(100L)).thenReturn(uc1);
        when(userCouponMapper.selectById(101L)).thenReturn(uc2);
        when(couponTemplateMapper.selectById(1L)).thenReturn(t1);
        when(couponTemplateMapper.selectById(2L)).thenReturn(t2);

        // Act
        Map<Long, Long> itemCoupons = Map.of(1L, 100L, 2L, 101L);
        String orderNo = orderService.createOrder(userId,
                List.of(buildItem(1L, 1), buildItem(2L, 1)),
                "张三", "13800001111", "北京", itemCoupons);

        // Assert：总折扣 = 50 + 80 = 130
        assertNotNull(orderNo);
        verify(orderMapper).insert(argThat((Order order) -> {
            BigDecimal expectedDiscount = new BigDecimal("130");
            return order.getDiscountAmount().compareTo(expectedDiscount) == 0;
        }));
    }

    // ========== 支付和取消测试 ==========

    @Test
    void pay_success() {
        // Arrange
        Order order = new Order();
        order.setOrderNo("20260101120000123456");
        order.setUserId(userId);
        order.setStatus(0);
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        // Act
        orderService.pay(userId, "20260101120000123456");

        // Assert
        assertEquals(1, order.getStatus());
        assertNotNull(order.getPayTime());
        verify(orderMapper).updateById(order);
    }

    @Test
    void pay_alreadyPaid() {
        // Arrange：已支付的订单
        Order order = new Order();
        order.setOrderNo("20260101120000123456");
        order.setUserId(userId);
        order.setStatus(1);
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> orderService.pay(userId, "20260101120000123456"));
    }

    @Test
    void cancel_success() {
        // Arrange：待支付订单
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("20260101120000123456");
        order.setUserId(userId);
        order.setStatus(0);
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        OrderItem item = new OrderItem();
        item.setOrderId(1L);
        item.setSkuId(1L);
        item.setQuantity(2);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(item));

        // Act
        orderService.cancel(userId, "20260101120000123456");

        // Assert：订单状态变为已取消
        assertEquals(4, order.getStatus());
        verify(skuMapper).incrStock(1L, 2); // 库存恢复
    }

    @Test
    void cancel_notPendingPayment() {
        // Arrange：已支付的订单不能取消
        Order order = new Order();
        order.setOrderNo("20260101120000123456");
        order.setUserId(userId);
        order.setStatus(1);
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> orderService.cancel(userId, "20260101120000123456"));
    }
}
