/** 通用后端响应格式 */
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

/** 分页结果 */
export interface PageResult<T> {
  total: number
  list: T[]
}

/** 用户信息 */
export interface UserInfo {
  id: number
  account: string
  nickname: string
  avatar: string
  signature: string
  role: string
}

/** 分类 */
export interface Category {
  id: number
  name: string
  parentId: number
  sortOrder: number
  status: number
}

/** 商品 */
export interface Product {
  id: number
  title: string
  subtitle: string
  mainImage: string
  price: number
  originalPrice: number
  categoryId: number
  categoryName: string
  sales: number
  status: number
  skuList: Sku[]
}

/** SKU */
export interface Sku {
  id: number
  productId: number
  specs: string
  price: number
  stock: number
  status: number
}

/** 购物车项 */
export interface CartItem {
  id: number
  skuId: number
  productId: number
  productName: string
  productImage: string
  specs: string
  price: number
  quantity: number
  checked: number
  subTotal: number
}

/** 订单 */
export interface Order {
  id: number
  orderNo: string
  status: number
  totalAmount: number
  discountAmount: number
  payAmount: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  payTime: string
  createdAt: string
}

/** 订单明细 */
export interface OrderItem {
  skuId: number
  productName: string
  productImage: string
  specDesc: string
  price: number
  quantity: number
  subTotal: number
}

/** 订单详情（含明细） */
export interface OrderDetail extends Order {
  items: OrderItem[]
}
