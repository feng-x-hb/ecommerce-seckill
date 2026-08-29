package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类（Product）
 * 对应数据库表：product
 * 和 sku 是 1 对 N：一个商品有多个规格（如 黑色 256G / 白色 128G）
 */
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商家 id（谁上架的） */
    private Long sellerId;

    /** 分类 id（属于哪个分类） */
    private Long categoryId;

    /** 商品标题 */
    private String title;

    /** 副标题/卖点 */
    private String subtitle;

    /** 主图 URL */
    private String mainImage;

    /** 图片列表（JSON 数组，如 ["url1","url2"]） */
    private String images;

    /** 商品详情（富文本 HTML） */
    private String detail;

    /** 列表展示价 = 当前最低 SKU 价（冗余，需同步） */
    private BigDecimal price;

    /** 状态：0下架 / 1上架 */
    private Integer status;

    /** 销量（冗余统计） */
    private Integer sales;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
