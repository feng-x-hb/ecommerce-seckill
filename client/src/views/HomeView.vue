<script setup lang="ts">
/**
 * 首页 - 华丽版
 * 大轮播图 + 分类导航 + 限时秒杀预告 + 为你推荐 + 热卖排行
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getProductList } from '@/api/product'
import request from '@/api/request'
import type { Product, Category } from '@/types'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()

const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const activeCategoryId = ref<number | undefined>()
const loading = ref(false)

// 轮播
const bannerIndex = ref(0)
const banners = [
  { gradient: 'linear-gradient(135deg, #e1251b, #ff6700)', icon: '🔥', title: '超级秒杀', sub: '限时限量 抢完即止', tag: '限时' },
  { gradient: 'linear-gradient(135deg, #667eea, #764ba2)', icon: '📱', title: '新品首发', sub: 'iPhone 15 Pro Max 震撼上市', tag: '新品' },
  { gradient: 'linear-gradient(135deg, #f093fb, #f5576c)', icon: '🎁', title: '品质生活', sub: '精选好物 品质保证', tag: '精选' },
  { gradient: 'linear-gradient(135deg, #4facfe, #00f2fe)', icon: '💰', title: '数码狂欢', sub: '爆款直降 限时优惠', tag: '特惠' }
]

// 倒计时
const now = ref(Date.now())
const targetTime = ref(new Date().setHours(23, 59, 59, 999))
let bannerTimer: ReturnType<typeof setInterval>
let countdownTimer: ReturnType<typeof setInterval>

const timeLeft = ref({ hours: 0, minutes: 0, seconds: 0 })

function updateCountdown() {
  const diff = targetTime.value - Date.now()
  if (diff <= 0) {
    timeLeft.value = { hours: 0, minutes: 0, seconds: 0 }
    return
  }
  timeLeft.value = {
    hours: Math.floor(diff / 3600000),
    minutes: Math.floor((diff % 3600000) / 60000),
    seconds: Math.floor((diff % 60000) / 1000)
  }
}

// 为你推荐数据（模拟）
const recommendedProducts = ref<Product[]>([])
const hotProducts = ref<Product[]>([])

async function fetchProducts() {
  loading.value = true
  try {
    const res: any = await getProductList({
      page: page.value, size: size.value,
      keyword: keyword.value || undefined,
      categoryId: activeCategoryId.value
    })
    products.value = res.data.list
    total.value = res.data.total
    recommendedProducts.value = res.data.list.slice(0, 5)
    hotProducts.value = res.data.list.slice(0, 4)
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res: any = await request.get('/category/list')
    categories.value = res.data.filter((c: Category) => c.status === 1)
  } catch { /* ignore */ }
}

function selectCategory(catId: number | undefined) {
  activeCategoryId.value = catId
  keyword.value = ''
  page.value = 1
  fetchProducts()
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
  updateCountdown()
  bannerTimer = setInterval(() => { bannerIndex.value = (bannerIndex.value + 1) % banners.length }, 5000)
  countdownTimer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  clearInterval(bannerTimer)
  clearInterval(countdownTimer)
})
</script>

<template>
  <div class="home-page">
    <!-- ========== 顶部轮播区 ========== -->
    <div class="hero-section container">
      <!-- 左侧分类 -->
      <div class="category-sidebar">
        <div class="sidebar-header">
          <el-icon><Grid /></el-icon> 全部商品分类
        </div>
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="cat-item"
          :class="{ active: activeCategoryId === cat.id }"
          @click="selectCategory(cat.id)"
        >
          <span class="cat-icon">📦</span>
          {{ cat.name }}
          <el-icon class="cat-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="cat-item" :class="{ active: !activeCategoryId }" @click="selectCategory(undefined)">
          <span class="cat-icon">🏷️</span> 全部
        </div>
      </div>

      <!-- 右侧轮播 -->
      <div class="banner-wrapper">
        <div class="banner-slide" :style="{ background: banners[bannerIndex].gradient }">
          <div class="banner-content animate-fade-in">
            <div class="banner-tag tag tag-red">{{ banners[bannerIndex].tag }}</div>
            <div class="banner-icon">{{ banners[bannerIndex].icon }}</div>
            <h2 class="banner-title">{{ banners[bannerIndex].title }}</h2>
            <p class="banner-sub">{{ banners[bannerIndex].sub }}</p>
            <button class="banner-btn">
              立即查看 <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
          <!-- 装饰圆 -->
          <div class="deco-circle deco-1"></div>
          <div class="deco-circle deco-2"></div>
          <div class="deco-circle deco-3"></div>
          <!-- 轮播指示器 -->
          <div class="banner-dots">
            <span v-for="(_, i) in banners" :key="i" class="dot" :class="{ active: i === bannerIndex }" @click="bannerIndex = i" />
          </div>
        </div>
      </div>

      <!-- 右侧信息卡 -->
      <div class="side-cards">
        <div class="side-card gradient-animated">
          <el-icon :size="24"><Timer /></el-icon>
          <div>
            <div class="side-card-title">限时秒杀</div>
            <div class="side-card-desc">每日上新</div>
          </div>
        </div>
        <div class="side-card" style="background:linear-gradient(135deg,#2baa6e,#34d058)">
          <el-icon :size="24"><CircleCheck /></el-icon>
          <div>
            <div class="side-card-title">品质保证</div>
            <div class="side-card-desc">正品行货</div>
          </div>
        </div>
        <div class="side-card" style="background:linear-gradient(135deg,#4facfe,#00f2fe)">
          <el-icon :size="24"><Van /></el-icon>
          <div>
            <div class="side-card-title">极速发货</div>
            <div class="side-card-desc">次日达</div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 限时秒杀倒计时 ========== -->
    <div class="section container">
      <div class="seckill-banner card">
        <div class="seckill-left">
          <span class="flash-icon animate-pulse">⚡</span>
          <div>
            <h3 class="seckill-title">限时秒杀</h3>
            <p class="seckill-sub">每日精选 限时限量</p>
          </div>
        </div>
        <div class="countdown-box">
          <span class="countdown-label">距结束</span>
          <div class="countdown-nums">
            <span class="num-block">{{ String(timeLeft.hours).padStart(2, '0') }}</span>
            <span class="num-sep">:</span>
            <span class="num-block">{{ String(timeLeft.minutes).padStart(2, '0') }}</span>
            <span class="num-sep">:</span>
            <span class="num-block">{{ String(timeLeft.seconds).padStart(2, '0') }}</span>
          </div>
        </div>
        <router-link to="/seckill" class="seckill-more">
          更多秒杀 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
    </div>

    <!-- ========== 为你推荐 ========== -->
    <div class="section container" v-if="recommendedProducts.length">
      <div class="section-header">
        <div class="section-title-group">
          <el-icon :size="24" color="#e1251b"><Star /></el-icon>
          <h2 class="section-title">为你推荐</h2>
        </div>
        <div class="section-desc">根据你的浏览推荐</div>
      </div>
      <div class="product-grid">
        <ProductCard
          v-for="(p, i) in recommendedProducts"
          :key="p.id"
          :id="p.id"
          :title="p.title"
          :image="p.mainImage"
          :price="p.price"
          :original-price="p.originalPrice"
          :sales="p.sales"
          :style="{ animationDelay: i * 0.1 + 's' }"
          class="animate-fade-in-up"
        />
      </div>
    </div>

    <!-- ========== 热卖排行 ========== -->
    <div class="section container" v-if="hotProducts.length">
      <div class="section-header">
        <div class="section-title-group">
          <el-icon :size="24" color="#ff6700"><TrendCharts /></el-icon>
          <h2 class="section-title">热卖排行</h2>
        </div>
        <div class="section-desc">大家都在买</div>
      </div>
      <div class="hot-grid">
        <div v-for="(p, i) in hotProducts" :key="p.id" class="hot-card card hover-lift" :style="{ animationDelay: i * 0.15 + 's' }" class2="animate-fade-in-up">
          <div class="hot-rank" :class="'rank-' + (i+1)">{{ i + 1 }}</div>
          <router-link :to="`/product/${p.id}`">
            <div class="hot-image" :style="{ background: `hsl(${p.id * 47 % 360}, 60%, 85%)` }">
              <span>{{ p.title.charAt(0) }}</span>
            </div>
            <div class="hot-info">
              <div class="hot-name ellipsis">{{ p.title }}</div>
              <div class="price">¥{{ p.price.toFixed(2) }}</div>
            </div>
          </router-link>
        </div>
      </div>
    </div>

    <!-- ========== 全部商品 ========== -->
    <div class="section container">
      <div class="section-header">
        <div class="section-title-group">
          <el-icon :size="24" color="#333"><Grid /></el-icon>
          <h2 class="section-title">全部商品</h2>
        </div>
        <div class="section-tags">
          <span
            v-for="cat in categories"
            :key="cat.id"
            class="filter-tag"
            :class="{ active: activeCategoryId === cat.id }"
            @click="selectCategory(cat.id)"
          >{{ cat.name }}</span>
        </div>
      </div>

      <div v-loading="loading" class="product-grid">
        <ProductCard
          v-for="(p, i) in products"
          :key="p.id"
          :id="p.id"
          :title="p.title"
          :image="p.mainImage"
          :price="p.price"
          :original-price="p.originalPrice"
          :sales="p.sales"
          :style="{ animationDelay: (i % 5) * 0.08 + 's' }"
          class="animate-fade-in-up"
        />
      </div>

      <div v-if="!loading && products.length === 0" class="empty-state">
        <div class="empty-icon animate-float">📦</div>
        <p>暂无商品</p>
      </div>

      <div class="pagination" v-if="total > size">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="(p: number) => { page = p; fetchProducts() }"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page { padding-bottom: 40px; }

/* ========== 轮播区 ========== */
.hero-section {
  display: flex;
  gap: 0;
  margin: 20px auto;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0,0,0,0.1);
}

.category-sidebar {
  width: 210px;
  background: linear-gradient(180deg, #2c2c2c, #1a1a1a);
  flex-shrink: 0;
}
.sidebar-header {
  padding: 14px 16px;
  color: #fff;
  font-size: 14px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.05);
}
.cat-item {
  padding: 10px 16px;
  color: #ccc;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}
.cat-item:hover, .cat-item.active {
  background: var(--jd-red);
  color: #fff;
  padding-left: 22px;
}
.cat-icon { font-size: 14px; }
.cat-arrow { margin-left: auto; opacity: 0; transition: opacity 0.3s; }
.cat-item:hover .cat-arrow { opacity: 1; }

.banner-wrapper { flex: 1; position: relative; }
.banner-slide {
  height: 340px;
  position: relative;
  display: flex;
  align-items: center;
  overflow: hidden;
}
.banner-content {
  position: relative;
  z-index: 2;
  padding: 40px;
  color: #fff;
}
.banner-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  margin-bottom: 12px;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(4px);
}
.banner-icon { font-size: 48px; margin-bottom: 12px; }
.banner-title { font-size: 36px; font-weight: bold; margin-bottom: 8px; text-shadow: 0 2px 8px rgba(0,0,0,0.2); }
.banner-sub { font-size: 16px; opacity: 0.9; margin-bottom: 20px; }
.banner-btn {
  padding: 10px 28px;
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.5);
  color: #fff;
  border-radius: 24px;
  cursor: pointer;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
  backdrop-filter: blur(4px);
}
.banner-btn:hover { background: #fff; color: #333; }

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
}
.deco-1 { width: 200px; height: 200px; top: -50px; right: 100px; animation: float 6s ease-in-out infinite; }
.deco-2 { width: 120px; height: 120px; bottom: -30px; right: 200px; animation: float 4s ease-in-out infinite 1s; }
.deco-3 { width: 80px; height: 80px; top: 50px; right: 50px; animation: float 5s ease-in-out infinite 0.5s; }

.banner-dots {
  position: absolute;
  bottom: 16px;
  right: 20px;
  display: flex;
  gap: 8px;
  z-index: 3;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255,255,255,0.4);
  cursor: pointer;
  transition: all 0.3s;
}
.dot.active { background: #fff; width: 28px; border-radius: 5px; }

.side-cards {
  width: 190px;
  display: flex;
  flex-direction: column;
  gap: 0;
  flex-shrink: 0;
}
.side-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: filter 0.3s;
}
.side-card:hover { filter: brightness(1.1); }
.side-card-title { font-weight: bold; }
.side-card-desc { font-size: 11px; opacity: 0.8; }

/* ========== 秒杀倒计时 ========== */
.seckill-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 30px;
  margin-top: 20px;
  background: linear-gradient(135deg, #fff5f5, #fff0f0);
  border: 1px solid #ffe0e0;
}
.seckill-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.flash-icon { font-size: 32px; }
.seckill-title { font-size: 20px; color: var(--jd-red); font-weight: bold; }
.seckill-sub { font-size: 13px; color: #999; }
.countdown-box { text-align: center; }
.countdown-label { font-size: 12px; color: #999; display: block; margin-bottom: 4px; }
.countdown-nums { display: flex; align-items: center; gap: 4px; }
.num-block {
  background: #333;
  color: #fff;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 18px;
  font-weight: bold;
  min-width: 40px;
  text-align: center;
}
.num-sep { font-size: 18px; font-weight: bold; color: var(--jd-red); }
.seckill-more {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--jd-red);
  font-size: 14px;
  font-weight: bold;
}

/* ========== Section ========== */
.section { margin-top: 30px; }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-title { font-size: 22px; font-weight: bold; }
.section-desc { font-size: 13px; color: #999; }

.section-tags { display: flex; gap: 8px; }
.filter-tag {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  background: #fff;
  color: #666;
  border: 1px solid #eee;
  transition: all 0.3s;
}
.filter-tag:hover { color: var(--jd-red); border-color: var(--jd-red); }
.filter-tag.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  min-height: 200px;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.hot-card {
  position: relative;
  padding: 16px;
  cursor: pointer;
}
.hot-rank {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: #ccc;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  z-index: 1;
}
.rank-1 { background: var(--jd-red); }
.rank-2 { background: var(--jd-orange); }
.rank-3 { background: var(--jd-gold); }

.hot-image {
  width: 100%;
  padding-top: 100%;
  border-radius: 8px;
  position: relative;
  margin-bottom: 10px;
}
.hot-image span {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 40px;
  color: rgba(255,255,255,0.7);
  font-weight: bold;
}
.hot-name { font-size: 13px; margin-bottom: 4px; }

.empty-state { text-align: center; padding: 80px 0; color: #ccc; }
.empty-icon { font-size: 60px; margin-bottom: 16px; }
.pagination { display: flex; justify-content: center; padding: 30px 0 20px; }

@media (max-width: 1200px) { .product-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 900px) { .product-grid, .hot-grid { grid-template-columns: repeat(3, 1fr); } }
</style>
