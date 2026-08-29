package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.Result;
import com.example.mall.entity.Product;
import com.example.mall.entity.Sku;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.mapper.SkuMapper;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家商品管理控制器
 * 商家只能管理自己上架的商品（通过 seller_id 隔离）
 */
@RestController
@RequestMapping("/api/seller/product")
public class SellerProductController {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;

    public SellerProductController(ProductMapper productMapper, SkuMapper skuMapper) {
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
    }

    /** 分页查询自己的商品 */
    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, userId);
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

    /** 商品详情 */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getSellerId().equals(userId)) {
            return Result.error(400, "商品不存在");
        }
        return Result.success(product);
    }

    /** 获取商品的 SKU 列表 */
    @GetMapping("/{id}/sku")
    public Result<List<Sku>> skuList(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getSellerId().equals(userId)) {
            return Result.error(400, "无权访问");
        }
        List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getProductId, id));
        return Result.success(skus);
    }

    /** 添加商品 */
    @PostMapping
    public Result<Product> add(@RequestBody Product product, @RequestAttribute("userId") Long userId) {
        product.setSellerId(userId);
        if (product.getStatus() == null) product.setStatus(0);
        if (product.getSales() == null) product.setSales(0);
        if (product.getPrice() == null) product.setPrice(BigDecimal.ZERO);
        productMapper.insert(product);
        return Result.success(product);
    }

    /** 更新商品 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product, @RequestAttribute("userId") Long userId) {
        Product existing = productMapper.selectById(id);
        if (existing == null || !existing.getSellerId().equals(userId)) {
            return Result.error(400, "商品不存在或无权修改");
        }
        product.setId(id);
        productMapper.updateById(product);
        return Result.success(null);
    }

    /** 上架 */
    @PutMapping("/{id}/onshelf")
    public Result<Void> onShelf(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getSellerId().equals(userId)) return Result.error(400, "商品不存在");
        product.setStatus(1);
        productMapper.updateById(product);
        return Result.success(null);
    }

    /** 下架 */
    @PutMapping("/{id}/offshelf")
    public Result<Void> offShelf(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getSellerId().equals(userId)) return Result.error(400, "商品不存在");
        product.setStatus(0);
        productMapper.updateById(product);
        return Result.success(null);
    }

    /** 删除商品 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getSellerId().equals(userId)) return Result.error(400, "商品不存在");
        productMapper.deleteById(id);
        return Result.success(null);
    }

    /** 商家统计 */
    @GetMapping("/stats")
    public Result<Object> stats(@RequestAttribute("userId") Long userId) {
        Long totalProducts = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getSellerId, userId));
        Long onShelf = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getSellerId, userId).eq(Product::getStatus, 1));
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getSellerId, userId));
        int totalSales = products.stream().mapToInt(p -> p.getSales() != null ? p.getSales() : 0).sum();
        BigDecimal totalRevenue = products.stream()
                .map(p -> p.getPrice() != null ? p.getPrice().multiply(BigDecimal.valueOf(p.getSales() != null ? p.getSales() : 0)) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var stats = new java.util.HashMap<String, Object>();
        stats.put("totalProducts", totalProducts);
        stats.put("onShelf", onShelf);
        stats.put("totalSales", totalSales);
        stats.put("totalRevenue", totalRevenue);
        return Result.success(stats);
    }
}
