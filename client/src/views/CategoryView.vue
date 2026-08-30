<script setup lang="ts">
/**
 * 分类商品列表页（CategoryView.vue）
 * 路由：/category/:id
 * 功能：根据分类ID展示商品列表，支持子分类筛选
 */
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductList } from '@/api/product'
import request from '@/api/request'
import ProductCard from '@/components/ProductCard.vue'
import type { Product, Category } from '@/types'

const route = useRoute()
const router = useRouter()

const category = ref<Category | null>(null)
const subcategories = ref<Category[]>([])
const products = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const activeSubId = ref<number | undefined>()

async function fetchCategoryInfo(catId: number) {
  try {
    const res: any = await request.get('/category/list')
    const all = res.data
    const found = all.find((c: Category) => c.id === catId)
    if (found) {
      category.value = found
      subcategories.value = (found as any).children || []
    }
  } catch { /* ignore */ }
}

async function fetchProducts() {
  loading.value = true
  try {
    const params: any = {
      page: page.value,
      size: size.value,
      categoryId: activeSubId.value || Number(route.params.id)
    }
    const res: any = await getProductList(params)
    products.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function selectSub(subId: number | undefined) {
  activeSubId.value = subId
  page.value = 1
  fetchProducts()
}

function handlePageChange(newPage: number) {
  page.value = newPage
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      activeSubId.value = undefined
      page.value = 1
      fetchCategoryInfo(Number(newId))
      fetchProducts()
    }
  }
)

onMounted(() => {
  const catId = Number(route.params.id)
  if (catId) {
    fetchCategoryInfo(catId)
    fetchProducts()
  }
})

const catEmojis: Record<string, string> = {
  '数码家电': '📱', '服饰鞋包': '👔', '家居日用': '🏠',
  '食品饮料': '🍜', '美妆个护': '💄', '运动户外': '⚽', '母婴玩具': '🍼'
}
</script>

<template>
  <div class="category-page">
    <!-- 分类头部 -->
    <div class="category-header">
      <div class="header-inner container">
        <button class="back-btn" @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon> 返回首页
        </button>
        <div class="cat-title" v-if="category">
          <span class="cat-emoji">{{ catEmojis[category.name] || '📦' }}</span>
          {{ category.name }}
        </div>
      </div>
    </div>

    <!-- 子分类筛选 -->
    <div class="sub-filter container" v-if="subcategories.length">
      <div
        class="sub-tag"
        :class="{ active: !activeSubId }"
        @click="selectSub(undefined)"
      >全部</div>
      <div
        v-for="sub in subcategories"
        :key="sub.id"
        class="sub-tag"
        :class="{ active: activeSubId === sub.id }"
        @click="selectSub(sub.id)"
      >{{ sub.name }}</div>
    </div>

    <!-- 商品列表 -->
    <div class="category-body container">
      <div v-if="loading" class="loading-wrap">
        <el-icon class="is-loading" :size="32" color="#e1251b"><Loading /></el-icon>
      </div>

      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <div class="empty-title">该分类暂无商品</div>
        <button class="go-home-btn" @click="router.push('/')">去首页逛逛</button>
      </div>

      <template v-else>
        <div class="product-grid">
          <ProductCard v-for="p in products" :key="p.id" :id="p.id" :title="p.title" :image="p.mainImage" :price="p.price" :original-price="p.originalPrice" :sales="p.sales" />
        </div>
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
.category-page { min-height: 100vh; background: var(--jd-bg); }
.category-header { background: #fff; border-bottom: 2px solid #f0f0f0; padding: 16px 0; }
.header-inner { display: flex; align-items: center; gap: 20px; margin-left: 60px; }
.back-btn {
  display: flex; align-items: center; gap: 4px; background: none; border: 1px solid #ddd;
  border-radius: 20px; padding: 6px 16px; cursor: pointer; font-size: 13px; color: #666;
  transition: all 0.3s;
}
.back-btn:hover { border-color: #e1251b; color: #e1251b; }
.cat-title { font-size: 20px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 8px; }
.cat-emoji { font-size: 28px; }

.sub-filter {
  display: flex; gap: 10px; padding: 16px 0; flex-wrap: wrap;
}
.sub-tag {
  padding: 6px 20px; border-radius: 20px; font-size: 13px; cursor: pointer;
  background: #fff; color: #666; border: 1px solid #eee; transition: all 0.3s;
}
.sub-tag:hover { border-color: #e1251b; color: #e1251b; }
.sub-tag.active {
  background: linear-gradient(135deg, #e1251b, #ff6700); color: #fff; border-color: transparent;
}

.category-body { padding: 0 0 60px; }
.loading-wrap { display: flex; justify-content: center; padding: 60px 0; }
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80px 0; }
.empty-icon { font-size: 64px; margin-bottom: 16px; }
.empty-title { font-size: 18px; color: #333; margin-bottom: 20px; }
.go-home-btn {
  background: linear-gradient(135deg, #e1251b, #ff6700); color: #fff; border: none;
  border-radius: 24px; padding: 10px 32px; font-size: 14px; cursor: pointer; transition: all 0.3s;
}
.go-home-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(225,37,27,0.35); }

.product-grid {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px;
}
.pagination-wrap { display: flex; justify-content: center; padding: 32px 0 0; }
</style>
