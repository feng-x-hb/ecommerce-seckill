import request from './request'

/** 商品列表（分页+搜索+分类筛选） */
export function getProductList(params: { page?: number; size?: number; keyword?: string; categoryId?: number }) {
  return request.get('/product/list', { params })
}

/** 商品详情（含 SKU 列表） */
export function getProductDetail(id: number) {
  return request.get(`/product/${id}`)
}
