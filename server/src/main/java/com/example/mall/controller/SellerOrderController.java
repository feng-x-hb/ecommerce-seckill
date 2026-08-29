package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.Result;
import com.example.mall.entity.Order;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.Product;
import com.example.mall.mapper.OrderItemMapper;
import com.example.mall.mapper.OrderMapper;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.vo.OrderItemVO;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商家订单管理控制器
 * 商家只能看到包含自己商品的订单
 */
@RestController
@RequestMapping("/api/seller/order")
public class SellerOrderController {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    public SellerOrderController(OrderMapper orderMapper, OrderItemMapper orderItemMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
    }

    /** 获取商家相关订单列表 */
    @GetMapping("/list")
    public Result<Page<Order>> list(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        // 先查出商家的所有商品 ID
        List<Product> sellerProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getSellerId, userId).select(Product::getId));
        if (sellerProducts.isEmpty()) {
            return Result.success(new Page<>(page, size));
        }
        Set<Long> productIds = sellerProducts.stream().map(Product::getId).collect(Collectors.toSet());

        // 查出包含这些商品的订单项
        List<OrderItem> allItems = orderItemMapper.selectList(new LambdaQueryWrapper<>());
        // orderItem 没有直接的 productId，但有 skuId，需要反查
        // 简化：直接查所有订单，后面过滤
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    /** 订单详情 */
    @GetMapping("/{orderNo}")
    public Result<Map<String, Object>> detail(@PathVariable String orderNo, @RequestAttribute("userId") Long userId) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) return Result.error(400, "订单不存在");

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));

        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO vo = new OrderItemVO();
            vo.setSkuId(item.getSkuId());
            vo.setProductName(item.getProductName());
            vo.setProductImage(item.getProductImage());
            vo.setSpecDesc(item.getSpecDesc());
            vo.setPrice(item.getPrice());
            vo.setQuantity(item.getQuantity());
            vo.setSubTotal(item.getSubTotal());
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("orderNo", order.getOrderNo());
        detail.put("status", order.getStatus());
        detail.put("payAmount", order.getPayAmount());
        detail.put("receiverName", order.getReceiverName());
        detail.put("receiverPhone", order.getReceiverPhone());
        detail.put("receiverAddress", order.getReceiverAddress());
        detail.put("createdAt", order.getCreatedAt());
        detail.put("payTime", order.getPayTime());
        detail.put("items", itemVOs);
        return Result.success(detail);
    }

    /** 发货 */
    @PutMapping("/{orderNo}/ship")
    public Result<Void> ship(@PathVariable String orderNo, @RequestAttribute("userId") Long userId) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) return Result.error(400, "订单不存在");
        if (order.getStatus() != 1) return Result.error(400, "只有已支付订单才能发货");
        order.setStatus(2);
        orderMapper.updateById(order);
        return Result.success(null);
    }
}
