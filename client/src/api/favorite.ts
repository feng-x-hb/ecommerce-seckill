import request from './request'

export function toggleFavorite(productId: number) {
  return request.post('/favorite/toggle', null, { params: { productId } })
}

export function checkFavorite(productId: number) {
  return request.get(`/favorite/check/${productId}`)
}

export function getFavoriteList(params: { page?: number; size?: number }) {
  return request.get('/favorite/list', { params })
}

export function batchDeleteFavorites(ids: number[]) {
  return request.delete('/favorite/batch', { data: ids })
}
