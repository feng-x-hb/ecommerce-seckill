package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "收藏", description = "商品收藏管理")
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/toggle")
    public Result<Void> toggle(@RequestAttribute("userId") Long userId, @RequestParam Long productId) {
        favoriteService.toggle(userId, productId);
        return Result.success(null);
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check/{productId}")
    public Result<Boolean> check(@RequestAttribute("userId") Long userId, @PathVariable Long productId) {
        return Result.success(favoriteService.isFavorite(userId, productId));
    }

    @Operation(summary = "收藏列表查询")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestAttribute("userId") Long userId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return Result.success(favoriteService.list(userId, page, size));
    }

    @Operation(summary = "批量取消收藏")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestAttribute("userId") Long userId, @RequestBody List<Long> ids) {
        favoriteService.batchDelete(userId, ids);
        return Result.success(null);
    }
}
