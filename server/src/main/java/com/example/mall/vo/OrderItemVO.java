package com.example.mall.vo;

import java.math.BigDecimal;

/**
 * 订单明细视图对象（OrderItemVO）
 * 用于订单详情页展示每个商品的快照信息
 */
public class OrderItemVO {

    private Long skuId;
    private String productName;
    private String productImage;
    private String specDesc;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;

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
