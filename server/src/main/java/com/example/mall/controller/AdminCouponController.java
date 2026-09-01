package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.entity.CouponTemplate;
import com.example.mall.service.AdminCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理-优惠券", description = "管理员优惠券模板管理")
@RestController
@RequestMapping("/api/admin/coupon")
public class AdminCouponController {
    private final AdminCouponService adminCouponService;

    public AdminCouponController(AdminCouponService adminCouponService) {
        this.adminCouponService = adminCouponService;
    }

    @Operation(summary = "优惠券模板列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminCouponService.listTemplates(page, size));
    }

    @Operation(summary = "创建优惠券模板")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody CouponTemplate template) {
        adminCouponService.saveTemplate(template);
        return Result.success(null);
    }

    @Operation(summary = "更新优惠券模板")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody CouponTemplate template) {
        adminCouponService.saveTemplate(template);
        return Result.success(null);
    }

    @Operation(summary = "删除优惠券模板")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCouponService.deleteTemplate(id);
        return Result.success(null);
    }

    @Operation(summary = "发放优惠券给用户")
    @PostMapping("/grant")
    public Result<Void> grant(@RequestParam Long templateId, @RequestParam Long userId) {
        adminCouponService.grantToUser(templateId, userId);
        return Result.success(null);
    }
}
