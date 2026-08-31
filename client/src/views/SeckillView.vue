<script setup lang="ts">
/**
 * 秒杀专场页 - 图片同款效果 v3
 * 放射线背景 + 发光边框 + 热抢中圆环 + 价格重播动画
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

// ========== 随机倒计时（每个商品独立，1s ~ 3min） ==========
const perItemTimers = ref<Record<number, number>>({})
const perItemTimerIntervals = ref<Record<number, ReturnType<typeof setInterval>>>({})

function initItemTimer(itemId: number) {
  perItemTimers.value[itemId] = Math.floor(Math.random() * 179 + 1)
  clearInterval(perItemTimerIntervals.value[itemId])
  perItemTimerIntervals.value[itemId] = setInterval(() => {
    perItemTimers.value[itemId]--
    if (perItemTimers.value[itemId] <= 0) {
      perItemTimers.value[itemId] = Math.floor(Math.random() * 179 + 1)
    }
  }, 1000)
}

function getRandCountdown(itemId: number) {
  const total = Math.max(0, perItemTimers.value[itemId] ?? 0)
  const m = Math.floor(total / 60)
  const s = total % 60
  return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0')
}

// ========== 价格动画（每次进入重新播放） ==========
const animatedPrices = ref<Record<number, number>>({})
const priceRefs = ref<HTMLElement[]>([])
const observers = ref<IntersectionObserver[]>([])

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
  // 清理旧 observer
  observers.value.forEach(o => o.disconnect())
  observers.value = []

  nextTick(() => {
    priceRefs.value.forEach((el, idx) => {
      if (!el) return
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          const item = seckillItems.value[idx]
          if (!item) return
          if (entry.isIntersecting) {
            // 进入视口 → 重置为原价 → 播放动画
            animatedPrices.value[item.seckillItemId] = item.normalPrice
            animatePrice(item.seckillItemId, item.normalPrice, item.seckillPrice)
          }
        })
      }, { threshold: 0.3 })
      observer.observe(el)
      observers.value.push(observer)
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

async function fetchSeckill() {
  loading.value = true
  try {
    const res: any = await request.get(`/seckill/list?activityId=${activityId}`)
    seckillItems.value = res.data
    res.data.forEach((item: any) => {
      animatedPrices.value[item.seckillItemId] = item.normalPrice
      initItemTimer(item.seckillItemId)
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
onUnmounted(() => {
  clearInterval(timer)
  Object.values(perItemTimerIntervals.value).forEach(id => clearInterval(id))
  observers.value.forEach(o => o.disconnect())
})
</script>

<template>
  <div class="seckill-page">
    <!-- ========== Hero 头部 ========== -->
    <div class="seckill-hero">
      <div class="light-sweep"></div>
      <div class="hero-particles">
        <span v-for="i in 20" :key="i" class="particle" :class="'pt-' + ((i % 4) + 1)" :style="{
          left: (i * 5) + '%', animationDelay: (i * 0.3) + 's',
          animationDuration: (2.5 + (i % 4) * 0.6) + 's'
        }"></span>
      </div>
      <div class="hero-particles">
        <span v-for="i in 8" :key="'l' + i" class="particle lightning-particle" :style="{
          left: (i * 12 + 3) + '%', animationDelay: (i * 0.7 + 0.2) + 's',
          animationDuration: (3 + (i % 3)) + 's'
        }">⚡</span>
      </div>
      <div class="hero-content container">
        <div class="hero-left">
          <div class="hero-badge-row">
            <span class="hero-badge" :class="statusClass">{{ statusLabel }}</span>
          </div>
          <h1 class="hero-title">
            <span class="hero-icon-wrap"><el-icon :size="32"><Lightning /></el-icon></span>
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
          <!-- 图片区 - 深色底 + 放射线 + 发光边框 -->
          <div class="card-image">
            <div class="card-dark-bg">
              <!-- 产品图 -->
              <div class="product-glow-wrap">
                <div class="product-img-box">
                  <!-- 放射线叠加在图片上 -->
                  <div class="radial-lines"></div>
                  <img v-if="item.productImage" :src="item.productImage" :alt="item.productName" class="card-img" @error="($event.target as HTMLImageElement).style.display='none'" />
                  <svg v-else class="placeholder-svg" viewBox="0 0 64 64" fill="none">
                    <rect x="12" y="16" width="40" height="32" rx="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
                    <path d="M16 40 L24 30 L30 36 L42 22 L48 30" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"/>
                    <circle cx="22" cy="26" r="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
                  </svg>
                  <!-- 热抢中圆环 - 图片内底部居中 -->
                  <div class="hot-ring">
                    <svg class="hot-ring-svg" viewBox="0 0 100 100">
                      <circle cx="50" cy="50" r="44" fill="none" stroke="rgba(255,100,0,0.2)" stroke-width="4" />
                      <circle cx="50" cy="50" r="44" fill="none" stroke="#ff4500" stroke-width="4"
                        stroke-linecap="round" stroke-dasharray="276.5"
                        :stroke-dashoffset="276.5 * (1 - getStockPercent(item) / 100)"
                        transform="rotate(-90 50 50)" class="hot-ring-progress" />
                    </svg>
                    <div class="hot-ring-inner">
                      <span class="hot-ring-icon">🔥</span>
                      <span class="hot-ring-text">热抢中</span>
                      <span class="hot-ring-count">{{ getViewersCount(item.skuId) }}人参与</span>
                    </div>
                  </div>
                </div>
              </div>
              <!-- 限时秒杀大标题 -->
              <div class="img-title-row">
                <span class="img-lightning">⚡</span>
                <span class="img-title-text">限时秒杀</span>
                <span class="img-lightning">⚡</span>
              </div>
              <!-- 左右装饰 -->
              <span class="deco-left">🔥</span>
              <span class="deco-right">⚡</span>
              <!-- 角标 - 随机倒计时 -->
              <div class="img-top-right" v-if="item.activityStatus === 1">
                <el-icon :size="14"><Clock /></el-icon>
                {{ getRandCountdown(item.seckillItemId) }}
              </div>
            </div>
          </div>

          <!-- 内容区 -->
          <div class="card-body" :ref="(el: any) => { if (el) priceRefs[index] = el }">
            <div class="product-name ellipsis-2">{{ item.productName }}</div>
            <div class="product-spec"><el-icon><Tag /></el-icon> {{ item.specs }}</div>

            <!-- 价格区 -->
            <div class="price-section">
              <div class="price-row">
                <span class="seckill-price">
                  <span class="price-symbol">¥</span>
                  <span class="price-num">{{ (animatedPrices[item.seckillItemId] || item.normalPrice).toFixed(0) }}</span>
                </span>
                <span class="normal-price">¥{{ item.normalPrice }}</span>
                <span class="flash-badge-inline">限时秒杀</span>
                <span class="save-burst">直降<br>¥{{ (item.normalPrice - item.seckillPrice).toFixed(0) }}</span>
              </div>
              <div class="save-tag-row">
                <span class="save-tag-orange">
                  <el-icon><Discount /></el-icon>
                  已省 ¥{{ (item.normalPrice - item.seckillPrice).toFixed(0) }}
                </span>
              </div>
            </div>

            <!-- 库存 + 人气 -->
            <div class="urgency-row">
              <span class="urgency-stock">
                <el-icon><Warning /></el-icon> 限购{{ item.purchaseLimit }}件 · 剩{{ item.seckillStock }}件
              </span>
              <span class="urgency-hot">
                <el-icon><View /></el-icon> {{ getViewersCount(item.skuId) }}人围观
              </span>
            </div>

            <!-- 抢购按钮 -->
            <button class="buy-btn" :class="{
              'btn-active': item.seckillStock > 0 && item.activityStatus === 1,
              'btn-disabled': item.seckillStock <= 0 || item.activityStatus !== 1
            }" @click="handleBuy(item)">
              <span class="btn-glow"></span>
              <span class="btn-text" v-if="item.seckillStock > 0 && item.activityStatus === 1">
                🔥 立即抢购
              </span>
              <span class="btn-text" v-else-if="item.seckillStock <= 0">
                <el-icon><CircleClose /></el-icon> 已抢完
              </span>
              <span class="btn-text" v-else>未开始</span>
            </button>
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
.seckill-page { min-height: 100vh; background: #f5f5f5; }

.seckill-hero {
  position: relative;
  background: linear-gradient(135deg, #b71c1c, #e1251b 30%, #ff6700 100%);
  overflow: hidden;
}
.seckill-hero::before {
  content: ''; position: absolute; inset: 0;
  background: url('/images/seckill-hero.png') center/cover no-repeat;
  opacity: 0.45; pointer-events: none;
}
.light-sweep {
  position: absolute; inset: 0;
  background: linear-gradient(105deg, transparent 40%, rgba(255,215,0,0.15) 45%, rgba(255,215,0,0.25) 50%, rgba(255,215,0,0.15) 55%, transparent 60%);
  animation: sweep 4s ease-in-out infinite; pointer-events: none; z-index: 1;
}
@keyframes sweep { 0%{transform:translateX(-100%);} 100%{transform:translateX(200%);} }

.hero-particles { position: absolute; inset: 0; overflow: hidden; pointer-events: none; z-index: 0; }
.particle { position: absolute; bottom: -20px; font-size: 16px; animation: particleUp linear infinite; }
.pt-1 { font-size: 14px; } .pt-2 { font-size: 18px; } .pt-3 { font-size: 12px; } .pt-4 { font-size: 20px; }
.particle.pt-1::after { content: '🪙'; } .particle.pt-2::after { content: '🧧'; }
.particle.pt-3::after { content: '🎁'; } .particle.pt-4::after { content: '💰'; }
.lightning-particle { font-size: 22px; filter: drop-shadow(0 0 6px rgba(255,215,0,0.8)); animation: particleUp linear infinite, lightningFlash 0.3s ease-in-out infinite; }
@keyframes particleUp { 0%{transform:translateY(0) rotate(0);opacity:0;} 10%{opacity:1;} 90%{opacity:0.8;} 100%{transform:translateY(-300px) rotate(20deg);opacity:0;} }
@keyframes lightningFlash { 0%,100%{opacity:1;} 50%{opacity:0.3;} }

.hero-content { position: relative; z-index: 2; display: flex; align-items: center; justify-content: space-between; padding: 48px 16px 60px; }
.hero-left { color: #fff; flex: 1; }
.hero-badge-row { margin-bottom: 12px; }
.hero-badge { display: inline-block; padding: 4px 14px; border-radius: 20px; font-size: 13px; font-weight: 600; animation: badgePulse 2s ease-in-out infinite; }
.status-active { background: rgba(255,255,255,0.25); color: #fff; border: 1px solid rgba(255,255,255,0.4); }
.status-upcoming { background: rgba(255,193,7,0.3); color: #ffd54f; border: 1px solid rgba(255,193,7,0.5); }
.status-ended { background: rgba(255,255,255,0.1); color: rgba(255,255,255,0.6); border: 1px solid rgba(255,255,255,0.2); animation: none; }
@keyframes badgePulse { 0%,100%{opacity:1;} 50%{opacity:0.7;} }

.hero-title { font-size: 36px; font-weight: 900; display: flex; align-items: baseline; gap: 8px; }
.hero-title-text { text-shadow: 0 0 20px rgba(255,215,0,0.6), 0 0 40px rgba(255,107,0,0.4), 0 2px 8px rgba(0,0,0,0.3); animation: titleGlow 2s ease-in-out infinite; }
.hero-title-sub { font-size: 20px; opacity: 0.85; font-weight: 700; }
@keyframes titleGlow { 0%,100%{text-shadow:0 0 20px rgba(255,215,0,0.6),0 0 40px rgba(255,107,0,0.4);} 50%{text-shadow:0 0 30px rgba(255,215,0,0.9),0 0 60px rgba(255,107,0,0.6);} }

.hero-icon-wrap { display: flex; align-items: center; justify-content: center; width: 52px; height: 52px; background: rgba(255,255,255,0.15); border-radius: 14px; backdrop-filter: blur(4px); animation: iconFlash 1.5s ease-in-out infinite; }
@keyframes iconFlash { 0%,100%{transform:scale(1);} 50%{transform:scale(1.12);} }

.hero-sub { font-size: 15px; opacity: 0.85; margin-top: 8px; }
.hero-tags { display: flex; gap: 8px; margin-top: 16px; }
.hero-tags .tag { display: inline-flex; align-items: center; gap: 4px; background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.25); color: #fff; padding: 5px 14px; border-radius: 20px; font-size: 12px; font-weight: 500; backdrop-filter: blur(4px); }

.countdown-card { background: rgba(0,0,0,0.4); border-radius: 16px; padding: 20px 24px; backdrop-filter: blur(12px); border: 1px solid rgba(255,100,0,0.3); text-align: center; color: #fff; min-width: 260px; box-shadow: 0 0 30px rgba(255,60,0,0.3), 0 0 60px rgba(255,60,0,0.15); }
.cd-label { font-size: 13px; opacity: 0.8; margin-bottom: 12px; letter-spacing: 2px; }
.cd-flip-row { display: flex; align-items: center; justify-content: center; gap: 5px; }
.cd-flip { width: 40px; height: 52px; background: #1a1a2e; border-radius: 8px; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 12px rgba(0,0,0,0.4); }
.cd-num-glow { font-size: 26px; font-weight: 900; color: #fff; font-variant-numeric: tabular-nums; text-shadow: 0 0 10px rgba(255,80,0,0.8), 0 0 20px rgba(255,80,0,0.5); animation: numPulse 1s ease-in-out infinite; }
@keyframes numPulse { 0%,100%{text-shadow:0 0 10px rgba(255,80,0,0.8),0 0 20px rgba(255,80,0,0.5);transform:scale(1);} 50%{text-shadow:0 0 16px rgba(255,80,0,1),0 0 32px rgba(255,80,0,0.7);transform:scale(1.05);} }
.cd-colon { display: flex; align-items: center; justify-content: center; margin: 0 2px; }
.colon-icon { font-size: 16px; color: #ffd700; animation: colonBlink 1s step-end infinite; filter: drop-shadow(0 0 4px rgba(255,215,0,0.8)); }
@keyframes colonBlink { 0%,100%{opacity:1;transform:scale(1);} 50%{opacity:0.3;transform:scale(0.8);} }
.cd-unit-row { display: flex; justify-content: center; gap: 52px; margin-top: 6px; font-size: 11px; opacity: 0.5; }

.hero-fire { position: absolute; bottom: -2px; left: 0; right: 0; height: 40px; background: linear-gradient(180deg, transparent 0%, #f5f5f5 100%); z-index: 3; }
.hero-fire::before { content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 30px; background: radial-gradient(ellipse at center bottom, rgba(255,107,0,0.25) 0%, transparent 70%); filter: blur(8px); }

/* ==================== 内容区 ==================== */
.seckill-content { padding: 32px 16px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.section-title-group { display: flex; align-items: center; gap: 12px; }
.title-icon-box { width: 32px; height: 32px; border-radius: 8px; background: linear-gradient(135deg, #e1251b, #ff6700); display: flex; align-items: center; justify-content: center; }
.section-title { font-size: 20px; font-weight: 700; }
.title-line { width: 32px; height: 3px; border-radius: 2px; background: linear-gradient(90deg, #e1251b, transparent); }
.section-desc { font-size: 13px; color: #999; display: flex; align-items: center; gap: 4px; }

/* ==================== 商品卡片 ==================== */
.seckill-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.seckill-card { border-radius: 12px; overflow: hidden; background: #fff; box-shadow: 0 2px 12px rgba(0,0,0,0.06); transition: transform 0.3s, box-shadow 0.3s; }
.seckill-card:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(225,37,27,0.15); }

/* 图片区 - 深色底 + 放射线 */
.card-image { position: relative; }
.card-dark-bg {
  position: relative;
  background: #1a1020;
  padding: 20px;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 放射线 - 叠加在图片上 */
.radial-lines {
  position: absolute; inset: -50%;
  width: 200%; height: 200%;
  background: repeating-conic-gradient(
    from 0deg,
    transparent 0deg 8deg,
    rgba(255,30,0,0.28) 8deg 10deg
  );
  animation: radialSpin 30s linear infinite;
  z-index: 2;
  pointer-events: none;
  mix-blend-mode: screen;
}
@keyframes radialSpin { 0%{transform:rotate(0deg);} 100%{transform:rotate(360deg);} }

/* 产品图发光框 */
.product-glow-wrap {
  position: relative; z-index: 2;
  padding: 6px;
  width: 96%;
  border-radius: 16px;
  background: linear-gradient(135deg, #ff2d00, #ff6700, #ff2d00);
  box-shadow: 0 0 30px rgba(255,45,0,0.6), 0 0 60px rgba(255,45,0,0.3);
  animation: glowPulse 2s ease-in-out infinite;
}
@keyframes glowPulse {
  0%,100%{box-shadow:0 0 30px rgba(255,45,0,0.6),0 0 60px rgba(255,45,0,0.3);}
  50%{box-shadow:0 0 40px rgba(255,45,0,0.8),0 0 80px rgba(255,45,0,0.5);}
}
.product-img-box {
  width: 100%; aspect-ratio: 1/1;
  border-radius: 12px;
  overflow: hidden;
  background: #2a2030;
  display: flex; align-items: center; justify-content: center;
  position: relative;
}
.card-img { width: 100%; height: 100%; object-fit: cover; }
.placeholder-svg { width: 56px; height: 56px; color: rgba(255,255,255,0.4); }

/* 限时秒杀大标题 */
.img-title-row {
  position: relative; z-index: 2;
  display: flex; align-items: center; gap: 8px;
  margin-top: 16px;
}
.img-lightning { font-size: 24px; filter: drop-shadow(0 0 8px rgba(255,215,0,0.8)); animation: lightningFlash 1s ease-in-out infinite; }
.img-title-text {
  font-size: 28px; font-weight: 900; color: #fff;
  text-shadow: 0 0 20px rgba(255,60,0,0.8), 0 0 40px rgba(255,60,0,0.5), 0 2px 4px rgba(0,0,0,0.5);
  letter-spacing: 4px;
}

/* 热抢中圆环 - 在图片内底部居中 */
.hot-ring {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px; height: 80px;
  z-index: 3;
}
.hot-ring-svg { width: 80px; height: 80px; }
.hot-ring-progress { transition: stroke-dashoffset 0.8s ease; filter: drop-shadow(0 0 6px rgba(255,69,0,0.6)); }
.hot-ring-inner {
  position: absolute; inset: 0;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
}
.hot-ring-icon { font-size: 18px; }
.hot-ring-text { font-size: 12px; font-weight: 800; color: #fff; text-shadow: 0 1px 3px rgba(0,0,0,0.5); }
.hot-ring-count { font-size: 9px; color: rgba(255,255,255,0.7); }

/* 左右装饰 */
.deco-left, .deco-right {
  position: absolute; z-index: 2; font-size: 20px;
  filter: drop-shadow(0 0 6px rgba(255,100,0,0.6));
}
.deco-left { left: 16px; top: 50%; transform: translateY(-50%); animation: decoFloat 2s ease-in-out infinite; }
.deco-right { right: 16px; top: 50%; transform: translateY(-50%); animation: decoFloat 2s ease-in-out infinite 0.5s; }
@keyframes decoFloat { 0%,100%{transform:translateY(-50%) scale(1);} 50%{transform:translateY(-50%) scale(1.2);} }

/* 右上角倒计时 */
.img-top-right {
  position: absolute; top: 12px; right: 12px; z-index: 3;
  background: rgba(0,0,0,0.75); backdrop-filter: blur(4px);
  color: #fff; font-size: 16px; font-weight: 900;
  padding: 6px 14px; border-radius: 10px;
  display: flex; align-items: center; gap: 5px;
  font-variant-numeric: tabular-nums;
  box-shadow: 0 2px 10px rgba(0,0,0,0.4);
  letter-spacing: 1px;
}

/* ==================== 内容区 ==================== */
.card-body { padding: 16px; }
.product-name { font-size: 14px; font-weight: 600; margin-bottom: 4px; line-height: 1.4; height: 40px; }
.product-spec { font-size: 12px; color: #999; margin-bottom: 8px; display: flex; align-items: center; gap: 4px; }

/* 价格区 */
.price-section { margin-bottom: 8px; }
.price-row { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.seckill-price { color: #e1251b; display: inline-flex; align-items: baseline; }
.price-symbol { font-size: 16px; font-weight: 700; }
.price-num { font-size: 34px; font-weight: 900; line-height: 1; }
.normal-price { font-size: 14px; color: #bbb; text-decoration: line-through; }

.flash-badge-inline {
  background: linear-gradient(135deg, #e1251b, #ff2d2d);
  color: #fff; font-size: 11px; font-weight: 800;
  padding: 3px 8px; border-radius: 4px;
  letter-spacing: 1px;
}

/* 直降爆炸标签 */
.save-burst {
  display: inline-flex; align-items: center; justify-content: center;
  width: 48px; height: 48px;
  background: linear-gradient(135deg, #ff4500, #ff6700);
  color: #fff; font-size: 10px; font-weight: 900;
  line-height: 1.1; text-align: center;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(255,69,0,0.4);
  animation: burstPulse 1.5s ease-in-out infinite;
  flex-shrink: 0;
}
@keyframes burstPulse { 0%,100%{transform:scale(1);} 50%{transform:scale(1.1);} }

.save-tag-row { margin-top: 6px; }
.save-tag-orange {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 12px; font-weight: 700; color: #fff;
  background: linear-gradient(135deg, #ff6700, #e1251b);
  padding: 4px 12px; border-radius: 999px;
  box-shadow: 0 2px 6px rgba(225,37,27,0.3);
}

.urgency-row {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 12px; color: #999; margin-bottom: 12px;
}
.urgency-stock { display: flex; align-items: center; gap: 3px; }
.urgency-hot { display: flex; align-items: center; gap: 3px; }

/* 抢购按钮 */
.buy-btn {
  width: 100%; height: 48px;
  border: 2px solid transparent; border-radius: 10px;
  font-size: 17px; font-weight: 900;
  letter-spacing: 2px; cursor: pointer;
  position: relative; overflow: hidden;
  transition: transform 0.2s, box-shadow 0.3s;
}
.btn-active {
  background: linear-gradient(135deg, #e1251b, #ff2d2d);
  color: #fff;
  box-shadow: 0 4px 20px rgba(225,37,27,0.5);
  border-color: rgba(255,215,0,0.4);
  animation: btnPulse 1.5s ease-in-out infinite, btnBorderPulse 2s ease-in-out infinite;
}
.btn-active:hover { transform: scale(1.03); box-shadow: 0 6px 30px rgba(225,37,27,0.6); }
.btn-glow {
  position: absolute; inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.25), transparent);
  transform: translateX(-100%);
  animation: btnShine 2.5s ease-in-out infinite;
}
.btn-disabled { background: #e0e0e0; color: #999; cursor: not-allowed; border-color: #ddd; }
.btn-text { position: relative; z-index: 1; display: inline-flex; align-items: center; gap: 6px; }
@keyframes btnPulse { 0%,100%{box-shadow:0 4px 20px rgba(225,37,27,0.5);} 50%{box-shadow:0 4px 30px rgba(225,37,27,0.7);} }
@keyframes btnBorderPulse { 0%,100%{border-color:rgba(255,215,0,0.4);} 50%{border-color:rgba(255,215,0,0.8);} }
@keyframes btnShine { 0%,70%,100%{transform:translateX(-100%);} 80%{transform:translateX(100%);} }

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
