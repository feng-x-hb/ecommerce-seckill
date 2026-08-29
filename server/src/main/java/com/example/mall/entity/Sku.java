package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品规格实体类（Sku）
 * 对应数据库表：sku
 * 和 product 是 N 对 1：每个 SKU 属于一个商品
 * 例：iPhone 15 有"黑/256G"和"白/128G"两个 SKU
 */
public class Sku {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属商品 id */
    private Long productId;

    /** 规格描述 JSON，如 {"颜色":"黑","容量":"256G"} */
    private String specs;

    /** 该规格的真实售价（下单以这个价为准） */
    private BigDecimal price;

    /** 库存（第一阶段直接放这里，UPDATE ... WHERE stock>0 防超卖） */
    private Integer stock;

    /** 状态：0停用 / 1启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getSpecs() { return specs; }
    public void setSpecs(String specs) { this.specs = specs; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
