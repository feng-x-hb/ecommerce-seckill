package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.Review;
import com.example.mall.entity.User;
import com.example.mall.mapper.ReviewMapper;
import com.example.mall.mapper.UserMapper;
import com.example.mall.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;

    public ReviewServiceImpl(ReviewMapper reviewMapper, UserMapper userMapper) {
        this.reviewMapper = reviewMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public void submit(Long userId, Long orderId, Long productId, Integer rating, String content, String images) {
        Long count = reviewMapper.selectCount(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .eq(Review::getOrderId, orderId)
                .eq(Review::getProductId, productId)
        );
        if (count > 0) {
            throw new BusinessException("您已评价过该商品");
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setOrderId(orderId);
        review.setProductId(productId);
        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        reviewMapper.insert(review);
    }

    @Override
    public Map<String, Object> productReviews(Long productId, int page, int size) {
        Page<Review> reviewPage = reviewMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, productId)
                .orderByDesc(Review::getCreatedAt)
        );
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (Review r : reviewPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("rating", r.getRating());
            item.put("content", r.getContent());
            item.put("images", r.getImages());
            item.put("createdAt", r.getCreatedAt());
            User user = userMapper.selectById(r.getUserId());
            item.put("nickname", user != null ? user.getNickname() : "匿名用户");
            list.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", reviewPage.getTotal());
        result.put("list", list);
        return result;
    }

    @Override
    public Map<String, Object> pendingReviews(Long userId, int page, int size) {
        Page<Review> reviewPage = reviewMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreatedAt)
        );
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (Review r : reviewPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("productId", r.getProductId());
            item.put("orderId", r.getOrderId());
            item.put("rating", r.getRating());
            item.put("content", r.getContent());
            item.put("createdAt", r.getCreatedAt());
            list.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", reviewPage.getTotal());
        result.put("list", list);
        return result;
    }
}
