package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.AdminOnly;
import com.example.mall.common.Result;
import com.example.mall.entity.Order;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.User;
import com.example.mall.mapper.OrderItemMapper;
import com.example.mall.mapper.OrderMapper;
import com.example.mall.mapper.UserMapper;
import com.example.mall.vo.OrderDetailVO;
import com.example.mall.vo.OrderItemVO;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员订单审核控制器
 * 查看所有订单，审核并发货
 */
@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    public AdminOrderController(OrderMapper orderMapper, OrderItemMapper orderItemMapper, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userMapper = userMapper;
    }

    /** 分页查询所有订单（管理员看全部） */
    @GetMapping("/list")
    @AdminOnly
    public Result<Page<Order>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Order::getOrderNo, keyword);
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    /** 订单详情（含商品明细 + 买家信息） */
    @GetMapping("/{orderNo}")
    @AdminOnly
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) return Result.error(400, "订单不存在");

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));

        // 转换为 OrderItemVO
        List<OrderItemVO> itemVOs = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemVO vo = new OrderItemVO();
            vo.setSkuId(item.getSkuId());
            vo.setProductName(item.getProductName());
            vo.setProductImage(item.getProductImage());
            vo.setSpecDesc(item.getSpecDesc());
            vo.setPrice(item.getPrice());
            vo.setQuantity(item.getQuantity());
            vo.setSubTotal(item.getSubTotal());
            itemVOs.add(vo);
        }

        User buyer = userMapper.selectById(order.getUserId());

        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setPayTime(order.getPayTime());
        vo.setItems(itemVOs);
        if (buyer != null) {
            vo.setBuyerName(buyer.getUsername());
            vo.setBuyerPhone(buyer.getPhone());
        }
        return Result.success(vo);
    }

    /** 审核通过：标记为已发货 */
    @PutMapping("/{orderNo}/ship")
    @AdminOnly
    public Result<Void> ship(@PathVariable String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) return Result.error(400, "订单不存在");
        if (order.getStatus() != 1) return Result.error(400, "只有已支付订单才能发货");
        order.setStatus(2);
        orderMapper.updateById(order);
        return Result.success(null);
    }

    /** 驳回订单（标记为已取消） */
    @PutMapping("/{orderNo}/reject")
    @AdminOnly
    public Result<Void> reject(@PathVariable String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) return Result.error(400, "订单不存在");
        if (order.getStatus() != 1) return Result.error(400, "只有已支付订单才能驳回");
        order.setStatus(4);
        orderMapper.updateById(order);
        return Result.success(null);
    }
}
