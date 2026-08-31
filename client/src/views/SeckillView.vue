<script setup lang="ts">
/**
 * 秒杀专场页 - 视觉增强版
 * 翻牌倒计时 + SVG圆环库存 + 脉冲按钮 + 光效hover
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const seckillItems = ref<any[]>([])
const loading = ref(true)
const activityId = 1
const now = ref(Date.now())
let timer: ReturnType<typeof setInterval>

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

const currentCountdown = computed(() => {
  if (!seckillItems.value.length) return { hours: '00', minutes: '00', seconds: '00' }
  const t = getTimeLeft(seckillItems.value[0]?.endTime)
  return {
    hours: String(t.hours).padStart(2, '0'),
    minutes: String(t.minutes).padStart(2, '0'),
    seconds: String(t.seconds).padStart(2, '0')
  }
})

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

function getViewersCount(skuId: number) {
  return (skuId * 137 % 500) + 128
}

function getRecentOrders(skuId: number) {
  return (skuId * 89 % 200) + 56
}

async function fetchSeckill() {
  loading.value = true
  try {
    const res: any = await request.get(`/seckill/list?activityId=${activityId}`)
    seckillItems.value = res.data
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
      <div class="hero-glow"></div>
      <div class="hero-particles">
        <span v-for="i in 12" :key="i" class="particle" :style="{
          left: (i * 8.3) + '%',
          animationDelay: (i * 0.4) + 's',
          animationDuration: (2 + (i % 3) * 0.8) + 's'
        }"></span>
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
            限时秒杀专场
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
              <div class="cd-flip">
                <div class="cd-flip-inner">
                  <span class="cd-top">{{ currentCountdown.hours }}</span>
                  <span class="cd-bottom">{{ currentCountdown.hours }}</span>
                </div>
              </div>
              <span class="cd-colon">:</span>
              <div class="cd-flip">
                <div class="cd-flip-inner">
                  <span class="cd-top">{{ currentCountdown.minutes }}</span>
                  <span class="cd-bottom">{{ currentCountdown.minutes }}</span>
                </div>
              </div>
              <span class="cd-colon">:</span>
              <div class="cd-flip">
                <div class="cd-flip-inner">
                  <span class="cd-top">{{ currentCountdown.seconds }}</span>
                  <span class="cd-bottom">{{ currentCountdown.seconds }}</span>
                </div>
              </div>
            </div>
            <div class="cd-unit-row">
              <span>时</span><span>分</span><span>秒</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 商品列表 ========== -->
    <div class="seckill-content container" v-loading="loading">
      <div class="section-header">
        <div class="section-title-group">
          <span class="title-icon-box"><el-icon :size="18" color="#fff"><Lightning /></el-icon></span>
          <h2 class="section-title">爆款秒杀</h2>
          <span class="title-line"></span>
        </div>
        <div class="section-desc">
          <el-icon><InfoFilled /></el-icon> 每人限购，抢完即止
        </div>
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
            <!-- 倒计时叠加层 -->
            <div class="img-countdown" v-if="item.activityStatus === 1">
              <el-icon :size="12"><Clock /></el-icon>
              {{ currentCountdown.hours }}:{{ currentCountdown.minutes }}:{{ currentCountdown.seconds }}
            </div>
            <!-- 底部圆环进度 -->
            <div class="stock-ring-wrap">
              <svg class="stock-ring" viewBox="0 0 48 48">
                <circle cx="24" cy="24" r="20" fill="none" stroke="rgba(255,255,255,0.25)" stroke-width="4" />
                <circle cx="24" cy="24" r="20" fill="none" :stroke="getStockColor(getStockPercent(item))" stroke-width="4"
                  stroke-linecap="round"
                  :stroke-dasharray="125.6"
                  :stroke-dashoffset="125.6 * (1 - getStockPercent(item) / 100)"
                  transform="rotate(-90 24 24)"
                  class="ring-progress"
                />
              </svg>
              <span class="ring-text">已抢{{ getStockPercent(item) }}%</span>
            </div>
          </div>

          <!-- 内容区 -->
          <div class="card-body">
            <div class="product-name ellipsis-2">{{ item.productName }}</div>
            <div class="product-spec">
              <el-icon><Tag /></el-icon> {{ item.specs }}
            </div>

            <!-- 价格 -->
            <div class="price-section">
              <div class="price-row">
                <span class="seckill-price">
                  <span class="price-symbol">¥</span>
                  <span class="price-num">{{ item.seckillPrice }}</span>
                </span>
                <span class="normal-price">¥{{ item.normalPrice }}</span>
              </div>
              <div class="discount-tag">
                <el-icon><Discount /></el-icon>
                省 ¥{{ (item.normalPrice - item.seckillPrice).toFixed(0) }}
              </div>
            </div>

            <!-- 限购 + 人气 -->
            <div class="meta-row">
              <span class="limit-info">
                <el-icon><Warning /></el-icon>
                限购{{ item.purchaseLimit }}件 · 剩{{ item.seckillStock }}件
              </span>
              <span class="viewer-info">
                <el-icon><View /></el-icon>
                {{ getViewersCount(item.skuId) }}人围观
              </span>
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
              <span class="btn-text" v-else>
                未开始
              </span>
            </button>

            <!-- 近期抢购 -->
            <div class="recent-orders" v-if="item.activityStatus === 1">
              <span class="recent-dot"></span>
              刚刚有{{ getRecentOrders(item.skuId) }}人抢购成功
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && seckillItems.length === 0" class="empty-state">
        <div class="empty-icon-wrap">
          <span class="empty-icon">⚡</span>
        </div>
        <p class="empty-text">暂无秒杀活动</p>
        <p class="empty-sub">敬请期待下一场秒杀</p>
        <router-link to="/" class="empty-btn">去首页逛逛</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== Hero 头部 ========== */
.seckill-page { min-height: 100vh; background: var(--jd-bg, #f5f5f5); }

.seckill-hero {
  position: relative;
  background: linear-gradient(135deg, #b71c1c, #e1251b 30%, #ff6700 100%);
  overflow: hidden;
  padding: 0;
}
.hero-glow {
  position: absolute;
  top: -50%; left: -20%;
  width: 60%; height: 200%;
  background: radial-gradient(ellipse, rgba(255,255,255,0.15) 0%, transparent 70%);
  animation: glowMove 6s ease-in-out infinite;
  pointer-events: none;
}
@keyframes glowMove {
  0%, 100% { transform: translateX(0) translateY(0); }
  50% { transform: translateX(30%) translateY(-10%); }
}

/* 粒子 */
.hero-particles {
  position: absolute; inset: 0; overflow: hidden; pointer-events: none;
}
.particle {
  position: absolute;
  bottom: -10px;
  width: 4px; height: 4px;
  background: rgba(255,255,255,0.5);
  border-radius: 50%;
  animation: particleUp linear infinite;
}
@keyframes particleUp {
  0% { transform: translateY(0) scale(1); opacity: 0.7; }
  100% { transform: translateY(-200px) scale(0); opacity: 0; }
}

.hero-content {
  position: relative; z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 48px 16px 48px;
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
@keyframes badgePulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.hero-title {
  font-size: 32px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 12px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.hero-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px; height: 48px;
  background: rgba(255,255,255,0.2);
  border-radius: 14px;
  backdrop-filter: blur(4px);
  animation: iconFlash 1.5s ease-in-out infinite;
}
@keyframes iconFlash {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.hero-sub {
  font-size: 15px;
  opacity: 0.85;
  margin-top: 8px;
}
.hero-tags {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}
.hero-tags .tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.25);
  color: #fff;
  padding: 5px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  backdrop-filter: blur(4px);
}

/* 翻牌倒计时 */
.countdown-card {
  background: rgba(0,0,0,0.35);
  border-radius: 16px;
  padding: 20px 28px;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.15);
  text-align: center;
  color: #fff;
  min-width: 220px;
}
.cd-label {
  font-size: 13px;
  opacity: 0.8;
  margin-bottom: 12px;
  letter-spacing: 2px;
}
.cd-flip-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.cd-flip {
  width: 52px; height: 62px;
  background: #1a1a2e;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 4px 12px rgba(0,0,0,0.4), inset 0 1px 0 rgba(255,255,255,0.1);
}
.cd-flip-inner {
  display: flex;
  flex-direction: column;
  width: 100%; height: 100%;
}
.cd-top, .cd-bottom {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  color: #fff;
}
.cd-top {
  border-bottom: 1px solid rgba(255,255,255,0.08);
  background: linear-gradient(180deg, rgba(255,255,255,0.05) 0%, transparent 100%);
}
.cd-bottom {
  background: linear-gradient(180deg, transparent 0%, rgba(0,0,0,0.15) 100%);
}
.cd-colon {
  font-size: 28px;
  font-weight: 800;
  opacity: 0.6;
  animation: colonBlink 1s step-end infinite;
  margin: 0 2px;
}
@keyframes colonBlink {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 0.1; }
}
.cd-unit-row {
  display: flex;
  justify-content: center;
  gap: 62px;
  margin-top: 6px;
  font-size: 11px;
  opacity: 0.5;
}

/* ========== 内容区 ========== */
.seckill-content { padding: 32px 16px; }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.section-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title-icon-box {
  width: 32px; height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #e1251b, #ff6700);
  display: flex;
  align-items: center;
  justify-content: center;
}
.section-title { font-size: 20px; font-weight: 700; }
.title-line {
  width: 32px; height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg, #e1251b, transparent);
}
.section-desc { font-size: 13px; color: #999; display: flex; align-items: center; gap: 4px; }

/* ========== 商品网格 ========== */
.seckill-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.seckill-card {
  position: relative;
  border-radius: 12px;
  overflow: visible;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.seckill-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(225,37,27,0.15), 0 0 0 1px rgba(225,37,27,0.08);
}

/* 排名角标 */
.rank-badge {
  position: absolute;
  top: -1px; right: 16px;
  padding: 5px 14px;
  border-radius: 0 0 8px 8px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 2;
}
.rank-1 { background: linear-gradient(135deg, #e1251b, #ff4757); }
.rank-2 { background: linear-gradient(135deg, #ff6700, #ffa502); }
.rank-3 { background: linear-gradient(135deg, #f5a623, #f0c040); }
.rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) { background: linear-gradient(135deg, #999, #bbb); }

/* 疯抢标签 */
.hot-tag {
  position: absolute;
  top: 12px; left: 12px;
  z-index: 2;
  background: linear-gradient(135deg, #e1251b, #ff4757);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
  animation: hotPulse 1.5s ease-in-out infinite;
}
.hot-dot {
  width: 6px; height: 6px;
  background: #fff;
  border-radius: 50%;
  animation: dotBlink 1s ease-in-out infinite;
}
@keyframes hotPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}
@keyframes dotBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* 图片区 */
.card-image {
  position: relative;
  padding-top: 100%;
  border-radius: 12px 12px 0 0;
  overflow: hidden;
}
.placeholder-img {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.4s;
}
.seckill-card:hover .placeholder-img { transform: scale(1.06); }
.placeholder-svg { width: 56px; height: 56px; color: rgba(255,255,255,0.6); }
.card-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 图片倒计时叠加 */
.img-countdown {
  position: absolute;
  top: 10px; right: 10px;
  background: rgba(0,0,0,0.65);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 1;
  font-variant-numeric: tabular-nums;
}

/* SVG 圆环库存 */
.stock-ring-wrap {
  position: absolute;
  bottom: 10px; left: 10px;
  width: 48px; height: 48px;
  z-index: 1;
}
.stock-ring { width: 48px; height: 48px; }
.ring-progress {
  transition: stroke-dashoffset 0.8s ease;
}
.ring-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 9px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
}

/* 内容区 */
.card-body { padding: 16px; }
.product-name { font-size: 14px; font-weight: 500; margin-bottom: 4px; line-height: 1.4; height: 40px; }
.product-spec { font-size: 12px; color: #999; margin-bottom: 10px; display: flex; align-items: center; gap: 4px; }

.price-section { margin-bottom: 8px; }
.price-row { display: flex; align-items: baseline; gap: 8px; }
.seckill-price { color: #e1251b; }
.price-symbol { font-size: 14px; }
.price-num { font-size: 26px; font-weight: 800; }
.normal-price { font-size: 13px; color: #bbb; text-decoration: line-through; }
.discount-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #ff6700;
  background: #fff0f0;
  padding: 3px 10px;
  border-radius: 999px;
  margin-top: 4px;
  border: 1px solid #ffe0d0;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #999;
  margin-bottom: 12px;
}
.limit-info, .viewer-info {
  display: flex;
  align-items: center;
  gap: 3px;
}

/* 抢购按钮 */
.buy-btn {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.3s;
}
.btn-active {
  background: linear-gradient(135deg, #e1251b, #ff4757);
  color: #fff;
  box-shadow: 0 4px 16px rgba(225,37,27,0.3);
  animation: btnPulse 2s ease-in-out infinite;
}
.btn-active:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 24px rgba(225,37,27,0.4);
}
.btn-active::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transform: translateX(-100%);
  animation: btnShine 3s ease-in-out infinite;
}
.btn-disabled {
  background: #e0e0e0;
  color: #999;
  cursor: not-allowed;
}
.btn-text {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
@keyframes btnPulse {
  0%, 100% { box-shadow: 0 4px 16px rgba(225,37,27,0.3); }
  50% { box-shadow: 0 4px 24px rgba(225,37,27,0.5); }
}
@keyframes btnShine {
  0%, 70%, 100% { transform: translateX(-100%); }
  80% { transform: translateX(100%); }
}

/* 近期抢购 */
.recent-orders {
  margin-top: 10px;
  font-size: 11px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 5px;
  animation: fadeInUp 0.5s ease;
}
.recent-dot {
  width: 5px; height: 5px;
  background: #52c41a;
  border-radius: 50%;
  flex-shrink: 0;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ========== 空状态 ========== */
.empty-state { text-align: center; padding: 80px 0; }
.empty-icon-wrap {
  width: 80px; height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #fff0f0, #ffe8d6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.empty-icon { font-size: 40px; }
.empty-text { font-size: 18px; color: #666; font-weight: 600; margin-bottom: 8px; }
.empty-sub { font-size: 14px; color: #999; margin-bottom: 24px; }
.empty-btn {
  display: inline-block;
  padding: 10px 28px;
  background: linear-gradient(135deg, #e1251b, #ff6700);
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: transform 0.2s, box-shadow 0.2s;
}
.empty-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(225,37,27,0.3); color: #fff; }

/* ========== 响应式 ========== */
@media (max-width: 1200px) { .seckill-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) {
  .seckill-grid { grid-template-columns: repeat(2, 1fr); }
  .hero-content { flex-direction: column; text-align: center; gap: 24px; }
  .hero-tags { justify-content: center; }
  .hero-badge-row { text-align: center; }
}
</style>
