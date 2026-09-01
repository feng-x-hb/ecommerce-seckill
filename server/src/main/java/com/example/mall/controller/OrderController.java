package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.service.OrderService;
import com.example.mall.vo.OrderDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器（OrderController）
 *
 * 所有接口都需要登录（不在公开白名单中）。
 * 前端带 token → 拦截器解析 userId → Controller 用 @RequestAttribute 取出。
 */
@Tag(name = "订单", description = "订单创建、支付、查询")
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
    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<Map<String, String>> create(@RequestAttribute("userId") Long userId,
                                              @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skuItems = (List<Map<String, Object>>) body.get("skuItems");
        String receiverName = (String) body.get("receiverName");
        String receiverPhone = (String) body.get("receiverPhone");
        String receiverAddress = (String) body.get("receiverAddress");

        // 每商品优惠券 {skuId: couponId}
        Map<Long, Long> itemCoupons = new java.util.HashMap<>();
        if (body.get("itemCoupons") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = (Map<String, Object>) body.get("itemCoupons");
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                if (entry.getValue() != null) {
                    itemCoupons.put(Long.valueOf(entry.getKey()), Long.valueOf(entry.getValue().toString()));
                }
            }
        }

        String orderNo = orderService.createOrder(userId, skuItems, receiverName, receiverPhone, receiverAddress, itemCoupons);
        return Result.success(Map.of("orderNo", orderNo));
    }

    /**
     * 模拟支付
     * POST /api/order/{orderNo}/pay
     */
    @Operation(summary = "模拟支付")
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
    @Operation(summary = "取消订单")
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
    @Operation(summary = "订单列表查询")
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
    @Operation(summary = "订单详情查询")
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> detail(@RequestAttribute("userId") Long userId,
                                        @PathVariable String orderNo) {
        OrderDetailVO detail = orderService.detail(userId, orderNo);
        return Result.success(detail);
    }

    /**
     * 修改收货地址（仅待支付状态）
     * PUT /api/order/{orderNo}/address
     */
    @Operation(summary = "修改收货地址")
    @PutMapping("/{orderNo}/address")
    public Result<Void> updateAddress(@RequestAttribute("userId") Long userId,
                                      @PathVariable String orderNo,
                                      @RequestBody Map<String, String> body) {
        orderService.updateAddress(userId, orderNo,
                body.get("receiverName"),
                body.get("receiverPhone"),
                body.get("receiverAddress"));
        return Result.success(null);
    }
}
