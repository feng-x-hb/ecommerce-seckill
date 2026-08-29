package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.service.ProductService;
import com.example.mall.vo.ProductDetailVO;
import com.example.mall.vo.ProductVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品控制器（ProductController）
 * 
 * 对应接口设计文档：
 * - GET /api/product/list → 商品列表（分页/分类筛选/搜索）公开
 * - GET /api/product/{id} → 商品详情（含 SKU 列表）公开
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品列表（分页）
     *
     * 对应接口：GET /api/product/list
     * 公开接口（不需要登录），用于首页/商品列表页展示
     *
     * 参数（均可选）：
     *   page=1&size=10&categoryId=1&keyword=手机
     *
     * 返回：
     *   { "total": 100, "list": [{ "id":1, "title":"iPhone 15", ... }] }
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> data = productService.list(page, size, categoryId, keyword);
        return Result.success(data);
    }

    /**
     * 商品详情
     *
     * 对应接口：GET /api/product/{id}
     * 公开接口（不需要登录），用于商品详情页展示
     *
     * 返回：商品完整信息 + 该商品的所有 SKU 列表（规格、价格、库存）
     */
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        ProductDetailVO detail = productService.detail(id);
        return Result.success(detail);
    }
}
