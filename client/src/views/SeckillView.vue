<script setup lang="ts">
/**
 * 秒杀专场页 - 华丽版 v2
 * 火焰特效头部 + 发光倒计时 + 渐变边框卡片 + 进度条
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const seckillItems = ref<any[]>([])
const loading = ref(true)
const activityId = 1
const now = ref(Date.now())
let timer: ReturnType<typeof setInterval>

function getTimeLeft(endTime: string) {
  const end = new Date(endTime).getTime()
  const diff = end - now.value
  if (diff <= 0) return { days: 0, hours: 0, minutes: 0, seconds: 0, text: '已结束' }
  return {
    days: Math.floor(diff / 86400000),
    hours: Math.floor((diff % 86400000) / 3600000),
    minutes: Math.floor((diff % 3600000) / 60000),
    seconds: Math.floor((diff % 60000) / 1000),
    text: ''
  }
}

function getStockPercent(item: any) {
  const total = 20
  return Math.max(5, Math.round((1 - item.seckillStock / total) * 100))
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
  try {
    const res: any = await request.post('/seckill/buy', { seckillItemId: item.seckillItemId })
    ElMessage.success('🎉 抢购成功！订单号：' + res.data.orderNo)
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
    <!-- ========== 火焰特效头部 ========== -->
    <div class="seckill-hero">
      <div class="hero-bg">
        <div class="fire-particle" v-for="i in 30" :key="i" :style="{
          left: Math.random() * 100 + '%',
          animationDelay: Math.random() * 3 + 's',
          animationDuration: (Math.random() * 2 + 2) + 's'
        }"></div>
      </div>
      <div class="hero-content container">
        <div class="hero-left">
          <div class="hero-badge animate-pulse">⚡</div>
          <h1 class="hero-title">
            <span class="title-icon">🔥</span>
            限时秒杀专场
          </h1>
          <p class="hero-sub">每日精选 · 限时限量 · 超值优惠</p>
          <div class="hero-tags">
            <span class="tag tag-red"><el-icon><Timer /></el-icon> 限时</span>
            <span class="tag tag-orange"><el-icon><Discount /></el-icon> 特价</span>
            <span class="tag tag-gold"><el-icon><Medal /></el-icon> 爆款</span>
          </div>
        </div>
        <div class="hero-right">
          <div class="big-countdown">
            <div class="countdown-title">
              <el-icon><Clock /></el-icon> 距结束
            </div>
            <div class="countdown-display" v-if="seckillItems.length">
              <div class="countdown-unit">
                <span class="unit-num glow-text">{{ String(getTimeLeft(seckillItems[0]?.endTime).hours).padStart(2, '0') }}</span>
                <span class="unit-label">时</span>
              </div>
              <span class="unit-sep pulse-sep">:</span>
              <div class="countdown-unit">
                <span class="unit-num glow-text">{{ String(getTimeLeft(seckillItems[0]?.endTime).minutes).padStart(2, '0') }}</span>
                <span class="unit-label">分</span>
              </div>
              <span class="unit-sep pulse-sep">:</span>
              <div class="countdown-unit">
                <span class="unit-num glow-text">{{ String(getTimeLeft(seckillItems[0]?.endTime).seconds).padStart(2, '0') }}</span>
                <span class="unit-label">秒</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 波浪装饰 -->
      <div class="wave-decoration">
        <svg viewBox="0 0 1440 120" preserveAspectRatio="none">
          <path d="M0,60 C360,120 720,0 1080,60 C1260,90 1380,60 1440,60 L1440,120 L0,120 Z" fill="#f5f5f5"/>
        </svg>
      </div>
    </div>

    <!-- ========== 秒杀商品列表 ========== -->
    <div class="seckill-content container" v-loading="loading">
      <div class="section-header">
        <div class="section-title-group">
          <span class="title-icon-box" style="background:linear-gradient(135deg,#e1251b,#ff6700)"><el-icon :size="18" color="#fff"><Lightning /></el-icon></span>
          <h2 class="section-title">爆款秒杀</h2>
          <span class="title-line"></span>
        </div>
        <div class="section-desc">
          <el-icon><InfoFilled /></el-icon> 每人限购，抢完即止
        </div>
      </div>

      <div class="seckill-grid">
        <div v-for="(item, index) in seckillItems" :key="item.seckillItemId" class="seckill-card" :style="{ animationDelay: index * 0.15 + 's' }">
          <!-- 排名标签 -->
          <div class="rank-badge" :class="'rank-' + (index + 1)">
            <el-icon v-if="index === 0"><Trophy /></el-icon>
            <el-icon v-else-if="index === 1"><Medal /></el-icon>
            <el-icon v-else><Star /></el-icon>
            {{ index === 0 ? 'TOP1' : index === 1 ? 'TOP2' : 'TOP3' }}
          </div>

          <!-- 商品图 -->
          <div class="card-image">
            <div class="placeholder-img" :style="{ background: `linear-gradient(135deg, hsl(${item.skuId * 47 % 360}, 70%, 80%), hsl(${item.skuId * 47 % 360 + 30}, 70%, 70%))` }">
              <span class="placeholder-icon">{{ item.productName?.charAt(0) }}</span>
            </div>
            <!-- 库存进度条 -->
            <div class="stock-progress">
              <div class="stock-fill" :style="{ width: getStockPercent(item) + '%' }">
                <div class="stock-shine"></div>
              </div>
              <span class="stock-text">已抢 {{ getStockPercent(item) }}%</span>
            </div>
          </div>

          <!-- 商品信息 -->
          <div class="card-body">
            <div class="product-name ellipsis-2">{{ item.productName }}</div>
            <div class="product-spec">
              <el-icon><Tag /></el-icon> {{ item.specs }}
            </div>

            <!-- 价格区 -->
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

            <!-- 限购信息 -->
            <div class="limit-info">
              <el-icon><Warning /></el-icon>
              限购 {{ item.purchaseLimit }} 件 · 剩余 {{ item.seckillStock }} 件
            </div>

            <!-- 抢购按钮 -->
            <el-button
              type="primary"
              size="large"
              class="buy-btn"
              :class="{ 'btn-disabled': item.seckillStock <= 0 || item.activityStatus !== 1 }"
              :disabled="item.seckillStock <= 0 || item.activityStatus !== 1"
              @click="handleBuy(item)"
            >
              <el-icon v-if="item.seckillStock > 0 && item.activityStatus === 1"><Lightning /></el-icon>
              <el-icon v-else><CircleClose /></el-icon>
              {{ item.seckillStock <= 0 ? '已抢完' : item.activityStatus !== 1 ? '未开始' : '立即抢购' }}
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="!loading && seckillItems.length === 0" class="empty-state">
        <div class="empty-illustration">
          <div class="empty-lightning">⚡</div>
          <div class="empty-rings">
            <div class="ring ring-1"></div>
            <div class="ring ring-2"></div>
            <div class="ring ring-3"></div>
          </div>
        </div>
        <p class="empty-text">暂无秒杀活动</p>
        <p class="empty-sub">敬请期待下一场秒杀</p>
        <router-link to="/" class="empty-btn">去首页逛逛</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== 火焰头部 ========== */
.seckill-hero {
  background: linear-gradient(135deg, #1a1a2e, #16213e, #0f3460);
  position: relative;
  overflow: hidden;
  padding: 0;
}
.hero-bg {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
}
.fire-particle {
  position: absolute;
  bottom: 0;
  width: 4px;
  height: 4px;
  background: #ff6b35;
  border-radius: 50%;
  animation: fireRise 3s ease-in infinite;
  box-shadow: 0 0 8px #ff6b35, 0 0 16px rgba(255,107,53,0.4);
}
.fire-particle:nth-child(3n) { background: #ffd700; box-shadow: 0 0 8px #ffd700, 0 0 16px rgba(255,215,0,0.4); width: 3px; height: 3px; }
.fire-particle:nth-child(5n) { background: #ff4e3a; box-shadow: 0 0 8px #ff4e3a, 0 0 16px rgba(255,78,58,0.4); width: 5px; height: 5px; }
@keyframes fireRise {
  0% { opacity: 1; transform: translateY(0) scale(1); }
  50% { opacity: 0.8; }
  100% { opacity: 0; transform: translateY(-250px) scale(0); }
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 50px 15px 80px;
  position: relative;
  z-index: 2;
}
.hero-left { color: #fff; }
.hero-badge { font-size: 48px; margin-bottom: 12px; filter: drop-shadow(0 0 20px rgba(255,215,0,0.6)); }
.hero-title { font-size: 36px; font-weight: bold; display: flex; align-items: center; gap: 12px; text-shadow: 0 2px 12px rgba(0,0,0,0.3); }
.title-icon { font-size: 40px; }
.hero-sub { font-size: 16px; opacity: 0.85; margin-top: 8px; text-shadow: 0 1px 4px rgba(0,0,0,0.2); }
.hero-tags { display: flex; gap: 8px; margin-top: 16px; }

.big-countdown {
  background: rgba(255,255,255,0.08);
  border-radius: 20px;
  padding: 28px 36px;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.15);
  text-align: center;
  color: #fff;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
  position: relative;
  overflow: hidden;
}
.big-countdown::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: conic-gradient(from 0deg, transparent, rgba(225,37,27,0.1), transparent, rgba(255,103,0,0.1), transparent);
  animation: rotateBg 8s linear infinite;
  pointer-events: none;
}
@keyframes rotateBg {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.countdown-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  opacity: 0.85;
  margin-bottom: 14px;
}
.countdown-display { display: flex; align-items: center; gap: 10px; }
.countdown-unit { text-align: center; }
.unit-num {
  display: block;
  background: linear-gradient(135deg, #e1251b, #ff6700);
  padding: 12px 18px;
  border-radius: 10px;
  font-size: 30px;
  font-weight: bold;
  min-width: 64px;
  box-shadow: 0 4px 16px rgba(225,37,27,0.4);
}
.glow-text {
  text-shadow: 0 0 12px rgba(255,107,53,0.8);
  animation: glowPulse 2s ease-in-out infinite;
}
@keyframes glowPulse {
  0%, 100% { text-shadow: 0 0 12px rgba(255,107,53,0.6); }
  50% { text-shadow: 0 0 24px rgba(255,107,53,1), 0 0 48px rgba(255,107,53,0.4); }
}
.pulse-sep { animation: sepPulse 1s ease-in-out infinite; }
@keyframes sepPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
.unit-label { font-size: 12px; opacity: 0.75; margin-top: 6px; display: block; }
.unit-sep { font-size: 30px; font-weight: bold; color: #ff6700; }

.wave-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
}
.wave-decoration svg { display: block; width: 100%; height: 60px; }

/* ========== 秒杀内容 ========== */
.seckill-content { padding: 30px 15px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.section-title-group { display: flex; align-items: center; gap: 10px; }
.title-icon-box {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.section-title { font-size: 22px; font-weight: bold; }
.title-line {
  width: 40px;
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg, #e1251b, transparent);
}
.section-desc { font-size: 13px; color: #999; display: flex; align-items: center; gap: 4px; }

.seckill-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.seckill-card {
  position: relative;
  border-radius: 14px;
  overflow: visible;
  background: #fff;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  border: 2px solid transparent;
  transition: all 0.35s;
}
.seckill-card::before {
  content: '';
  position: absolute;
  top: -2px; left: -2px; right: -2px; bottom: -2px;
  border-radius: 16px;
  background: linear-gradient(135deg, #e1251b, #ff6700, #f5a623, #e1251b);
  background-size: 300% 300%;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.4s;
}
.seckill-card:hover::before {
  opacity: 1;
  animation: gradientShift 3s ease infinite;
}
.seckill-card:hover {
  border-color: transparent;
  box-shadow: 0 12px 40px rgba(225,37,27,0.2);
  transform: translateY(-8px);
}
@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.rank-badge {
  position: absolute;
  top: -1px;
  right: 16px;
  padding: 5px 14px;
  border-radius: 0 0 10px 10px;
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 1;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.rank-1 { background: linear-gradient(135deg, #e1251b, #ff4e3a); }
.rank-2 { background: linear-gradient(135deg, #ff6700, #ff9500); }
.rank-3 { background: linear-gradient(135deg, #f5a623, #f7c948); }

.card-image { position: relative; padding-top: 100%; border-radius: 12px 12px 0 0; overflow: hidden; }
.placeholder-img {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.4s;
}
.seckill-card:hover .placeholder-img { transform: scale(1.05); }
.placeholder-icon { font-size: 56px; color: rgba(255,255,255,0.7); font-weight: bold; }

.stock-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 28px;
  background: rgba(0,0,0,0.65);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
}
.stock-fill {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  background: linear-gradient(90deg, #ff4e3a, #e1251b, #ff6700);
  background-size: 200% 100%;
  animation: shimmer 2s infinite;
  transition: width 0.8s ease;
}
.stock-shine {
  position: absolute;
  top: 0; right: 0; bottom: 0;
  width: 30px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  animation: shineMove 2s infinite;
}
@keyframes shineMove {
  0% { transform: translateX(-30px); }
  100% { transform: translateX(30px); }
}
.stock-text { position: relative; z-index: 1; color: #fff; font-size: 12px; font-weight: bold; text-shadow: 0 1px 2px rgba(0,0,0,0.3); }

.card-body { padding: 16px; }
.product-name { font-size: 14px; font-weight: bold; margin-bottom: 6px; line-height: 1.4; height: 40px; }
.product-spec { font-size: 12px; color: #999; margin-bottom: 10px; display: flex; align-items: center; gap: 4px; }

.price-section { margin-bottom: 8px; }
.price-row { display: flex; align-items: baseline; gap: 8px; }
.seckill-price { color: var(--jd-red); }
.price-symbol { font-size: 14px; }
.price-num { font-size: 24px; font-weight: bold; }
.normal-price { font-size: 13px; color: #ccc; text-decoration: line-through; }
.discount-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--jd-orange);
  background: linear-gradient(135deg, #fff5f0, #fff0e8);
  padding: 3px 10px;
  border-radius: 12px;
  margin-top: 4px;
  border: 1px solid #ffe0d0;
}

.limit-info {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.buy-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 22px;
  font-weight: bold;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #e1251b, #ff4e3a) !important;
  border: none !important;
  transition: all 0.3s;
  box-shadow: 0 4px 16px rgba(225,37,27,0.3);
  position: relative;
  overflow: hidden;
}
.buy-btn::after {
  content: '';
  position: absolute;
  top: 0; left: -100%; right: 0; bottom: 0;
  width: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  transition: none;
}
.buy-btn:hover:not(.btn-disabled)::after {
  animation: btnShine 0.6s ease forwards;
}
@keyframes btnShine {
  0% { left: -100%; }
  100% { left: 100%; }
}
.buy-btn:hover:not(.btn-disabled) {
  transform: scale(1.03);
  box-shadow: 0 8px 28px rgba(225,37,27,0.45);
}
.btn-disabled {
  background: linear-gradient(135deg, #ccc, #ddd) !important;
  box-shadow: none !important;
}

/* ========== 空状态 ========== */
.empty-state { text-align: center; padding: 80px 0; }
.empty-illustration {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 24px;
}
.empty-lightning {
  font-size: 56px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
  animation: float 3s ease-in-out infinite;
}
.empty-rings {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
}
.ring {
  position: absolute;
  border-radius: 50%;
  border: 2px solid #e1251b;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: ringPulse 2s ease-out infinite;
}
.ring-1 { width: 60px; height: 60px; opacity: 0.6; animation-delay: 0s; }
.ring-2 { width: 90px; height: 90px; opacity: 0.3; animation-delay: 0.4s; }
.ring-3 { width: 120px; height: 120px; opacity: 0.1; animation-delay: 0.8s; }
@keyframes ringPulse {
  0% { transform: translate(-50%, -50%) scale(0.8); opacity: 0.6; }
  100% { transform: translate(-50%, -50%) scale(1.4); opacity: 0; }
}
.empty-text { font-size: 18px; color: #666; font-weight: bold; margin-bottom: 8px; }
.empty-sub { font-size: 14px; color: #999; margin-bottom: 24px; }
.empty-btn {
  display: inline-block;
  padding: 10px 28px;
  background: linear-gradient(135deg, #e1251b, #ff6700);
  color: #fff;
  border-radius: 24px;
  font-size: 14px;
  font-weight: bold;
  text-decoration: none;
  transition: all 0.3s;
  box-shadow: 0 4px 16px rgba(225,37,27,0.3);
}
.empty-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 24px rgba(225,37,27,0.4); }

@media (max-width: 1200px) { .seckill-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) { .seckill-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
