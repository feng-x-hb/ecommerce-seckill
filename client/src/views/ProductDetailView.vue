<script setup lang="ts">
/**
 * 商品详情页（ProductDetailView.vue）
 * 京东风格：左图右信息 + SKU 选择器 + 加购按钮
 */
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import { toggleFavorite, checkFavorite } from '@/api/favorite'
import { getProductReviews } from '@/api/review'
import type { Product, Sku } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const product = ref<Product | null>(null)
const selectedSku = ref<Sku | null>(null)
const quantity = ref(1)
const loading = ref(true)
const isFavorited = ref(false)
const reviews = ref<any[]>([])
const reviewTotal = ref(0)

async function fetchProduct() {
  loading.value = true
  try {
    const res: any = await getProductDetail(Number(route.params.id))
    product.value = res.data
    // 默认选中第一个有库存的 SKU
    if (res.data.skuList?.length) {
      selectedSku.value = res.data.skuList.find((s: Sku) => s.stock > 0) || res.data.skuList[0]
    }
  } finally {
    loading.value = false
  }
}

function selectSku(sku: Sku) {
  selectedSku.value = sku
  quantity.value = 1
}

async function handleAddCart() {
  if (!selectedSku.value) return ElMessage.warning('请选择规格')
  if (selectedSku.value.stock <= 0) return ElMessage.warning('库存不足')
  try {
    await addToCart(selectedSku.value.id, quantity.value)
    ElMessage.success('已加入购物车')
  } catch { /* interceptor handles */ }
}

function handleBuyNow() {
  handleAddCart().then(() => router.push('/cart'))
}

async function handleToggleFavorite() {
  try {
    await toggleFavorite(Number(route.params.id))
    isFavorited.value = !isFavorited.value
    ElMessage.success(isFavorited.value ? '已收藏' : '已取消收藏')
  } catch { /* interceptor handles */ }
}

async function fetchReviews() {
  try {
    const res: any = await getProductReviews(Number(route.params.id), { page: 1, size: 5 })
    reviews.value = res.data.list || []
    reviewTotal.value = res.data.total || 0
  } catch { /* ignore */ }
}

onMounted(async () => {
  await fetchProduct()
  fetchReviews()
  try {
    const res: any = await checkFavorite(Number(route.params.id))
    isFavorited.value = res.data
  } catch { /* ignore */ }
})
</script>

<template>
  <div class="detail-page container" v-loading="loading">
    <template v-if="product">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span class="sep">></span>
        <span>{{ product.categoryName }}</span>
        <span class="sep">></span>
        <span>{{ product.title }}</span>
      </div>

      <!-- 主体区域 -->
      <div class="detail-main card">
        <!-- 左侧商品图 -->
        <div class="detail-image">
          <div class="big-image" :style="{ background: `hsl(${product.id * 47 % 360}, 60%, 85%)` }">
            <img v-if="product.mainImage" :src="product.mainImage" :alt="product.title" class="detail-img" @error="($event.target as HTMLImageElement).style.display='none'" />
            <span v-if="!product.mainImage" class="big-icon">{{ product.title.charAt(0) }}</span>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="detail-info">
          <h1 class="detail-title">{{ product.title }}</h1>
          <p class="detail-subtitle">{{ product.subtitle }}</p>

          <div class="price-box">
            <span class="label">价格</span>
            <span class="price price-lg">¥{{ (selectedSku?.price || product.price).toFixed(2) }}</span>
            <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice.toFixed(2) }}</span>
          </div>

          <div class="sales-box">
            <span class="label">销量</span>
            <span class="sales-value">{{ product.sales || 0 }}件</span>
          </div>

          <!-- SKU 选择 -->
          <div class="sku-section" v-if="product.skuList?.length">
            <div class="label">选择规格</div>
            <div class="sku-list">
              <div
                v-for="sku in product.skuList"
                :key="sku.id"
                class="sku-item"
                :class="{ active: selectedSku?.id === sku.id, disabled: sku.stock <= 0 }"
                @click="selectSku(sku)"
              >
                {{ sku.specs }}
                <span class="sku-stock" v-if="sku.stock <= 0">已售罄</span>
              </div>
            </div>
          </div>

          <!-- 数量 -->
          <div class="quantity-section">
            <span class="label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="selectedSku?.stock || 99" size="default" />
            <span class="stock-hint">库存 {{ selectedSku?.stock || 0 }} 件</span>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="handleBuyNow" :disabled="!selectedSku || selectedSku.stock <= 0">
              立即购买
            </el-button>
            <el-button size="large" @click="handleAddCart" :disabled="!selectedSku || selectedSku.stock <= 0">
              加入购物车
            </el-button>
            <el-button size="large" :type="isFavorited ? 'danger' : 'default'" @click="handleToggleFavorite">
              {{ isFavorited ? '❤ 已收藏' : '♡ 收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 评价区域 -->
      <div class="review-section card" v-if="reviews.length || reviewTotal > 0">
        <div class="section-header">
          <h3>商品评价</h3>
          <span class="review-count">共 {{ reviewTotal }} 条评价</span>
        </div>
        <div class="review-list">
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="review-user">
              <span class="review-name">{{ r.nickname }}</span>
              <span class="review-stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
              <span class="review-time">{{ r.createdAt?.slice(0, 10) }}</span>
            </div>
            <div class="review-content">{{ r.content }}</div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail-page { padding: 20px 15px; }

.breadcrumb {
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
}
.breadcrumb a { color: #666; }
.breadcrumb a:hover { color: var(--jd-red); }
.sep { margin: 0 6px; color: #ccc; }

.detail-main {
  display: flex;
  gap: 30px;
  padding: 30px;
}

.detail-image {
  width: 400px;
  flex-shrink: 0;
}
.big-image {
  width: 100%;
  padding-top: 100%;
  border-radius: 8px;
  position: relative;
}
.big-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 80px;
  color: rgba(255,255,255,0.7);
  font-weight: bold;
}
.detail-img {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  object-fit: contain;
  border-radius: 8px;
  z-index: 2;
}

.detail-info {
  flex: 1;
}
.detail-title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 8px;
  line-height: 1.4;
}
.detail-subtitle {
  color: #999;
  font-size: 14px;
  margin-bottom: 20px;
}

.price-box, .sales-box {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 14px 16px;
  background: #fafafa;
  border-radius: 6px;
  margin-bottom: 12px;
}
.label {
  color: #999;
  font-size: 13px;
  min-width: 36px;
}
.sales-value { color: #666; font-size: 14px; }

.sku-section {
  margin: 16px 0;
}
.sku-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}
.sku-item {
  padding: 8px 18px;
  border: 1px solid var(--jd-border);
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  position: relative;
}
.sku-item:hover { border-color: var(--jd-red); color: var(--jd-red); }
.sku-item.active { border-color: var(--jd-red); color: var(--jd-red); background: #fff0f0; }
.sku-item.disabled { color: #ccc; border-color: #eee; cursor: not-allowed; }
.sku-stock {
  display: block;
  font-size: 11px;
  color: #ccc;
  text-align: center;
  margin-top: 2px;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
}
.stock-hint { color: #999; font-size: 13px; }

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.review-section {
  margin-top: 24px;
  padding: 24px 30px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}
.review-count {
  font-size: 13px;
  color: #999;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.review-item {
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.review-item:last-child {
  border-bottom: none;
}
.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.review-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.review-stars {
  color: #ff9900;
  font-size: 14px;
}
.review-time {
  font-size: 12px;
  color: #ccc;
}
.review-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}
</style>
