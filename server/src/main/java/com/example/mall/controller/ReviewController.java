package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "评价", description = "商品评价管理")
@RestController
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "提交商品评价")
    @PostMapping("/submit")
    public Result<Void> submit(@RequestAttribute("userId") Long userId,
                                @RequestParam Long orderId,
                                @RequestParam Long productId,
                                @RequestParam(defaultValue = "5") Integer rating,
                                @RequestParam(required = false) String content,
                                @RequestParam(required = false) String images) {
        reviewService.submit(userId, orderId, productId, rating, content, images);
        return Result.success(null);
    }

    @Operation(summary = "商品评价列表")
    @GetMapping("/product/{productId}")
    public Result<Map<String, Object>> productReviews(@PathVariable Long productId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.productReviews(productId, page, size));
    }

    @Operation(summary = "我的待评价列表")
    @GetMapping("/my")
    public Result<Map<String, Object>> myReviews(@RequestAttribute("userId") Long userId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.pendingReviews(userId, page, size));
    }
}
