package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.entity.Product;
import com.example.mall.entity.Sku;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.mapper.SkuMapper;
import com.example.mall.service.ProductService;
import com.example.mall.vo.ProductDetailVO;
import com.example.mall.vo.ProductVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类（ProductServiceImpl）
 *
 * 核心知识点：
 *   - MyBatis-Plus 分页：new Page<>(页码, 每页条数) 作为参数传给 selectPage，框架自动 COUNT + 分页查询
 *   - LambdaQueryWrapper：类型安全的条件构建，不会写错字段名
 *   - 模糊查询：like() 生成 SQL 的 LIKE '%keyword%'
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;

    public ProductServiceImpl(ProductMapper productMapper, SkuMapper skuMapper) {
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
    }

    @Override
    public Map<String, Object> list(int page, int size, Long categoryId, String keyword) {
        // 构建查询条件：只查上架商品（status=1）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);

        // 按分类筛选
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 按关键词模糊搜索（标题包含关键词）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Product::getTitle, keyword.trim());
        }

        // 按销量降序排
        wrapper.orderByDesc(Product::getSales);

        // 分页查询
        Page<Product> pageResult = productMapper.selectPage(new Page<>(page, size), wrapper);

        // 转成 VO（只含列表页需要的字段）
        List<ProductVO> voList = pageResult.getRecords().stream().map(product -> {
            ProductVO vo = new ProductVO();
            vo.setId(product.getId());
            vo.setTitle(product.getTitle());
            vo.setMainImage(product.getMainImage());
            vo.setPrice(product.getPrice());
            vo.setSales(product.getSales());
            return vo;
        }).collect(Collectors.toList());

        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("list", voList);
        return result;
    }

    @Override
    public ProductDetailVO detail(Long id) {
        // 查商品基本信息
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }

        // 查该商品的所有 SKU（只查启用的）
        List<Sku> skuList = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getProductId, id)
                        .eq(Sku::getStatus, 1)
                        .orderByAsc(Sku::getPrice)); // 按价格升序，最低价排前面

        // 组装详情 VO
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setCategoryId(product.getCategoryId());
        vo.setTitle(product.getTitle());
        vo.setSubtitle(product.getSubtitle());
        vo.setMainImage(product.getMainImage());
        vo.setImages(product.getImages());
        vo.setDetail(product.getDetail());
        vo.setPrice(product.getPrice());
        vo.setStatus(product.getStatus());
        vo.setSales(product.getSales());
        vo.setSkuList(skuList);
        return vo;
    }
}
