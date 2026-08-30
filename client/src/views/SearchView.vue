<script setup lang="ts">
/**
 * 搜索结果页（SearchView.vue）
 * 路由：/search?keyword=xxx
 * 功能：根据关键词搜索商品，复用 ProductCard 组件展示结果
 */
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductList } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'
import type { Product } from '@/types'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const products = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

async function fetchProducts() {
  if (!keyword.value) return
  loading.value = true
  try {
    const res: any = await getProductList({
      page: page.value,
      size: size.value,
      keyword: keyword.value
    })
    products.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handlePageChange(newPage: number) {
  page.value = newPage
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 监听路由变化（从 Navbar 再次搜索时路由会更新）
watch(
  () => route.query.keyword,
  (newKw) => {
    if (newKw && typeof newKw === 'string') {
      keyword.value = newKw
      page.value = 1
      fetchProducts()
    }
  }
)

onMounted(() => {
  const kw = route.query.keyword
  if (kw && typeof kw === 'string') {
    keyword.value = kw
    fetchProducts()
  }
})
</script>

<template>
  <div class="search-page">
    <!-- 搜索结果头部 -->
    <div class="search-header">
      <div class="header-inner container">
        <button class="back-btn" @click="router.back()">
          <el-icon :size="18"><ArrowLeft /></el-icon>
          返回
        </button>
        <div class="search-title">
          <el-icon :size="20" style="color:#e1251b"><Search /></el-icon>
          <span>搜索 <em>"{{ keyword }}"</em> 的结果</span>
        </div>
        <div class="result-count" v-if="!loading">
          共找到 <strong>{{ total }}</strong> 件商品
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="search-body container">
      <!-- 加载态 -->
      <div v-if="loading" class="loading-wrap">
        <el-icon class="is-loading" :size="32" color="#e1251b"><Loading /></el-icon>
        <span>搜索中...</span>
      </div>

      <!-- 空状态 -->
      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon">🔍</div>
        <div class="empty-title">未找到相关商品</div>
        <div class="empty-desc">换个关键词试试吧~</div>
        <button class="go-home-btn" @click="router.push('/')">去首页逛逛</button>
      </div>

      <!-- 有结果 -->
      <template v-else>
        <div class="product-grid">
          <ProductCard
            v-for="p in products"
            :key="p.id"
            :id="p.id"
            :title="p.title"
            :image="p.mainImage"
            :price="p.price"
            :original-price="p.originalPrice"
            :sales="p.sales"
          />
        </div>

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="total > size">
          <el-pagination
            :current-page="page"
            :page-size="size"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  min-height: 100vh;
  background: var(--jd-bg);
}

.search-header {
  background: #fff;
  border-bottom: 2px solid #f0f0f0;
  padding: 20px 0;
}
.header-inner {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-left: 60px;
}
.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 6px 16px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
  transition: all 0.3s;
}
.back-btn:hover {
  border-color: #e1251b;
  color: #e1251b;
}
.search-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  color: #333;
}
.search-title em {
  color: #e1251b;
  font-style: normal;
  font-weight: 600;
}
.result-count {
  margin-left: auto;
  font-size: 13px;
  color: #999;
}
.result-count strong {
  color: #e1251b;
  font-size: 16px;
}

.search-body {
  padding: 24px 0 60px;
}

.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 80px 0;
  color: #999;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
}
.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}
.empty-title {
  font-size: 18px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
}
.empty-desc {
  font-size: 14px;
  color: #999;
  margin-bottom: 24px;
}
.go-home-btn {
  background: linear-gradient(135deg, #e1251b, #ff6700);
  color: #fff;
  border: none;
  border-radius: 24px;
  padding: 10px 32px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}
.go-home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(225,37,27,0.35);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 32px 0 0;
}
</style>
