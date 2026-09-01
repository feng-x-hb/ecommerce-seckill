package com.example.mall.service;

import java.util.List;
import java.util.Map;

public interface FavoriteService {
    void toggle(Long userId, Long productId);
    boolean isFavorite(Long userId, Long productId);
    Map<String, Object> list(Long userId, int page, int size);
    void batchDelete(Long userId, List<Long> ids);
}
