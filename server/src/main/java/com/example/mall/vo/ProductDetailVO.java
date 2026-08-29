package com.example.mall.vo;

import com.example.mall.entity.Sku;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情视图对象（ProductDetailVO）
 * 返回给前端完整商品信息 + 该商品的所有 SKU 列表。
 */
public class ProductDetailVO {

    private Long id;
    private Long categoryId;
    private String title;
    private String subtitle;
    private String mainImage;
    private String images;
    private String detail;
    private BigDecimal price;
    private Integer status;
    private Integer sales;
    private List<Sku> skuList; // 该商品的所有规格

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
    public List<Sku> getSkuList() { return skuList; }
    public void setSkuList(List<Sku> skuList) { this.skuList = skuList; }
}
