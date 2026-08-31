<script setup lang="ts">
/**
 * 秒杀专场页 - 视觉增强版 v2
 * 华丽Hero + 金币红包粒子 + 倒计时外发光 + 光束扫描 + 火焰过渡
 * 价格滚动动画 + 已抢放大 + 限时秒杀2x2标签
 */
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const seckillItems = ref<any[]>([])
const loading = ref(true)
const activityId = 1
const now = ref(Date.now())
let timer: ReturnType<typeof setInterval>

// ========== 价格动画 ==========
const animatedPrices = ref<Record<number, number>>({})
const priceStarted = ref<Record<number, boolean>>({})
const priceRefs = ref<HTMLElement[]>([])

function animatePrice(itemId: number, from: number, to: number) {
  const duration = 1200
  const start = performance.now()
  const step = (ts: number) => {
    const progress = Math.min((ts - start) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    animatedPrices.value[itemId] = from + (to - from) * eased
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

function setupPriceObserver() {
  nextTick(() => {
    priceRefs.value.forEach((el, idx) => {
      if (!el) return
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const item = seckillItems.value[idx]
            if (item && !priceStarted.value[item.seckillItemId]) {
              priceStarted.value[item.seckillItemId] = true
              animatePrice(item.seckillItemId, item.normalPrice, item.seckillPrice)
            }
            observer.unobserve(entry.target)
          }
        })
      }, { threshold: 0.3 })
      observer.observe(el)
    })
  })
}

// ========== 活动状态 ==========
const activityStatus = computed(() => {
  if (!seckillItems.value.length) return 'unknown'
  const item = seckillItems.value[0]
  if (item.activityStatus === 1) return 'active'
  if (item.activityStatus === 0) return 'upcoming'
  return 'ended'
})
const statusLabel = computed(() => {
  const map: Record<string, string> = { active: '🔴 进行中', upcoming: '⏳ 未开始', ended: '已结束', unknown: '' }
  return map[activityStatus.value] || ''
})
const statusClass = computed(() => 'status-' + activityStatus.value)

// ========== 倒计时 ==========
function getTimeLeft(endTime: string) {
  const end = new Date(endTime).getTime()
  const diff = end - now.value
  if (diff <= 0) return { days: 0, hours: 0, minutes: 0, seconds: 0 }
  return {
    days: Math.floor(diff / 86400000),
    hours: Math.floor((diff % 86400000) / 3600000),
    minutes: Math.floor((diff % 3600000) / 60000),
    seconds: Math.floor((diff % 60000) / 1000)
  }
}

const countdownDigits = computed(() => {
  if (!seckillItems.value.length) return { h1: '0', h2: '0', m1: '0', m2: '0', s1: '0', s2: '0' }
  const t = getTimeLeft(seckillItems.value[0]?.endTime)
  const h = String(t.hours).padStart(2, '0')
  const m = String(t.minutes).padStart(2, '0')
  const s = String(t.seconds).padStart(2, '0')
  return { h1: h[0], h2: h[1], m1: m[0], m2: m[1], s1: s[0], s2: s[1] }
})

// ========== 数据 ==========
function getStockPercent(item: any) {
  const total = 20
  return Math.max(2, Math.round((1 - item.seckillStock / total) * 100))
}

function getStockColor(percent: number) {
  if (percent >= 80) return '#e1251b'
  if (percent >= 50) return '#ff6700'
  if (percent >= 30) return '#f5a623'
  return '#52c41a'
}

function getViewersCount(skuId: number) { return (skuId * 137 % 500) + 128 }
function getRecentOrders(skuId: number) { return (skuId * 89 % 200) + 56 }

async function fetchSeckill() {
  loading.value = true
  try {
    const res: any = await request.get(`/seckill/list?activityId=${activityId}`)
    seckillItems.value = res.data
    // 初始化价格为原价
    res.data.forEach((item: any) => {
      animatedPrices.value[item.seckillItemId] = item.normalPrice
    })
    setupPriceObserver()
  } finally {
    loading.value = false
  }
}

async function handleBuy(item: any) {
  if (!localStorage.getItem('token')) return router.push('/login')
  if (item.seckillStock <= 0) return ElMessage.warning('已抢完')
  if (item.activityStatus !== 1) return ElMessage.warning('活动未开始')
  try {
    const res: any = await request.post('/seckill/buy', { seckillItemId: item.seckillItemId })
    ElMessage.success('抢购成功！订单号：' + res.data.orderNo)
    router.push(`/order/${res.data.orderNo}`)
  } catch { /* interceptor handles */ }
}

onMounted(() => {
  fetchSeckill()
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})
onUnmounted(() => { clearInterval(timer) })
</script>

<template>
  <div class="seckill-page">
    <!-- ========== Hero 头部 ========== -->
    <div class="seckill-hero">
      <!-- 光束扫描 -->
      <div class="light-sweep"></div>

      <!-- 金币红包粒子 -->
      <div class="hero-particles">
        <span v-for="i in 20" :key="i" class="particle" :class="'pt-' + ((i % 4) + 1)" :style="{
          left: (i * 5) + '%',
          animationDelay: (i * 0.3) + 's',
          animationDuration: (2.5 + (i % 4) * 0.6) + 's'
        }"></span>
      </div>

      <!-- 闪电粒子 -->
      <div class="hero-particles">
        <span v-for="i in 8" :key="'l' + i" class="particle lightning-particle" :style="{
          left: (i * 12 + 3) + '%',
          animationDelay: (i * 0.7 + 0.2) + 's',
          animationDuration: (3 + (i % 3)) + 's'
        }">⚡</span>
      </div>

      <div class="hero-content container">
        <div class="hero-left">
          <div class="hero-badge-row">
            <span class="hero-badge" :class="statusClass">{{ statusLabel }}</span>
          </div>
          <h1 class="hero-title">
            <span class="hero-icon-wrap">
              <el-icon :size="32"><Lightning /></el-icon>
            </span>
            <span class="hero-title-text">限时秒杀</span>
            <span class="hero-title-sub">专场</span>
          </h1>
          <p class="hero-sub">每日精选 · 限量抢购 · 超值优惠</p>
          <div class="hero-tags">
            <span class="tag"><el-icon><Timer /></el-icon> 限时</span>
            <span class="tag"><el-icon><Discount /></el-icon> 特价</span>
            <span class="tag"><el-icon><Warning /></el-icon> 限购</span>
          </div>
        </div>
        <div class="hero-right">
          <div class="countdown-card">
            <div class="cd-label">距结束</div>
            <div class="cd-flip-row">
              <div class="cd-flip"><span class="cd-num-glow">{{ countdownDigits.h1 }}</span></div>
              <div class="cd-flip"><span class="cd-num-glow">{{ countdownDigits.h2 }}</span></div>
              <span class="cd-colon"><span class="colon-icon">⚡</span></span>
              <div class="cd-flip"><span class="cd-num-glow">{{ countdownDigits.m1 }}</span></div>
              <div class="cd-flip"><span class="cd-num-glow">{{ countdownDigits.m2 }}</span></div>
              <span class="cd-colon"><span class="colon-icon">⚡</span></span>
              <div class="cd-flip"><span class="cd-num-glow">{{ countdownDigits.s1 }}</span></div>
              <div class="cd-flip"><span class="cd-num-glow">{{ countdownDigits.s2 }}</span></div>
            </div>
            <div class="cd-unit-row"><span>时</span><span>分</span><span>秒</span></div>
          </div>
        </div>
      </div>

      <!-- 底部火焰过渡 -->
      <div class="hero-fire"></div>
    </div>

    <!-- ========== 商品列表 ========== -->
    <div class="seckill-content container" v-loading="loading">
      <div class="section-header">
        <div class="section-title-group">
          <span class="title-icon-box"><el-icon :size="18" color="#fff"><Lightning /></el-icon></span>
          <h2 class="section-title">爆款秒杀</h2>
          <span class="title-line"></span>
        </div>
        <div class="section-desc"><el-icon><InfoFilled /></el-icon> 每人限购，抢完即止</div>
      </div>

      <div class="seckill-grid">
        <div v-for="(item, index) in seckillItems" :key="item.seckillItemId" class="seckill-card">
          <!-- 排名角标 -->
          <div class="rank-badge" :class="'rank-' + (index + 1)">
            <el-icon v-if="index === 0"><Trophy /></el-icon>
            <el-icon v-else-if="index === 1"><Medal /></el-icon>
            <el-icon v-else><Star /></el-icon>
            {{ index < 3 ? 'TOP' + (index + 1) : '秒杀' }}
          </div>

          <!-- 疯抢标签 -->
          <div class="hot-tag" v-if="getStockPercent(item) >= 60">
            <span class="hot-dot"></span> 正在疯抢
          </div>

          <!-- 图片区 -->
          <div class="card-image">
            <div class="placeholder-img" :style="{ background: `linear-gradient(135deg, hsl(${item.skuId * 47 % 360}, 55%, 88%), hsl(${item.skuId * 47 % 360 + 30}, 55%, 80%))` }">
              <img v-if="item.productImage" :src="item.productImage" :alt="item.productName" class="card-img" @error="($event.target as HTMLImageElement).style.display='none'" />
              <svg v-if="!item.productImage" class="placeholder-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="12" y="16" width="40" height="32" rx="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
                <path d="M16 40 L24 30 L30 36 L42 22 L48 30" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.4"/>
                <circle cx="22" cy="26" r="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
              </svg>
            </div>
            <!-- 倒计时叠加 -->
            <div class="img-countdown" v-if="item.activityStatus === 1">
              <el-icon :size="12"><Clock /></el-icon>
              {{ countdownDigits.h1 }}{{ countdownDigits.h2 }}:{{ countdownDigits.m1 }}{{ countdownDigits.m2 }}:{{ countdownDigits.s1 }}{{ countdownDigits.s2 }}
            </div>
            <!-- 已抢百分比大圆环 -->
            <div class="stock-ring-wrap">
              <svg class="stock-ring" viewBox="0 0 64 64">
                <circle cx="32" cy="32" r="28" fill="none" stroke="rgba(255,255,255,0.2)" stroke-width="5" />
                <circle cx="32" cy="32" r="28" fill="none" :stroke="getStockColor(getStockPercent(item))" stroke-width="5"
                  stroke-linecap="round" :stroke-dasharray="175.9" :stroke-dashoffset="175.9 * (1 - getStockPercent(item) / 100)"
                  transform="rotate(-90 32 32)" class="ring-progress" />
              </svg>
              <div class="ring-text-wrap">
                <span class="ring-percent">{{ getStockPercent(item) }}%</span>
                <span class="ring-label">已抢</span>
              </div>
            </div>
          </div>

          <!-- 内容区 -->
          <div class="card-body" :ref="(el: any) => { if (el) priceRefs[index] = el }">
            <div class="product-name ellipsis-2">{{ item.productName }}</div>
            <div class="product-spec"><el-icon><Tag /></el-icon> {{ item.specs }}</div>

            <!-- 价格 + 限时秒杀标签 -->
            <div class="price-section">
              <div class="price-row">
                <span class="seckill-price">
                  <span class="price-symbol">¥</span>
                  <span class="price-num">{{ (animatedPrices[item.seckillItemId] || item.normalPrice).toFixed(0) }}</span>
                </span>
                <span class="normal-price">¥{{ item.normalPrice }}</span>
                <div class="flash-badge-2x2">
                  <span>限时</span><span>秒杀</span>
                </div>
              </div>
              <div class="discount-tag">
                <el-icon><Discount /></el-icon>
                已省 ¥{{ (item.normalPrice - item.seckillPrice).toFixed(0) }}
              </div>
            </div>

            <!-- 限购 + 人气 -->
            <div class="meta-row">
              <span class="limit-info"><el-icon><Warning /></el-icon> 限购{{ item.purchaseLimit }}件 · 剩{{ item.seckillStock }}件</span>
              <span class="viewer-info"><el-icon><View /></el-icon> {{ getViewersCount(item.skuId) }}人围观</span>
            </div>

            <!-- 抢购按钮 -->
            <button class="buy-btn" :class="{
              'btn-active': item.seckillStock > 0 && item.activityStatus === 1,
              'btn-disabled': item.seckillStock <= 0 || item.activityStatus !== 1
            }" @click="handleBuy(item)">
              <span class="btn-text" v-if="item.seckillStock > 0 && item.activityStatus === 1">
                <el-icon><Lightning /></el-icon> 立即抢购
              </span>
              <span class="btn-text" v-else-if="item.seckillStock <= 0">
                <el-icon><CircleClose /></el-icon> 已抢完
              </span>
              <span class="btn-text" v-else>未开始</span>
            </button>

            <div class="recent-orders" v-if="item.activityStatus === 1">
              <span class="recent-dot"></span>
              刚刚有{{ getRecentOrders(item.skuId) }}人抢购成功
            </div>
          </div>
        </div>
      </div>

      <div v-if="!loading && seckillItems.length === 0" class="empty-state">
        <div class="empty-icon-wrap"><span class="empty-icon">⚡</span></div>
        <p class="empty-text">暂无秒杀活动</p>
        <p class="empty-sub">敬请期待下一场秒杀</p>
        <router-link to="/" class="empty-btn">去首页逛逛</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ==================== HERO ==================== */
.seckill-page { min-height: 100vh; background: var(--jd-bg, #f5f5f5); }

.seckill-hero {
  position: relative;
  background: linear-gradient(135deg, #b71c1c, #e1251b 30%, #ff6700 100%);
  overflow: hidden;
  padding: 0;
}
.seckill-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url('/images/seckill-hero.png') center/cover no-repeat;
  opacity: 0.45;
  pointer-events: none;
}

/* 光束扫描 */
.light-sweep {
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, transparent 40%, rgba(255,215,0,0.15) 45%, rgba(255,215,0,0.25) 50%, rgba(255,215,0,0.15) 55%, transparent 60%);
  animation: sweep 4s ease-in-out infinite;
  pointer-events: none;
  z-index: 1;
}
@keyframes sweep {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(200%); }
}

/* 粒子 */
.hero-particles { position: absolute; inset: 0; overflow: hidden; pointer-events: none; z-index: 0; }
.particle {
  position: absolute;
  bottom: -20px;
  width: auto;
  font-size: 16px;
  animation: particleUp linear infinite;
}
.pt-1 { font-size: 14px; opacity: 0.7; }
.pt-2 { font-size: 18px; opacity: 0.8; }
.pt-3 { font-size: 12px; opacity: 0.6; }
.pt-4 { font-size: 20px; opacity: 0.9; }
.particle.pt-1::after { content: '🪙'; }
.particle.pt-2::after { content: '🧧'; }
.particle.pt-3::after { content: '🎁'; }
.particle.pt-4::after { content: '💰'; }

.lightning-particle {
  font-size: 22px;
  filter: drop-shadow(0 0 6px rgba(255,215,0,0.8));
  animation: particleUp linear infinite, lightningFlash 0.3s ease-in-out infinite;
}
@keyframes particleUp {
  0% { transform: translateY(0) rotate(0deg); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 0.8; }
  100% { transform: translateY(-300px) rotate(20deg); opacity: 0; }
}
@keyframes lightningFlash {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* Hero 内容 */
.hero-content {
  position: relative; z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 48px 16px 60px;
}
.hero-left { color: #fff; flex: 1; }
.hero-badge-row { margin-bottom: 12px; }
.hero-badge {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  animation: badgePulse 2s ease-in-out infinite;
}
.status-active { background: rgba(255,255,255,0.25); color: #fff; border: 1px solid rgba(255,255,255,0.4); }
.status-upcoming { background: rgba(255,193,7,0.3); color: #ffd54f; border: 1px solid rgba(255,193,7,0.5); }
.status-ended { background: rgba(255,255,255,0.1); color: rgba(255,255,255,0.6); border: 1px solid rgba(255,255,255,0.2); animation: none; }
@keyframes badgePulse { 0%,100%{opacity:1;} 50%{opacity:0.7;} }

.hero-title {
  font-size: 36px;
  font-weight: 900;
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.hero-title-text {
  text-shadow: 0 0 20px rgba(255,215,0,0.6), 0 0 40px rgba(255,107,0,0.4), 0 2px 8px rgba(0,0,0,0.3);
  animation: titleGlow 2s ease-in-out infinite;
}
.hero-title-sub {
  font-size: 20px;
  opacity: 0.85;
  font-weight: 700;
}
@keyframes titleGlow {
  0%, 100% { text-shadow: 0 0 20px rgba(255,215,0,0.6), 0 0 40px rgba(255,107,0,0.4), 0 2px 8px rgba(0,0,0,0.3); }
  50% { text-shadow: 0 0 30px rgba(255,215,0,0.9), 0 0 60px rgba(255,107,0,0.6), 0 2px 8px rgba(0,0,0,0.3); }
}

.hero-icon-wrap {
  display: flex; align-items: center; justify-content: center;
  width: 52px; height: 52px;
  background: rgba(255,255,255,0.15);
  border-radius: 14px;
  backdrop-filter: blur(4px);
  animation: iconFlash 1.5s ease-in-out infinite;
}
@keyframes iconFlash { 0%,100%{transform:scale(1);} 50%{transform:scale(1.12);} }

.hero-sub { font-size: 15px; opacity: 0.85; margin-top: 8px; }
.hero-tags { display: flex; gap: 8px; margin-top: 16px; }
.hero-tags .tag {
  display: inline-flex; align-items: center; gap: 4px;
  background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.25);
  color: #fff; padding: 5px 14px; border-radius: 20px;
  font-size: 12px; font-weight: 500; backdrop-filter: blur(4px);
}

/* 倒计时 - 外发光 */
.countdown-card {
  background: rgba(0,0,0,0.4);
  border-radius: 16px;
  padding: 20px 24px;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,100,0,0.3);
  text-align: center;
  color: #fff;
  min-width: 260px;
  box-shadow: 0 0 30px rgba(255,60,0,0.3), 0 0 60px rgba(255,60,0,0.15);
}
.cd-label { font-size: 13px; opacity: 0.8; margin-bottom: 12px; letter-spacing: 2px; }
.cd-flip-row { display: flex; align-items: center; justify-content: center; gap: 5px; }
.cd-flip {
  width: 40px; height: 52px;
  background: #1a1a2e;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.4), inset 0 1px 0 rgba(255,255,255,0.1);
}
.cd-num-glow {
  font-size: 26px;
  font-weight: 900;
  color: #fff;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 0 10px rgba(255,80,0,0.8), 0 0 20px rgba(255,80,0,0.5);
  animation: numPulse 1s ease-in-out infinite;
}
@keyframes numPulse {
  0%, 100% { text-shadow: 0 0 10px rgba(255,80,0,0.8), 0 0 20px rgba(255,80,0,0.5); transform: scale(1); }
  50% { text-shadow: 0 0 16px rgba(255,80,0,1), 0 0 32px rgba(255,80,0,0.7); transform: scale(1.05); }
}

.cd-colon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 2px;
}
.colon-icon {
  font-size: 16px;
  color: #ffd700;
  animation: colonBlink 1s step-end infinite;
  filter: drop-shadow(0 0 4px rgba(255,215,0,0.8));
}
@keyframes colonBlink { 0%,100%{opacity:1;transform:scale(1);} 50%{opacity:0.3;transform:scale(0.8);} }

.cd-unit-row { display: flex; justify-content: center; gap: 52px; margin-top: 6px; font-size: 11px; opacity: 0.5; }

/* 底部火焰过渡 */
.hero-fire {
  position: absolute;
  bottom: -2px;
  left: 0; right: 0;
  height: 40px;
  background: linear-gradient(180deg, transparent 0%, var(--jd-bg, #f5f5f5) 100%);
  z-index: 3;
}
.hero-fire::before {
  content: '';
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 30px;
  background: radial-gradient(ellipse at center bottom, rgba(255,107,0,0.25) 0%, transparent 70%);
  filter: blur(8px);
}

/* ==================== 内容区 ==================== */
.seckill-content { padding: 32px 16px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.section-title-group { display: flex; align-items: center; gap: 12px; }
.title-icon-box { width: 32px; height: 32px; border-radius: 8px; background: linear-gradient(135deg, #e1251b, #ff6700); display: flex; align-items: center; justify-content: center; }
.section-title { font-size: 20px; font-weight: 700; }
.title-line { width: 32px; height: 3px; border-radius: 2px; background: linear-gradient(90deg, #e1251b, transparent); }
.section-desc { font-size: 13px; color: #999; display: flex; align-items: center; gap: 4px; }

/* ==================== 商品网格 ==================== */
.seckill-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.seckill-card {
  position: relative;
  border-radius: 12px;
  overflow: visible;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.seckill-card:hover { transform: translateY(-6px); box-shadow: 0 12px 32px rgba(225,37,27,0.15), 0 0 0 1px rgba(225,37,27,0.08); }

/* 排名角标 */
.rank-badge {
  position: absolute; top: -1px; right: 16px;
  padding: 5px 14px; border-radius: 0 0 8px 8px;
  color: #fff; font-size: 12px; font-weight: 600;
  display: flex; align-items: center; gap: 4px; z-index: 2;
}
.rank-1 { background: linear-gradient(135deg, #e1251b, #ff4757); }
.rank-2 { background: linear-gradient(135deg, #ff6700, #ffa502); }
.rank-3 { background: linear-gradient(135deg, #f5a623, #f0c040); }
.rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) { background: linear-gradient(135deg, #999, #bbb); }

/* 疯抢标签 */
.hot-tag {
  position: absolute; top: 12px; left: 12px; z-index: 2;
  background: linear-gradient(135deg, #e1251b, #ff4757);
  color: #fff; font-size: 11px; font-weight: 600;
  padding: 3px 10px; border-radius: 12px;
  display: flex; align-items: center; gap: 5px;
  animation: hotPulse 1.5s ease-in-out infinite;
}
.hot-dot { width: 6px; height: 6px; background: #fff; border-radius: 50%; animation: dotBlink 1s ease-in-out infinite; }
@keyframes hotPulse { 0%,100%{transform:scale(1);} 50%{transform:scale(1.05);} }
@keyframes dotBlink { 0%,100%{opacity:1;} 50%{opacity:0.3;} }

/* 图片区 */
.card-image { position: relative; padding-top: 100%; border-radius: 12px 12px 0 0; overflow: hidden; }
.placeholder-img {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.4s;
}
.seckill-card:hover .placeholder-img { transform: scale(1.06); }
.placeholder-svg { width: 56px; height: 56px; color: rgba(255,255,255,0.6); }
.card-img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }

.img-countdown {
  position: absolute; top: 10px; right: 10px;
  background: rgba(0,0,0,0.65); backdrop-filter: blur(4px);
  color: #fff; font-size: 11px; font-weight: 600;
  padding: 3px 10px; border-radius: 12px;
  display: flex; align-items: center; gap: 4px;
  z-index: 1; font-variant-numeric: tabular-nums;
}

/* 已抢大圆环 */
.stock-ring-wrap {
  position: absolute; bottom: 10px; left: 10px;
  width: 64px; height: 64px; z-index: 1;
}
.stock-ring { width: 64px; height: 64px; }
.ring-progress { transition: stroke-dashoffset 0.8s ease; }
.ring-text-wrap {
  position: absolute; inset: 0;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
}
.ring-percent { font-size: 13px; font-weight: 800; color: #fff; text-shadow: 0 1px 3px rgba(0,0,0,0.5); line-height: 1; }
.ring-label { font-size: 8px; color: rgba(255,255,255,0.8); text-shadow: 0 1px 2px rgba(0,0,0,0.5); }

/* 内容区 */
.card-body { padding: 16px; }
.product-name { font-size: 14px; font-weight: 500; margin-bottom: 4px; line-height: 1.4; height: 40px; }
.product-spec { font-size: 12px; color: #999; margin-bottom: 10px; display: flex; align-items: center; gap: 4px; }

/* 价格 + 限时秒杀标签 */
.price-section { margin-bottom: 8px; }
.price-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.seckill-price { color: #e1251b; display: inline-flex; align-items: baseline; }
.price-symbol { font-size: 14px; }
.price-num { font-size: 26px; font-weight: 800; }
.normal-price { font-size: 13px; color: #bbb; text-decoration: line-through; }

/* 限时秒杀 2x2 标签 */
.flash-badge-2x2 {
  display: inline-grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  background: linear-gradient(135deg, #e1251b, #ff4757);
  color: #fff;
  font-size: 11px;
  font-weight: 900;
  line-height: 1.1;
  padding: 3px 5px;
  border-radius: 4px;
  text-align: center;
  letter-spacing: 2px;
  box-shadow: 0 2px 8px rgba(225,37,27,0.4);
  animation: flashBadgePulse 1.5s ease-in-out infinite;
}
@keyframes flashBadgePulse {
  0%, 100% { box-shadow: 0 2px 8px rgba(225,37,27,0.4); transform: scale(1); }
  50% { box-shadow: 0 2px 14px rgba(225,37,27,0.7); transform: scale(1.05); }
}

.discount-tag {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 11px; color: #ff6700;
  background: #fff0f0; padding: 3px 10px;
  border-radius: 999px; margin-top: 4px;
  border: 1px solid #ffe0d0;
}

.meta-row {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 11px; color: #999; margin-bottom: 12px;
}
.limit-info, .viewer-info { display: flex; align-items: center; gap: 3px; }

/* 抢购按钮 */
.buy-btn {
  width: 100%; height: 42px;
  border: none; border-radius: 8px;
  font-size: 15px; font-weight: 700;
  letter-spacing: 1px; cursor: pointer;
  position: relative; overflow: hidden;
  transition: transform 0.2s, box-shadow 0.3s;
}
.btn-active {
  background: linear-gradient(135deg, #e1251b, #ff4757);
  color: #fff; box-shadow: 0 4px 16px rgba(225,37,27,0.3);
  animation: btnPulse 2s ease-in-out infinite;
}
.btn-active:hover { transform: scale(1.02); box-shadow: 0 6px 24px rgba(225,37,27,0.4); }
.btn-active::after {
  content: ''; position: absolute; inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transform: translateX(-100%);
  animation: btnShine 3s ease-in-out infinite;
}
.btn-disabled { background: #e0e0e0; color: #999; cursor: not-allowed; }
.btn-text { position: relative; z-index: 1; display: inline-flex; align-items: center; gap: 6px; }
@keyframes btnPulse { 0%,100%{box-shadow:0 4px 16px rgba(225,37,27,0.3);} 50%{box-shadow:0 4px 24px rgba(225,37,27,0.5);} }
@keyframes btnShine { 0%,70%,100%{transform:translateX(-100%);} 80%{transform:translateX(100%);} }

.recent-orders { margin-top: 10px; font-size: 11px; color: #999; display: flex; align-items: center; gap: 5px; }
.recent-dot { width: 5px; height: 5px; background: #52c41a; border-radius: 50%; flex-shrink: 0; }

/* ==================== 空状态 ==================== */
.empty-state { text-align: center; padding: 80px 0; }
.empty-icon-wrap { width: 80px; height: 80px; margin: 0 auto 20px; background: linear-gradient(135deg, #fff0f0, #ffe8d6); border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.empty-icon { font-size: 40px; }
.empty-text { font-size: 18px; color: #666; font-weight: 600; margin-bottom: 8px; }
.empty-sub { font-size: 14px; color: #999; margin-bottom: 24px; }
.empty-btn { display: inline-block; padding: 10px 28px; background: linear-gradient(135deg, #e1251b, #ff6700); color: #fff; border-radius: 8px; font-size: 14px; font-weight: 500; text-decoration: none; transition: transform 0.2s, box-shadow 0.2s; }
.empty-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(225,37,27,0.3); color: #fff; }

/* ==================== 响应式 ==================== */
@media (max-width: 1200px) { .seckill-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) {
  .seckill-grid { grid-template-columns: repeat(2, 1fr); }
  .hero-content { flex-direction: column; text-align: center; gap: 24px; }
  .hero-tags { justify-content: center; }
  .hero-badge-row { text-align: center; }
}
</style>
