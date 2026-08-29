package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.AdminOnly;
import com.example.mall.common.Result;
import com.example.mall.entity.Product;
import com.example.mall.entity.Sku;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.mapper.SkuMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员商品管理控制器
 * 管理员可以上架平台自营商品（seller_id = 管理员自己的 id）
 * 也可以管理所有商家的商品（审核/下架）
 */
@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;

    public AdminProductController(ProductMapper productMapper, SkuMapper skuMapper) {
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
    }

    /** 分页查询所有商品（管理员看全部，含下架商品） */
    @GetMapping("/list")
    @AdminOnly
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Product::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> result = productMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    /** 商品详情（含 SKU 列表） */
    @GetMapping("/{id}")
    @AdminOnly
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) return Result.error(400, "商品不存在");
        return Result.success(product);
    }

    /** 获取商品的 SKU 列表 */
    @GetMapping("/{id}/sku")
    @AdminOnly
    public Result<List<Sku>> skuList(@PathVariable Long id) {
        List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getProductId, id));
        return Result.success(skus);
    }

    /** 上架商品 */
    @PutMapping("/{id}/onshelf")
    @AdminOnly
    public Result<Void> onShelf(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) return Result.error(400, "商品不存在");
        product.setStatus(1);
        productMapper.updateById(product);
        return Result.success(null);
    }

    /** 下架商品 */
    @PutMapping("/{id}/offshelf")
    @AdminOnly
    public Result<Void> offShelf(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) return Result.error(400, "商品不存在");
        product.setStatus(0);
        productMapper.updateById(product);
        return Result.success(null);
    }

    /** 删除商品（管理员权限） */
    @DeleteMapping("/{id}")
    @AdminOnly
    public Result<Void> delete(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.success(null);
    }

    /** 添加平台自营商品 */
    @PostMapping
    @AdminOnly
    public Result<Product> add(@RequestBody Product product, @RequestAttribute("userId") Long userId) {
        product.setSellerId(userId);
        if (product.getStatus() == null) product.setStatus(1);
        if (product.getSales() == null) product.setSales(0);
        productMapper.insert(product);
        return Result.success(product);
    }

    /** 更新商品信息 */
    @PutMapping("/{id}")
    @AdminOnly
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productMapper.updateById(product);
        return Result.success(null);
    }
}
