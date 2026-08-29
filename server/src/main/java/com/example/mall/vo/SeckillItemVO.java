package com.example.mall.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品视图对象（SeckillItemVO）
 * 包含活动信息 + 商品信息 + 秒杀信息
 */
public class SeckillItemVO {

    private Long seckillItemId;
    private Long activityId;
    private String activityTitle;
    private Long skuId;
    private Long productId;
    private String productName;
    private String productImage;
    private String specs;
    private BigDecimal seckillPrice;
    private BigDecimal normalPrice;
    private Integer seckillStock;
    private Integer purchaseLimit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 活动状态：0未开始/1进行中/2已结束 */
    private Integer activityStatus;

    public Long getSeckillItemId() { return seckillItemId; }
    public void setSeckillItemId(Long seckillItemId) { this.seckillItemId = seckillItemId; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public String getSpecs() { return specs; }
    public void setSpecs(String specs) { this.specs = specs; }
    public BigDecimal getSeckillPrice() { return seckillPrice; }
    public void setSeckillPrice(BigDecimal seckillPrice) { this.seckillPrice = seckillPrice; }
    public BigDecimal getNormalPrice() { return normalPrice; }
    public void setNormalPrice(BigDecimal normalPrice) { this.normalPrice = normalPrice; }
    public Integer getSeckillStock() { return seckillStock; }
    public void setSeckillStock(Integer seckillStock) { this.seckillStock = seckillStock; }
    public Integer getPurchaseLimit() { return purchaseLimit; }
    public void setPurchaseLimit(Integer purchaseLimit) { this.purchaseLimit = purchaseLimit; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getActivityStatus() { return activityStatus; }
    public void setActivityStatus(Integer activityStatus) { this.activityStatus = activityStatus; }
}
