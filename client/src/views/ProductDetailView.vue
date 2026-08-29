<script setup lang="ts">
/**
 * 商品详情页（ProductDetailView.vue）
 * 京东风格：左图右信息 + SKU 选择器 + 加购按钮
 */
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import type { Product, Sku } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const product = ref<Product | null>(null)
const selectedSku = ref<Sku | null>(null)
const quantity = ref(1)
const loading = ref(true)

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

onMounted(fetchProduct)
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
            <span class="big-icon">{{ product.title.charAt(0) }}</span>
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
</style>
