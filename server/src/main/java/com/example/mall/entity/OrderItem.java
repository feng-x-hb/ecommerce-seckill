package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.math.BigDecimal;

/**
 * 订单明细实体类（OrderItem）
 * 对应数据库表：order_item
 * 快照机制：商品名/图/规格/单价在下单时原样复制，商品表后续改动不影响历史订单
 */
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属订单 id */
    private Long orderId;

    /** SKU id */
    private Long skuId;

    /** 商品名（快照） */
    private String productName;

    /** 商品图（快照） */
    private String productImage;

    /** 规格描述（快照） */
    private String specDesc;

    /** 成交单价（快照） */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 小计 = 单价 × 数量 */
    private BigDecimal subTotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public String getSpecDesc() { return specDesc; }
    public void setSpecDesc(String specDesc) { this.specDesc = specDesc; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
}
