package com.example.mall.vo;

import java.math.BigDecimal;

/**
 * 商品列表视图对象（ProductVO）
 * 只含列表页需要的字段（不暴露 sellerId、detail 等内部数据）。
 */
public class ProductVO {

    private Long id;
    private String title;
    private String mainImage;
    private BigDecimal price;
    private Integer sales;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
}
