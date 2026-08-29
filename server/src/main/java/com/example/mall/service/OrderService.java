package com.example.mall.service;

import com.example.mall.vo.OrderDetailVO;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口（OrderService）
 */
public interface OrderService {

    /**
     * 创建订单
     *
     * @param userId          当前用户 id
     * @param skuItems        购物车勾选项 [{skuId, quantity}]
     * @param receiverName    收货人
     * @param receiverPhone   收货电话
     * @param receiverAddress 收货地址
     * @return 订单号
     */
    String createOrder(Long userId, List<Map<String, Object>> skuItems,
                       String receiverName, String receiverPhone, String receiverAddress);

    /** 模拟支付 */
    void pay(Long userId, String orderNo);

    /** 取消订单（恢复库存） */
    void cancel(Long userId, String orderNo);

    /** 订单列表（分页） */
    Map<String, Object> list(Long userId, int page, int size, Integer status);

    /** 订单详情 */
    OrderDetailVO detail(Long userId, String orderNo);
}
