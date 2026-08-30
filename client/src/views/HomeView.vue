<script setup lang="ts">
/**
 * 首页 - 华丽3D版
 * 鼠标粒子 + 3D卡片 + 滚动入场 + 自定义图标 + Mesh渐变背景
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductList } from '@/api/product'
import request from '@/api/request'
import type { Product, Category } from '@/types'
import ProductCard from '@/components/ProductCard.vue'
import CategoryIcon from '@/components/CategoryIcon.vue'
import { useMouseParticles } from '@/composables/useMouseParticles'

const route = useRoute()
const router = useRouter()
const heroRef = ref<HTMLElement>()

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
  { gradient: 'linear-gradient(135deg, #e1251b 0%, #ff6700 100%)', icon: '🔥', title: '超级秒杀', sub: '限时限量 抢完即止', tag: '限时', link: '/seckill' },
  { gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', icon: '📱', title: '新品首发', sub: 'iPhone 15 Pro Max 震撼上市', tag: '新品', link: '/search?keyword=新品' },
  { gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', icon: '🎁', title: '品质生活', sub: '精选好物 品质保证', tag: '精选', link: '/coupons' },
  { gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', icon: '💰', title: '数码狂欢', sub: '爆款直降 限时优惠', tag: '特惠', link: '/category/1' }
]

// 倒计时
const now = ref(Date.now())
const targetTime = ref(new Date().setHours(23, 59, 59, 999))
let bannerTimer: ReturnType<typeof setInterval>
let countdownTimer: ReturnType<typeof setInterval>

function resetBannerTimer() {
  clearInterval(bannerTimer)
  bannerTimer = setInterval(() => { bannerIndex.value = (bannerIndex.value + 1) % banners.length }, 5000)
}

function bannerPrev() {
  bannerIndex.value = (bannerIndex.value - 1 + banners.length) % banners.length
  resetBannerTimer()
}

function bannerNext() {
  bannerIndex.value = (bannerIndex.value + 1) % banners.length
  resetBannerTimer()
}

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
    categories.value = res.data.filter((c: Category) => c.status === 0)
  } catch { /* ignore */ }
}

function selectCategory(catId: number | undefined) {
  if (catId) {
    router.push(`/category/${catId}`)
  } else {
    activeCategoryId.value = undefined
    keyword.value = ''
    page.value = 1
    fetchProducts()
  }
}

// 鼠标粒子
useMouseParticles(heroRef)

onMounted(async () => {
  await fetchProducts()
  await fetchCategories()
  updateCountdown()
  resetBannerTimer()
  countdownTimer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  clearInterval(bannerTimer)
  clearInterval(countdownTimer)
})
</script>

<template>
  <div class="home-page">
    <!-- ========== 顶部通知条 ========== -->
    <div class="notice-bar">
      <div class="notice-inner container">
        <span class="notice-tag">📢 公告</span>
        <div class="notice-scroll">
          <span class="notice-text">新用户注册立减5元 | 全场满99包邮 | 每日10点秒杀上新</span>
        </div>
      </div>
    </div>

    <!-- ========== 顶部轮播区（带鼠标粒子） ========== -->
    <div class="hero-section container" ref="heroRef">
      <!-- 左侧分类 -->
      <div class="category-sidebar">
        <div class="sidebar-header">
          <el-icon><Grid /></el-icon> 全部商品分类
        </div>
        <div
          v-for="(cat, idx) in categories"
          :key="cat.id"
          class="cat-item"
          :class="{ active: activeCategoryId === cat.id }"
          @click="selectCategory(cat.id)"
        >
          <CategoryIcon :name="cat.name" :size="18" />
          <span class="cat-name">{{ cat.name }}</span>
          <el-icon class="cat-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="cat-item" :class="{ active: !activeCategoryId }" @click="selectCategory(undefined)">
          <CategoryIcon name="全部" :size="18" />
          <span class="cat-name">全部</span>
        </div>
      </div>

      <!-- 右侧轮播 -->
      <div class="banner-wrapper">
        <button class="banner-arrow banner-arrow-left" @click="bannerPrev">
          <el-icon :size="20"><ArrowLeft /></el-icon>
        </button>
        <button class="banner-arrow banner-arrow-right" @click="bannerNext">
          <el-icon :size="20"><ArrowRight /></el-icon>
        </button>
        <div class="banner-slide" :style="{ background: banners[bannerIndex].gradient }">
          <div class="banner-content animate-fade-in" :key="bannerIndex">
            <div class="banner-tag">{{ banners[bannerIndex].tag }}</div>
            <div class="banner-icon">{{ banners[bannerIndex].icon }}</div>
            <h2 class="banner-title">{{ banners[bannerIndex].title }}</h2>
            <p class="banner-sub">{{ banners[bannerIndex].sub }}</p>
            <router-link :to="banners[bannerIndex].link" class="banner-btn">
              立即查看 <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
          <!-- 轮播指示器 -->
          <div class="banner-dots">
            <span v-for="(_, i) in banners" :key="i" class="dot" :class="{ active: i === bannerIndex }" @click="bannerIndex = i" />
          </div>
        </div>
      </div>

      <!-- 右侧信息卡 -->
      <div class="side-cards">
        <div class="side-card side-card-red">
          <div class="side-icon-wrap"><el-icon :size="24"><Timer /></el-icon></div>
          <div>
            <div class="side-card-title">限时秒杀</div>
            <div class="side-card-desc">每日上新</div>
          </div>
        </div>
        <div class="side-card side-card-green">
          <div class="side-icon-wrap"><el-icon :size="24"><CircleCheck /></el-icon></div>
          <div>
            <div class="side-card-title">品质保证</div>
            <div class="side-card-desc">正品行货</div>
          </div>
        </div>
        <div class="side-card side-card-blue">
          <div class="side-icon-wrap"><el-icon :size="24"><Van /></el-icon></div>
          <div>
            <div class="side-card-title">极速发货</div>
            <div class="side-card-desc">次日达</div>
          </div>
        </div>
        <div class="side-card side-card-purple">
          <div class="side-icon-wrap"><el-icon :size="24"><Service /></el-icon></div>
          <div>
            <div class="side-card-title">售后服务</div>
            <div class="side-card-desc">7天退换</div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 限时秒杀倒计时 ========== -->
    <div class="section container">
      <div class="seckill-banner card">
        <div class="seckill-left">
          <div class="flash-badge">⚡</div>
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
          <span class="title-icon-box" style="background:linear-gradient(135deg,#e1251b,#ff6700)"><el-icon :size="18" color="#fff"><Star /></el-icon></span>
          <h2 class="section-title">为你推荐</h2>
          <span class="title-line"></span>
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
        />
      </div>
    </div>

    <!-- ========== 热卖排行 ========== -->
    <div class="section container" v-if="hotProducts.length">
      <div class="section-header">
        <div class="section-title-group">
          <span class="title-icon-box" style="background:linear-gradient(135deg,#ff6700,#f5a623)"><el-icon :size="18" color="#fff"><TrendCharts /></el-icon></span>
          <h2 class="section-title">热卖排行</h2>
          <span class="title-line"></span>
        </div>
        <div class="section-desc">大家都在买</div>
      </div>
      <div class="hot-grid">
        <div v-for="(p, i) in hotProducts" :key="p.id" class="hot-card card">
          <div class="hot-rank" :class="'rank-' + (i+1)">{{ i + 1 }}</div>
          <router-link :to="`/product/${p.id}`">
            <div class="hot-image" :style="{ background: `linear-gradient(135deg, hsl(${p.id * 47 % 360}, 55%, 88%), hsl(${p.id * 47 % 360 + 30}, 55%, 80%))` }">
              <img v-if="p.mainImage" :src="p.mainImage" :alt="p.title" class="hot-img" @error="($event.target as HTMLImageElement).style.display='none'" />
              <svg v-if="!p.mainImage" class="hot-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="12" y="16" width="40" height="32" rx="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
                <path d="M16 40 L24 30 L30 36 L42 22 L48 30" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.4"/>
                <circle cx="22" cy="26" r="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
              </svg>
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
          <span class="title-icon-box" style="background:linear-gradient(135deg,#333,#666)"><el-icon :size="18" color="#fff"><Grid /></el-icon></span>
          <h2 class="section-title">全部商品</h2>
          <span class="title-line"></span>
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
        />
      </div>

      <div v-if="!loading && products.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
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
.home-page { padding-bottom: 20px; }

/* ========== 通知条 ========== */
.notice-bar {
  background: linear-gradient(90deg, #fff8f0, #fff5f5, #fff8f0);
  border-bottom: 1px solid #ffe8e8;
  overflow: hidden;
}
.notice-inner {
  display: flex;
  align-items: center;
  height: 28px;
  gap: 10px;
}
.notice-tag {
  flex-shrink: 0;
  background: linear-gradient(135deg, #e1251b, #ff6700);
  color: #fff;
  padding: 1px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: bold;
}
.notice-scroll { overflow: hidden; flex: 1; }
.notice-text {
  display: inline-block;
  white-space: nowrap;
  animation: scrollNotice 20s linear infinite;
  font-size: 12px;
  color: #666;
}
@keyframes scrollNotice {
  0% { transform: translateX(100%); }
  100% { transform: translateX(-100%); }
}

/* ========== 轮播区 ========== */
.hero-section {
  display: flex;
  gap: 0;
  margin: 8px auto;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  position: relative;
  z-index: 1;
}

.category-sidebar {
  width: 185px;
  background: linear-gradient(180deg, #2c2c2c, #1a1a1a);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding: 8px 12px;
  color: #fff;
  font-size: 13px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.08);
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.cat-item {
  padding: 7px 12px;
  color: #ccc;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
  border-left: 3px solid transparent;
  flex: 1;
}
.cat-item:hover, .cat-item.active {
  background: linear-gradient(90deg, rgba(225,37,27,0.15), transparent);
  color: #fff;
  padding-left: 16px;
  border-left-color: #e1251b;
}
.cat-name { flex: 1; }
.cat-arrow { opacity: 0; transition: all 0.3s; transform: translateX(-4px); }
.cat-item:hover .cat-arrow { opacity: 1; transform: translateX(0); }

.banner-wrapper { flex: 1; position: relative; }
.banner-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.25);
  backdrop-filter: blur(4px);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
}
.banner-wrapper:hover .banner-arrow { opacity: 1; }
.banner-arrow:hover { background: rgba(255,255,255,0.5); color: #333; transform: translateY(-50%) scale(1.1); }
.banner-arrow-left { left: 8px; }
.banner-arrow-right { right: 8px; }
.banner-slide {
  height: 435px;
  position: relative;
  display: flex;
  align-items: center;
  overflow: hidden;
}
.banner-content {
  position: relative;
  z-index: 2;
  padding: 24px;
  color: #fff;
}
.banner-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 16px;
  font-size: 11px;
  margin-bottom: 8px;
  background: rgba(255,255,255,0.25);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.3);
}
.banner-icon { font-size: 36px; margin-bottom: 8px; filter: drop-shadow(0 4px 8px rgba(0,0,0,0.2)); }
.banner-title { font-size: 28px; font-weight: bold; margin-bottom: 6px; text-shadow: 0 3px 12px rgba(0,0,0,0.25); }
.banner-sub { font-size: 14px; opacity: 0.9; margin-bottom: 14px; text-shadow: 0 1px 4px rgba(0,0,0,0.15); }
.banner-btn {
  padding: 7px 20px;
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.5);
  color: #fff;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s;
  backdrop-filter: blur(8px);
  text-decoration: none;
}
.banner-btn:hover { background: #fff; color: #333; transform: translateX(4px); }

.banner-dots {
  position: absolute;
  bottom: 10px;
  right: 14px;
  display: flex;
  gap: 6px;
  z-index: 3;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255,255,255,0.4);
  cursor: pointer;
  transition: all 0.3s;
}
.dot.active { background: #fff; width: 22px; border-radius: 4px; box-shadow: 0 1px 4px rgba(0,0,0,0.2); }

.side-cards {
  width: 165px;
  display: flex;
  flex-direction: column;
  gap: 0;
  flex-shrink: 0;
}
.side-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.side-card:last-child { border-bottom: none; }
.side-card:hover { filter: brightness(1.1); transform: scale(1.02); }
.side-card-red { background: linear-gradient(135deg, #e1251b, #ff6700); }
.side-card-green { background: linear-gradient(135deg, #2baa6e, #34d058); }
.side-card-blue { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.side-card-purple { background: linear-gradient(135deg, #a18cd1, #fbc2eb); }
.side-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.side-card-title { font-weight: bold; font-size: 12px; }
.side-card-desc { font-size: 10px; opacity: 0.8; }

/* ========== 秒杀倒计时 ========== */
.seckill-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  margin-top: 10px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-left: 4px solid #e1251b;
}
.seckill-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.flash-badge {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e1251b, #ff6700);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.seckill-title { font-size: 16px; color: var(--jd-red); font-weight: bold; }
.seckill-sub { font-size: 11px; color: #999; }
.countdown-box { text-align: center; }
.countdown-label { font-size: 11px; color: #999; display: block; margin-bottom: 2px; }
.countdown-nums { display: flex; align-items: center; gap: 3px; }
.num-block {
  background: #333;
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 15px;
  font-weight: bold;
  min-width: 34px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}
.num-sep { font-size: 15px; font-weight: bold; color: var(--jd-red); }
.seckill-more {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--jd-red);
  font-size: 13px;
  font-weight: bold;
  transition: transform 0.3s;
}
.seckill-more:hover { transform: translateX(4px); }

/* ========== Section ========== */
.section { margin-top: 16px; }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.section-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.title-icon-box {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.section-title { font-size: 18px; font-weight: bold; }
.title-line {
  width: 30px;
  height: 2px;
  border-radius: 2px;
  background: linear-gradient(90deg, #e1251b, transparent);
}
.section-desc { font-size: 12px; color: #999; }

.section-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.filter-tag {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
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
  gap: 6px;
  min-height: 150px;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.hot-card {
  position: relative;
  padding: 10px;
  cursor: pointer;
}
.hot-rank {
  position: absolute;
  top: 6px;
  left: 6px;
  width: 22px;
  height: 22px;
  border-radius: 4px;
  background: #ccc;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  z-index: 1;
}
.rank-1 { background: linear-gradient(135deg, #e1251b, #ff4e3a); }
.rank-2 { background: linear-gradient(135deg, #ff6700, #ff9500); }
.rank-3 { background: linear-gradient(135deg, #f5a623, #f7c948); }

.hot-image {
  width: 100%;
  padding-top: 80%;
  border-radius: 6px;
  position: relative;
  margin-bottom: 6px;
  overflow: hidden;
}
.hot-img {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  object-fit: cover;
  z-index: 2;
  transition: transform 0.4s cubic-bezier(0.23, 1, 0.32, 1);
}
.hot-card:hover .hot-img { transform: scale(1.06); }
.hot-svg { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 40px; height: 40px; color: rgba(255,255,255,0.6); }
.hot-name { font-size: 12px; margin-bottom: 2px; }

.empty-state { text-align: center; padding: 60px 0; color: #ccc; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.pagination { display: flex; justify-content: center; padding: 20px 0 12px; }

@media (max-width: 1200px) { .product-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 900px) { .product-grid, .hot-grid { grid-template-columns: repeat(3, 1fr); } }
</style>
