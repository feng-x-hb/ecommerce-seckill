package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.CartItem;
import com.example.mall.entity.Product;
import com.example.mall.entity.Sku;
import com.example.mall.mapper.CartItemMapper;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.mapper.SkuMapper;
import com.example.mall.service.CartService;
import com.example.mall.vo.CartVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类（CartServiceImpl）
 *
 * 核心知识点：
 *   - 联合唯一索引 (user_id, sku_id) 兜底：同一用户+同一 SKU 只能有一行
 *   - 加购策略：先查是否存在 → 存在则数量累加，不存在则新增
 *   - 列表查询：需要查 3 张表拼装（cart_item → sku → product）
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;

    public CartServiceImpl(CartItemMapper cartItemMapper, SkuMapper skuMapper, ProductMapper productMapper) {
        this.cartItemMapper = cartItemMapper;
        this.skuMapper = skuMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public void add(Long userId, Long skuId, Integer quantity) {
        // 校验 SKU 是否存在且启用
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null || sku.getStatus() != 1) {
            throw new BusinessException("商品规格不存在或已停用");
        }

        // 查是否已有同一 SKU 在购物车
        CartItem existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getSkuId, skuId));

        if (existing != null) {
            // 已存在：数量累加
            existing.setQuantity(existing.getQuantity() + quantity);
            cartItemMapper.updateById(existing);
        } else {
            // 不存在：新增
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setSkuId(skuId);
            item.setQuantity(quantity);
            item.setChecked(1);
            cartItemMapper.insert(item);
        }
    }

    @Override
    public List<CartVO> list(Long userId) {
        // 第 1 步：查该用户所有购物车项
        List<CartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .orderByDesc(CartItem::getCreatedAt));

        if (items.isEmpty()) {
            return List.of();
        }

        // 第 2 步：收集所有 skuId，批量查 SKU（避免 N+1）
        Set<Long> skuIds = items.stream()
                .map(CartItem::getSkuId)
                .collect(Collectors.toSet());
        List<Sku> skuList = skuMapper.selectBatchIds(skuIds);
        Map<Long, Sku> skuMap = skuList.stream()
                .collect(Collectors.toMap(Sku::getId, s -> s));

        // 第 3 步：收集所有 productId，批量查商品（避免 N+1）
        Set<Long> productIds = skuList.stream()
                .map(Sku::getProductId)
                .collect(Collectors.toSet());
        List<Product> productList = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 第 4 步：组装 CartVO
        return items.stream().map(item -> {
            CartVO vo = new CartVO();
            vo.setId(item.getId());
            vo.setSkuId(item.getSkuId());
            vo.setChecked(item.getChecked());
            vo.setQuantity(item.getQuantity());

            Sku sku = skuMap.get(item.getSkuId());
            if (sku != null) {
                vo.setSpecs(sku.getSpecs());
                vo.setPrice(sku.getPrice());
                vo.setSubTotal(sku.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

                Product product = productMap.get(sku.getProductId());
                if (product != null) {
                    vo.setProductId(product.getId());
                    vo.setProductName(product.getTitle());
                    vo.setProductImage(product.getMainImage());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        if (quantity < 1) {
            throw new BusinessException("数量不能小于1");
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long cartItemId) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        cartItemMapper.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void updateChecked(Long userId, Long cartItemId, Integer checked) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        item.setChecked(checked);
        cartItemMapper.updateById(item);
    }
}
