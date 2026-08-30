import request from './request'

/** 创建订单 */
export function createOrder(data: {
  skuItems: { skuId: number; quantity: number }[]
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  couponId?: number | null
}) {
  return request.post('/order/create', data)
}

/** 模拟支付 */
export function payOrder(orderNo: string) {
  return request.post(`/order/${orderNo}/pay`)
}

/** 取消订单 */
export function cancelOrder(orderNo: string) {
  return request.post(`/order/${orderNo}/cancel`)
}

/** 订单列表 */
export function getOrderList(params: { page?: number; size?: number; status?: number }) {
  return request.get('/order/list', { params })
}

/** 订单详情 */
export function getOrderDetail(orderNo: string) {
  return request.get(`/order/${orderNo}`)
}

/** 修改收货地址 */
export function updateOrderAddress(orderNo: string, data: {
  receiverName: string
  receiverPhone: string
  receiverAddress: string
}) {
  return request.put(`/order/${orderNo}/address`, data)
}
