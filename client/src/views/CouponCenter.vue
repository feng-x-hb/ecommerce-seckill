<script setup lang="ts">
/**
 * 优惠券中心（CouponCenter.vue）
 * 路由：/coupons
 * 功能：浏览可领取优惠券 + 我的优惠券
 */
import { ref, onMounted } from 'vue'
import { getCouponList, claimCoupon, getMyCoupons } from '@/api/coupon'
import { ElMessage } from 'element-plus'

const activeTab = ref<'available' | 'my'>('available')
const availableList = ref<any[]>([])
const myList = ref<any[]>([])
const loading = ref(false)
const claimedIds = ref<Set<number>>(new Set())

async function fetchAvailable() {
  loading.value = true
  try {
    const res: any = await getCouponList({ page: 1, size: 50 })
    availableList.value = res.data.list || []
  } finally { loading.value = false }
}

async function fetchMy() {
  loading.value = true
  try {
    const res: any = await getMyCoupons({ page: 1, size: 50 })
    myList.value = res.data.list || []
    myList.value.forEach((c: any) => claimedIds.value.add(c.templateId))
  } finally { loading.value = false }
}

async function handleClaim(templateId: number) {
  try {
    await claimCoupon(templateId)
    claimedIds.value.add(templateId)
    ElMessage.success('领取成功')
    fetchMy()
  } catch { /* error handled by interceptor */ }
}

function switchTab(tab: 'available' | 'my') {
  activeTab.value = tab
  if (tab === 'available') fetchAvailable()
  else fetchMy()
}

const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '可使用', color: '#52c41a' },
  1: { label: '已使用', color: '#999' },
  2: { label: '已过期', color: '#ccc' }
}

onMounted(() => { fetchMy(); fetchAvailable() })
</script>

<template>
  <div class="coupon-page">
    <div class="coupon-container container">
      <h1 class="page-title">🎫 优惠券中心</h1>

      <!-- Tab 切换 -->
      <div class="tab-bar">
        <div class="tab" :class="{ active: activeTab === 'available' }" @click="switchTab('available')">可领取</div>
        <div class="tab" :class="{ active: activeTab === 'my' }" @click="switchTab('my')">我的优惠券</div>
      </div>

      <div v-if="loading" class="loading-wrap">
        <el-icon class="is-loading" :size="32" color="#e1251b"><Loading /></el-icon>
      </div>

      <!-- 可领取列表 -->
      <template v-if="activeTab === 'available' && !loading">
        <div v-if="availableList.length === 0" class="empty-state">
          <div class="empty-icon">🎫</div>
          <div class="empty-text">暂无可领取的优惠券</div>
        </div>
        <div v-else class="coupon-grid">
          <div v-for="c in availableList" :key="c.id" class="coupon-card">
            <div class="coupon-left">
              <div class="coupon-amount">¥{{ c.discount }}</div>
              <div class="coupon-condition">满{{ c.minAmount }}可用</div>
            </div>
            <div class="coupon-mid">
              <div class="coupon-name">{{ c.name }}</div>
              <div class="coupon-date">{{ c.startDate }} ~ {{ c.endDate }}</div>
              <div class="coupon-remain">剩余 {{ c.remain }} 张</div>
            </div>
            <button v-if="claimedIds.has(c.id)" class="claim-btn claimed" disabled>已领取</button>
            <button v-else class="claim-btn" @click="handleClaim(c.id)">立即领取</button>
          </div>
        </div>
      </template>

      <!-- 我的优惠券 -->
      <template v-if="activeTab === 'my' && !loading">
        <div v-if="myList.length === 0" class="empty-state">
          <div class="empty-icon">📭</div>
          <div class="empty-text">暂无优惠券</div>
        </div>
        <div v-else class="coupon-grid">
          <div v-for="c in myList" :key="c.id" class="coupon-card" :class="{ used: c.status !== 0 }">
            <div class="coupon-left">
              <div class="coupon-amount">¥{{ c.discount }}</div>
              <div class="coupon-condition">满{{ c.minAmount }}可用</div>
            </div>
            <div class="coupon-mid">
              <div class="coupon-name">{{ c.name }}</div>
              <div class="coupon-date">有效期至 {{ c.endDate }}</div>
            </div>
            <div class="coupon-status" :style="{ color: statusMap[c.status]?.color }">
              {{ statusMap[c.status]?.label || '未知' }}
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.coupon-page { min-height: 100vh; background: var(--jd-bg); padding: 30px 0 60px; }
.coupon-container { max-width: 800px; }
.page-title { font-size: 24px; font-weight: 600; margin-bottom: 24px; }

.tab-bar {
  display: flex; gap: 0; background: #fff; border-radius: 12px; overflow: hidden;
  margin-bottom: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.tab {
  flex: 1; text-align: center; padding: 14px; cursor: pointer; font-size: 15px;
  color: #666; transition: all 0.3s; font-weight: 500;
}
.tab.active {
  background: linear-gradient(135deg, #e1251b, #ff6700); color: #fff;
}
.tab:hover:not(.active) { background: #f5f5f5; transform: scale(1.05); }

.loading-wrap { display: flex; justify-content: center; padding: 60px; }
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 60px; }
.empty-icon { font-size: 64px; margin-bottom: 12px; }
.empty-text { color: #999; font-size: 15px; }

.coupon-grid { display: flex; flex-direction: column; gap: 14px; }
.coupon-card {
  display: flex; align-items: center; background: #fff; border-radius: 12px;
  overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.05); transition: all 0.3s;
}
.coupon-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.coupon-card.used { opacity: 0.6; }

.coupon-left {
  background: linear-gradient(135deg, #e1251b, #ff6700); color: #fff;
  padding: 20px 24px; text-align: center; min-width: 120px;
}
.coupon-amount { font-size: 28px; font-weight: 700; }
.coupon-condition { font-size: 12px; opacity: 0.8; margin-top: 4px; }

.coupon-mid { flex: 1; padding: 16px 20px; }
.coupon-name { font-size: 15px; font-weight: 500; color: #333; }
.coupon-date { font-size: 12px; color: #999; margin-top: 6px; }
.coupon-remain { font-size: 12px; color: #ff6700; margin-top: 4px; }

.claim-btn {
  background: linear-gradient(135deg, #e1251b, #ff6700); color: #fff; border: none;
  border-radius: 20px; padding: 8px 24px; margin-right: 16px; cursor: pointer;
  font-size: 13px; font-weight: 500; transition: all 0.3s; white-space: nowrap;
}
.claim-btn:hover:not(.claimed) { transform: scale(1.05); box-shadow: 0 4px 12px rgba(225,37,27,0.3); }
.claim-btn.claimed {
  background: #e0e0e0; color: #999; cursor: not-allowed; transform: none;
  box-shadow: none;
}

.coupon-status { font-size: 13px; font-weight: 500; margin-right: 20px; white-space: nowrap; }
</style>
