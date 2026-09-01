package com.example.mall.service;

import com.example.mall.entity.CouponTemplate;
import java.util.Map;

public interface AdminCouponService {
    Map<String, Object> listTemplates(int page, int size);
    void saveTemplate(CouponTemplate template);
    void deleteTemplate(Long id);
    void grantToUser(Long templateId, Long userId);
}
