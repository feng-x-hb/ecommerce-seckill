package com.example.mall.service;

import java.util.Map;

public interface ReviewService {
    void submit(Long userId, Long orderId, Long productId, Integer rating, String content, String images);
    Map<String, Object> productReviews(Long productId, int page, int size);
    Map<String, Object> pendingReviews(Long userId, int page, int size);
}
