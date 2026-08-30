<script setup lang="ts">
/**
 * 秒杀专场页 - 精简专业版
 * 红色系头部 + 简洁倒计时 + 干净商品卡片
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
    <!-- ========== 红色系头部 ========== -->
    <div class="seckill-hero">
      <div class="hero-content container">
        <div class="hero-left">
          <h1 class="hero-title">
            <el-icon :size="32"><Lightning /></el-icon>
            限时秒杀专场
          </h1>
          <p class="hero-sub">每日精选 · 限时限量 · 超值优惠</p>
          <div class="hero-tags">
            <span class="tag tag-red"><el-icon><Timer /></el-icon> 限时</span>
            <span class="tag tag-orange"><el-icon><Discount /></el-icon> 特价</span>
          </div>
        </div>
        <div class="hero-right">
          <div class="countdown-box">
            <div class="countdown-label">
              <el-icon><Clock /></el-icon> 距结束
            </div>
            <div class="countdown-display" v-if="seckillItems.length">
              <span class="cd-num">{{ String(getTimeLeft(seckillItems[0]?.endTime).hours).padStart(2, '0') }}</span>
              <span class="cd-sep">:</span>
              <span class="cd-num">{{ String(getTimeLeft(seckillItems[0]?.endTime).minutes).padStart(2, '0') }}</span>
              <span class="cd-sep">:</span>
              <span class="cd-num">{{ String(getTimeLeft(seckillItems[0]?.endTime).seconds).padStart(2, '0') }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 秒杀商品列表 ========== -->
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
          <div class="rank-badge" :class="'rank-' + (index + 1)">
            <el-icon v-if="index === 0"><Trophy /></el-icon>
            <el-icon v-else-if="index === 1"><Medal /></el-icon>
            <el-icon v-else><Star /></el-icon>
            {{ index === 0 ? 'TOP1' : index === 1 ? 'TOP2' : 'TOP3' }}
          </div>

          <div class="card-image">
            <div class="placeholder-img" :style="{ background: `linear-gradient(135deg, hsl(${item.skuId * 47 % 360}, 55%, 88%), hsl(${item.skuId * 47 % 360 + 30}, 55%, 80%))` }">
              <svg class="placeholder-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="12" y="16" width="40" height="32" rx="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
                <path d="M16 40 L24 30 L30 36 L42 22 L48 30" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.4"/>
                <circle cx="22" cy="26" r="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
              </svg>
            </div>
            <div class="stock-progress">
              <div class="stock-fill" :style="{ width: getStockPercent(item) + '%' }"></div>
              <span class="stock-text">已抢 {{ getStockPercent(item) }}%</span>
            </div>
          </div>

          <div class="card-body">
            <div class="product-name ellipsis-2">{{ item.productName }}</div>
            <div class="product-spec">
              <el-icon><Tag /></el-icon> {{ item.specs }}
            </div>

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

            <div class="limit-info">
              <el-icon><Warning /></el-icon>
              限购 {{ item.purchaseLimit }} 件 · 剩余 {{ item.seckillStock }} 件
            </div>

            <el-button
              type="primary"
              size="large"
              class="buy-btn"
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
        <div class="empty-icon">⚡</div>
        <p class="empty-text">暂无秒杀活动</p>
        <p class="empty-sub">敬请期待下一场秒杀</p>
        <router-link to="/" class="empty-btn">去首页逛逛</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== 头部 ========== */
.seckill-hero {
  background: linear-gradient(135deg, #e1251b, #ff6700);
  padding: 0;
}
.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-12, 48px) var(--sp-4, 16px) var(--sp-12, 48px);
}
.hero-left { color: #fff; }
.hero-title {
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: var(--sp-3, 12px);
}
.hero-sub {
  font-size: 15px;
  opacity: 0.85;
  margin-top: var(--sp-2, 8px);
}
.hero-tags {
  display: flex;
  gap: var(--sp-2, 8px);
  margin-top: var(--sp-4, 16px);
}
.hero-tags .tag { background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); }

.countdown-box {
  background: rgba(255,255,255,0.12);
  border-radius: var(--jd-radius, 12px);
  padding: var(--sp-5, 20px) var(--sp-6, 24px);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.2);
  text-align: center;
  color: #fff;
}
.countdown-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  opacity: 0.85;
  margin-bottom: var(--sp-3, 12px);
}
.countdown-display {
  display: flex;
  align-items: center;
  gap: 6px;
}
.cd-num {
  display: inline-block;
  background: rgba(0,0,0,0.2);
  padding: 8px 14px;
  border-radius: var(--jd-radius-sm, 8px);
  font-size: 24px;
  font-weight: 700;
  min-width: 52px;
  font-variant-numeric: tabular-nums;
}
.cd-sep {
  font-size: 24px;
  font-weight: 700;
  opacity: 0.7;
}

/* ========== 内容区 ========== */
.seckill-content { padding: var(--sp-8, 32px) var(--sp-4, 16px); }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-6, 24px);
}
.section-title-group {
  display: flex;
  align-items: center;
  gap: var(--sp-3, 12px);
}
.title-icon-box {
  width: 32px;
  height: 32px;
  border-radius: var(--jd-radius-sm, 8px);
  background: linear-gradient(135deg, var(--jd-red, #e1251b), var(--jd-orange, #ff6700));
  display: flex;
  align-items: center;
  justify-content: center;
}
.section-title { font-size: 20px; font-weight: 700; }
.title-line {
  width: 32px;
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg, var(--jd-red, #e1251b), transparent);
}
.section-desc { font-size: 13px; color: var(--jd-text-light, #999); display: flex; align-items: center; gap: 4px; }

/* ========== 商品网格 ========== */
.seckill-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--sp-5, 20px);
}
.seckill-card {
  position: relative;
  border-radius: var(--jd-radius, 12px);
  overflow: visible;
  background: #fff;
  box-shadow: var(--jd-shadow, 0 2px 12px rgba(0,0,0,0.08));
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.seckill-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--jd-shadow-hover, 0 8px 24px rgba(0,0,0,0.12));
}

.rank-badge {
  position: absolute;
  top: -1px;
  right: 16px;
  padding: 5px 14px;
  border-radius: 0 0 var(--jd-radius-sm, 8px) var(--jd-radius-sm, 8px);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 1;
}
.rank-1 { background: var(--jd-red, #e1251b); }
.rank-2 { background: var(--jd-orange, #ff6700); }
.rank-3 { background: #f5a623; }

.card-image { position: relative; padding-top: 100%; border-radius: var(--jd-radius, 12px) var(--jd-radius, 12px) 0 0; overflow: hidden; }
.placeholder-img {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.4s;
}
.seckill-card:hover .placeholder-img { transform: scale(1.04); }
.placeholder-svg { width: 56px; height: 56px; color: rgba(255,255,255,0.6); }

.stock-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 28px;
  background: rgba(0,0,0,0.55);
  display: flex;
  align-items: center;
  justify-content: center;
}
.stock-fill {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  background: linear-gradient(90deg, var(--jd-red, #e1251b), var(--jd-orange, #ff6700));
  transition: width 0.8s ease;
}
.stock-text { position: relative; z-index: 1; color: #fff; font-size: 12px; font-weight: 500; }

.card-body { padding: var(--sp-4, 16px); }
.product-name { font-size: 14px; font-weight: 500; margin-bottom: var(--sp-1, 4px); line-height: 1.4; height: 40px; }
.product-spec { font-size: 12px; color: var(--jd-text-light, #999); margin-bottom: var(--sp-3, 12px); display: flex; align-items: center; gap: 4px; }

.price-section { margin-bottom: var(--sp-2, 8px); }
.price-row { display: flex; align-items: baseline; gap: var(--sp-2, 8px); }
.seckill-price { color: var(--jd-red, #e1251b); }
.price-symbol { font-size: 14px; }
.price-num { font-size: 24px; font-weight: 700; }
.normal-price { font-size: 13px; color: var(--jd-text-muted, #bbb); text-decoration: line-through; }
.discount-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--jd-orange, #ff6700);
  background: var(--jd-red-light, #fff0f0);
  padding: 3px 10px;
  border-radius: var(--jd-radius-pill, 999px);
  margin-top: 4px;
  border: 1px solid #ffe0d0;
}

.limit-info {
  font-size: 12px;
  color: var(--jd-text-light, #999);
  margin-bottom: var(--sp-3, 12px);
  display: flex;
  align-items: center;
  gap: 4px;
}

.buy-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
  border-radius: var(--jd-radius-sm, 8px);
  font-weight: 600;
  letter-spacing: 1px;
}

/* ========== 空状态 ========== */
.empty-state { text-align: center; padding: var(--sp-12, 48px) 0; }
.empty-icon { font-size: 48px; margin-bottom: var(--sp-4, 16px); }
.empty-text { font-size: 18px; color: var(--jd-text-secondary, #666); font-weight: 600; margin-bottom: var(--sp-2, 8px); }
.empty-sub { font-size: 14px; color: var(--jd-text-light, #999); margin-bottom: var(--sp-6, 24px); }
.empty-btn {
  display: inline-block;
  padding: 10px 28px;
  background: var(--jd-red, #e1251b);
  color: #fff;
  border-radius: var(--jd-radius-sm, 8px);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.3s;
}
.empty-btn:hover { background: var(--jd-red-hover, #c81623); color: #fff; }

@media (max-width: 1200px) { .seckill-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) { .seckill-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
