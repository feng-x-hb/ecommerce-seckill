package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.service.CartService;
import com.example.mall.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器（CartController）
 * 
 * 所有接口都需要登录（不进公开白名单），由 LoginInterceptor 拦截。
 * 前端带 token → 拦截器解析 userId → Controller 用 @RequestAttribute 取出。
 */
@Tag(name = "购物车", description = "购物车增删改查")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 加购物车
     * POST /api/cart
     * 参数：{ "skuId": 1, "quantity": 2 }
     * 逻辑：同一 SKU 已存在则数量累加，不存在则新增
     */
    @Operation(summary = "添加购物车")
    @PostMapping
    public Result<Void> add(@RequestAttribute("userId") Long userId,
                            @RequestBody java.util.Map<String, Object> body) {
        Long skuId = Long.valueOf(body.get("skuId").toString());
        Integer quantity = body.get("quantity") != null
                ? Integer.valueOf(body.get("quantity").toString()) : 1;
        cartService.add(userId, skuId, quantity);
        return Result.success(null);
    }

    /**
     * 购物车列表
     * GET /api/cart/list
     * 返回：SKU 信息 + 数量 + 勾选状态 + 小计
     */
    @Operation(summary = "购物车列表")
    @GetMapping("/list")
    public Result<List<CartVO>> list(@RequestAttribute("userId") Long userId) {
        List<CartVO> list = cartService.list(userId);
        return Result.success(list);
    }

    /**
     * 改数量
     * PUT /api/cart/{id}
     * 参数：{ "quantity": 5 }
     */
    @Operation(summary = "修改购物车数量")
    @PutMapping("/{id}")
    public Result<Void> updateQuantity(@RequestAttribute("userId") Long userId,
                                       @PathVariable Long id,
                                       @RequestBody java.util.Map<String, Object> body) {
        Integer quantity = Integer.valueOf(body.get("quantity").toString());
        cartService.updateQuantity(userId, id, quantity);
        return Result.success(null);
    }

    /**
     * 删除购物车项
     * DELETE /api/cart/{id}
     */
    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestAttribute("userId") Long userId,
                               @PathVariable Long id) {
        cartService.delete(userId, id);
        return Result.success(null);
    }

    /**
     * 批量删除购物车项
     * DELETE /api/cart/batch
     * 参数：[1, 2, 3] （购物车项 ID 列表）
     */
    @Operation(summary = "批量删除购物车项")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestAttribute("userId") Long userId,
                                    @RequestBody List<Long> ids) {
        cartService.batchDelete(userId, ids);
        return Result.success(null);
    }

    /**
     * 勾选/取消勾选
     * PUT /api/cart/{id}/checked
     * 参数：{ "checked": 1 } 或 { "checked": 0 }
     */
    @Operation(summary = "勾选/取消勾选购物车项")
    @PutMapping("/{id}/checked")
    public Result<Void> updateChecked(@RequestAttribute("userId") Long userId,
                                      @PathVariable Long id,
                                      @RequestBody java.util.Map<String, Object> body) {
        Integer checked = Integer.valueOf(body.get("checked").toString());
        cartService.updateChecked(userId, id, checked);
        return Result.success(null);
    }
}
