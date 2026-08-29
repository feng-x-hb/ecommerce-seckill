import request from './request'

/** 加购物车 */
export function addToCart(skuId: number, quantity: number) {
  return request.post('/cart', { skuId, quantity })
}

/** 购物车列表 */
export function getCartList() {
  return request.get('/cart/list')
}

/** 改数量 */
export function updateCartQuantity(id: number, quantity: number) {
  return request.put(`/cart/${id}`, { quantity })
}

/** 删除购物车项 */
export function deleteCartItem(id: number) {
  return request.delete(`/cart/${id}`)
}

/** 勾选/取消勾选 */
export function updateCartChecked(id: number, checked: number) {
  return request.put(`/cart/${id}/checked`, { checked })
}
