package com.example.mall.service;

import java.util.Map;

public interface CouponService {
    Map<String, Object> availableCoupons(int page, int size);
    void claim(Long userId, Long templateId);
    Map<String, Object> myCoupons(Long userId, int page, int size);
}
