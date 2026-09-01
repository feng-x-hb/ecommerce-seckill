package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.CouponTemplate;
import com.example.mall.entity.UserCoupon;
import com.example.mall.mapper.CouponTemplateMapper;
import com.example.mall.mapper.UserCouponMapper;
import com.example.mall.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CouponServiceImpl implements CouponService {
    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;

    public CouponServiceImpl(CouponTemplateMapper couponTemplateMapper, UserCouponMapper userCouponMapper) {
        this.couponTemplateMapper = couponTemplateMapper;
        this.userCouponMapper = userCouponMapper;
    }

    @Override
    public Map<String, Object> availableCoupons(int page, int size) {
        Page<CouponTemplate> result = couponTemplateMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<CouponTemplate>()
                .eq(CouponTemplate::getStatus, 1)
                .gt(CouponTemplate::getRemain, 0)
                .le(CouponTemplate::getStartDate, LocalDate.now())
                .ge(CouponTemplate::getEndDate, LocalDate.now())
                .orderByDesc(CouponTemplate::getDiscount)
        );
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("list", result.getRecords());
        return map;
    }

    @Override
    @Transactional
    public void claim(Long userId, Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null || template.getStatus() != 1) {
            throw new BusinessException("优惠券不存在或已下架");
        }
        if (template.getRemain() <= 0) {
            throw new BusinessException("优惠券已领完");
        }
        if (LocalDate.now().isBefore(template.getStartDate()) || LocalDate.now().isAfter(template.getEndDate())) {
            throw new BusinessException("不在领取时间内");
        }
        Long alreadyClaimed = userCouponMapper.selectCount(
            new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getTemplateId, templateId)
        );
        if (alreadyClaimed > 0) {
            throw new BusinessException("您已领取过该优惠券");
        }
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setTemplateId(templateId);
        userCoupon.setStatus(0);
        userCouponMapper.insert(userCoupon);
        template.setRemain(template.getRemain() - 1);
        couponTemplateMapper.updateById(template);
    }

    @Override
    public Map<String, Object> myCoupons(Long userId, int page, int size) {
        Page<UserCoupon> couponPage = userCouponMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getCreatedAt)
        );
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserCoupon uc : couponPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", uc.getId());
            item.put("templateId", uc.getTemplateId());
            item.put("status", uc.getStatus());
            item.put("usedAt", uc.getUsedAt());
            item.put("createdAt", uc.getCreatedAt());
            CouponTemplate template = couponTemplateMapper.selectById(uc.getTemplateId());
            if (template != null) {
                item.put("name", template.getName());
                item.put("discount", template.getDiscount());
                item.put("minAmount", template.getMinAmount());
                item.put("endDate", template.getEndDate());
            }
            list.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", couponPage.getTotal());
        result.put("list", list);
        return result;
    }
}
