/**
 * 路由配置（router/index.ts）
 * 京东风格页面路由
 */
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue') },
    { path: '/', name: 'Home', component: () => import('@/views/HomeView.vue') },
    { path: '/search', name: 'Search', component: () => import('@/views/SearchView.vue') },
    { path: '/category/:id', name: 'Category', component: () => import('@/views/CategoryView.vue') },
    { path: '/profile', name: 'Profile', component: () => import('@/views/ProfileView.vue') },
    { path: '/favorites', name: 'Favorites', component: () => import('@/views/FavoritesView.vue') },
    { path: '/coupons', name: 'Coupons', component: () => import('@/views/CouponCenter.vue') },
    { path: '/seckill', name: 'Seckill', component: () => import('@/views/SeckillView.vue') },
    { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetailView.vue') },
    { path: '/cart', name: 'Cart', component: () => import('@/views/CartView.vue') },
    { path: '/checkout', name: 'Checkout', component: () => import('@/views/CheckoutView.vue') },
    { path: '/orders', name: 'Orders', component: () => import('@/views/OrderListView.vue') },
    { path: '/order/:orderNo', name: 'OrderDetail', component: () => import('@/views/OrderDetailView.vue') },
    { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/ForgotPasswordView.vue') },
    { path: '/merchant-login', name: 'MerchantLogin', component: () => import('@/views/MerchantLoginView.vue') },
    {
      path: '/seller',
      component: () => import('@/views/seller/SellerDashboard.vue'),
      meta: { requiresSeller: true },
      children: [
        { path: '', redirect: '/seller/products' },
        { path: 'products', name: 'SellerProducts', component: () => import('@/views/seller/SellerProductView.vue') },
        { path: 'orders', name: 'SellerOrders', component: () => import('@/views/seller/SellerOrderView.vue') },
        { path: 'stats', name: 'SellerStats', component: () => import('@/views/seller/SellerStatsView.vue') },
      ]
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminDashboard.vue'),
      meta: { requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/products' },
        { path: 'products', name: 'AdminProducts', component: () => import('@/views/admin/AdminProductView.vue') },
        { path: 'seckill', name: 'AdminSeckill', component: () => import('@/views/admin/AdminSeckillView.vue') },
        { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/AdminOrderView.vue') },
        { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/AdminUserView.vue') },
        { path: 'categories', name: 'AdminCategories', component: () => import('@/views/admin/AdminCategoryView.vue') },
        { path: 'coupons', name: 'AdminCoupons', component: () => import('@/views/admin/AdminCouponView.vue') },
      ]
    },
    { path: '/about', name: 'About', component: () => import('@/views/AboutView.vue') },
    { path: '/contact', name: 'Contact', component: () => import('@/views/ContactView.vue') },
    { path: '/merchant', name: 'Merchant', component: () => import('@/views/MerchantView.vue') }
  ],
  scrollBehavior() { return { top: 0 } }
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const publicPages = ['Login', 'Home', 'Search', 'Category', 'Profile', 'Favorites', 'Coupons', 'ProductDetail', 'Seckill', 'ForgotPassword', 'MerchantLogin', 'About', 'Contact', 'Merchant']
  if (!publicPages.includes(to.name as string) && !token) {
    return { name: 'Login' }
  }
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null
  // 管理员页面：需要 role=2
  if (to.matched.some(r => r.meta.requiresAdmin)) {
    if (!user || user.role !== 2) return { name: 'Home' }
  }
  // 商家页面：需要 role=1 或 role=2
  if (to.matched.some(r => r.meta.requiresSeller)) {
    if (!user || (user.role !== 1 && user.role !== 2)) return { name: 'Home' }
  }
})

export default router
