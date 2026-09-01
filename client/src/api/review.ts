import request from './request'

export function submitReview(data: { orderId: number; productId: number; rating?: number; content?: string; images?: string }) {
  return request.post('/review/submit', null, { params: data })
}

export function getProductReviews(productId: number, params: { page?: number; size?: number }) {
  return request.get(`/review/product/${productId}`, { params })
}

export function getMyReviews(params: { page?: number; size?: number }) {
  return request.get('/review/my', { params })
}
