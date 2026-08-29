package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.service.OrderService;
import com.example.mall.vo.OrderDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器（OrderController）
 *
 * 所有接口都需要登录（不在公开白名单中）。
 * 前端带 token → 拦截器解析 userId → Controller 用 @RequestAttribute 取出。
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     * POST /api/order/create
     * 参数：{ skuItems: [{skuId, quantity}], receiverName, receiverPhone, receiverAddress }
     * 返回：订单号 orderNo
     */
    @PostMapping("/create")
    public Result<Map<String, String>> create(@RequestAttribute("userId") Long userId,
                                              @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skuItems = (List<Map<String, Object>>) body.get("skuItems");
        String receiverName = (String) body.get("receiverName");
        String receiverPhone = (String) body.get("receiverPhone");
        String receiverAddress = (String) body.get("receiverAddress");

        String orderNo = orderService.createOrder(userId, skuItems, receiverName, receiverPhone, receiverAddress);
        return Result.success(Map.of("orderNo", orderNo));
    }

    /**
     * 模拟支付
     * POST /api/order/{orderNo}/pay
     */
    @PostMapping("/{orderNo}/pay")
    public Result<Void> pay(@RequestAttribute("userId") Long userId,
                            @PathVariable String orderNo) {
        orderService.pay(userId, orderNo);
        return Result.success(null);
    }

    /**
     * 取消订单（恢复库存）
     * POST /api/order/{orderNo}/cancel
     */
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@RequestAttribute("userId") Long userId,
                               @PathVariable String orderNo) {
        orderService.cancel(userId, orderNo);
        return Result.success(null);
    }

    /**
     * 订单列表（分页）
     * GET /api/order/list?page=1&size=10&status=0（status 可选）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestAttribute("userId") Long userId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Integer status) {
        Map<String, Object> result = orderService.list(userId, page, size, status);
        return Result.success(result);
    }

    /**
     * 订单详情（含明细列表）
     * GET /api/order/{orderNo}
     */
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> detail(@RequestAttribute("userId") Long userId,
                                        @PathVariable String orderNo) {
        OrderDetailVO detail = orderService.detail(userId, orderNo);
        return Result.success(detail);
    }
}
