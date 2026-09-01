import request from './request'

export function getCouponList(params: { page?: number; size?: number }) {
  return request.get('/coupon/list', { params })
}

export function claimCoupon(templateId: number) {
  return request.post('/coupon/claim', null, { params: { templateId } })
}

export function getMyCoupons(params: { page?: number; size?: number }) {
  return request.get('/coupon/my', { params })
}
