package com.example.mall.service;

import com.example.mall.vo.ProductDetailVO;
import com.example.mall.vo.ProductVO;

import java.util.Map;

/**
 * 商品服务接口（ProductService）
 */
public interface ProductService {

    /**
     * 分页查询商品列表
     *
     * @param page       页码（从 1 开始）
     * @param size       每页条数
     * @param categoryId 分类 id（可选，null 不筛选）
     * @param keyword    搜索关键词（可选，null 不筛选，模糊匹配标题）
     * @return Map：{ "total": 总条数, "list": 商品列表 }
     */
    Map<String, Object> list(int page, int size, Long categoryId, String keyword);

    /**
     * 查询商品详情（含 SKU 列表）
     *
     * @param id 商品 id
     * @return 商品详情（含规格列表）
     */
    ProductDetailVO detail(Long id);
}
