package com.example.mall.vo;

import java.math.BigDecimal;

/**
 * 购物车列表视图对象（CartVO）
 * 返回给前端：购物车项信息 + SKU 信息 + 商品信息
 * 需要查 3 张表拼出来：cart_item → sku → product
 */
public class CartVO {

    private Long id;           // 购物车项 id
    private Long skuId;        // SKU id
    private Long productId;    // 商品 id
    private String productName; // 商品名（快照）
    private String productImage; // 商品主图
    private String specs;      // 规格描述 JSON
    private BigDecimal price;  // SKU 单价
    private Integer quantity;  // 数量
    private Integer checked;   // 是否勾选
    private BigDecimal subTotal; // 小计 = price × quantity

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getChecked() { return checked; }
    public void setChecked(Integer checked) { this.checked = checked; }
    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
}
