package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "优惠券(用户)", description = "用户领取、查看优惠券")
@RestController
@RequestMapping("/api/coupon")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @Operation(summary = "可领取优惠券列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return Result.success(couponService.availableCoupons(page, size));
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/claim")
    public Result<Void> claim(@RequestAttribute("userId") Long userId, @RequestParam Long templateId) {
        couponService.claim(userId, templateId);
        return Result.success(null);
    }

    @Operation(summary = "我的优惠券列表")
    @GetMapping("/my")
    public Result<Map<String, Object>> myCoupons(@RequestAttribute("userId") Long userId,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return Result.success(couponService.myCoupons(userId, page, size));
    }
}
