package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.*;
import com.example.mall.mapper.*;
import com.example.mall.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CouponServiceImpl 单元测试
 *
 * 测试策略：Mock CouponTemplateMapper 和 UserCouponMapper，只验证业务逻辑。
 */
@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @InjectMocks
    private CouponServiceImpl couponService;

    @Mock
    private CouponTemplateMapper couponTemplateMapper;

    @Mock
    private UserCouponMapper userCouponMapper;

    // ========== 领取优惠券测试 ==========

    @Test
    void claim_success() {
        // Arrange
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setName("满1000减100");
        template.setStatus(1);
        template.setRemain(100);
        template.setStartDate(LocalDate.now().minusDays(10));
        template.setEndDate(LocalDate.now().plusDays(10));
        template.setMinAmount(new BigDecimal("1000"));
        template.setDiscount(new BigDecimal("100"));
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);
        when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Act
        couponService.claim(10001L, 1L);

        // Assert
        verify(userCouponMapper).insert(any(UserCoupon.class));
        verify(couponTemplateMapper).updateById(template);
        assertEquals(99, template.getRemain(), "库存应减1");
    }

    @Test
    void claim_templateNotFound() {
        // Arrange：模板不存在
        when(couponTemplateMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.claim(10001L, 999L));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    void claim_templateDisabled() {
        // Arrange：模板已下架
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setStatus(0);
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> couponService.claim(10001L, 1L));
    }

    @Test
    void claim_stockExhausted() {
        // Arrange：库存为0
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setRemain(0);
        template.setStartDate(LocalDate.now().minusDays(1));
        template.setEndDate(LocalDate.now().plusDays(1));
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.claim(10001L, 1L));
        assertTrue(ex.getMessage().contains("领完"));
    }

    @Test
    void claim_outsideDateRange() {
        // Arrange：不在领取时间范围内（已过期）
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setRemain(50);
        template.setStartDate(LocalDate.now().minusDays(20));
        template.setEndDate(LocalDate.now().minusDays(5)); // 已过期
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.claim(10001L, 1L));
        assertTrue(ex.getMessage().contains("时间"));
    }

    @Test
    void claim_alreadyClaimed() {
        // Arrange：已领取过该优惠券
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setRemain(50);
        template.setStartDate(LocalDate.now().minusDays(1));
        template.setEndDate(LocalDate.now().plusDays(1));
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);
        when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> couponService.claim(10001L, 1L));
        assertTrue(ex.getMessage().contains("已领取"));
    }

    @Test
    void claim_stockDecrementAtomicity() {
        // Arrange：验证库存扣减的原子性
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setRemain(1); // 最后1张
        template.setStartDate(LocalDate.now().minusDays(1));
        template.setEndDate(LocalDate.now().plusDays(1));
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);
        when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Act
        couponService.claim(10001L, 1L);

        // Assert：库存应变为0
        assertEquals(0, template.getRemain());
        verify(couponTemplateMapper).updateById(template);
    }
}
