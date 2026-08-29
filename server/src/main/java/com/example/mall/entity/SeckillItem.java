package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品实体类（SeckillItem）
 */
@TableName("seckill_item")
public class SeckillItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long skuId;

    /** 秒杀价 */
    private BigDecimal seckillPrice;

    /** 秒杀库存（独立于普通库存） */
    private Integer seckillStock;

    /** 原价（展示用） */
    private BigDecimal normalPrice;

    /** 每人限购数量 */
    private Integer purchaseLimit;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public BigDecimal getSeckillPrice() { return seckillPrice; }
    public void setSeckillPrice(BigDecimal seckillPrice) { this.seckillPrice = seckillPrice; }
    public Integer getSeckillStock() { return seckillStock; }
    public void setSeckillStock(Integer seckillStock) { this.seckillStock = seckillStock; }
    public BigDecimal getNormalPrice() { return normalPrice; }
    public void setNormalPrice(BigDecimal normalPrice) { this.normalPrice = normalPrice; }
    public Integer getPurchaseLimit() { return purchaseLimit; }
    public void setPurchaseLimit(Integer purchaseLimit) { this.purchaseLimit = purchaseLimit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
