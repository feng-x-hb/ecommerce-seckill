package com.example.mall.service;

import com.example.mall.vo.CartVO;

import java.util.List;

/**
 * 购物车服务接口（CartService）
 */
public interface CartService {

    /** 加购物车（同一 SKU 已存在则数量累加） */
    void add(Long userId, Long skuId, Integer quantity);

    /** 获取购物车列表（含 SKU + 商品信息） */
    List<CartVO> list(Long userId);

    /** 改数量 */
    void updateQuantity(Long userId, Long cartItemId, Integer quantity);

    /** 删除购物车项 */
    void delete(Long userId, Long cartItemId);

    /** 勾选/取消勾选 */
    void updateChecked(Long userId, Long cartItemId, Integer checked);
}
