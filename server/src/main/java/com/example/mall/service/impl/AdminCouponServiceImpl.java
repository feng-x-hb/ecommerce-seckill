package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.CouponTemplate;
import com.example.mall.entity.UserCoupon;
import com.example.mall.mapper.CouponTemplateMapper;
import com.example.mall.mapper.UserCouponMapper;
import com.example.mall.service.AdminCouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminCouponServiceImpl implements AdminCouponService {
    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;

    public AdminCouponServiceImpl(CouponTemplateMapper couponTemplateMapper, UserCouponMapper userCouponMapper) {
        this.couponTemplateMapper = couponTemplateMapper;
        this.userCouponMapper = userCouponMapper;
    }

    @Override
    public Map<String, Object> listTemplates(int page, int size) {
        Page<CouponTemplate> result = couponTemplateMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<CouponTemplate>().orderByDesc(CouponTemplate::getCreatedAt)
        );
        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("list", result.getRecords());
        return map;
    }

    @Override
    public void saveTemplate(CouponTemplate template) {
        if (template.getId() != null) {
            couponTemplateMapper.updateById(template);
        } else {
            template.setRemain(template.getTotal());
            couponTemplateMapper.insert(template);
        }
    }

    @Override
    public void deleteTemplate(Long id) {
        couponTemplateMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void grantToUser(Long templateId, Long userId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException("优惠券模板不存在");
        }
        if (template.getRemain() <= 0) {
            throw new BusinessException("优惠券已发完");
        }
        Long alreadyClaimed = userCouponMapper.selectCount(
            new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getTemplateId, templateId)
        );
        if (alreadyClaimed > 0) {
            throw new BusinessException("该用户已拥有此优惠券");
        }
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setTemplateId(templateId);
        userCoupon.setStatus(0);
        userCouponMapper.insert(userCoupon);
        template.setRemain(template.getRemain() - 1);
        couponTemplateMapper.updateById(template);
    }
}
