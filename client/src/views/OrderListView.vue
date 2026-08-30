<script setup lang="ts">
/**
 * 订单列表页 - 华丽版 v2
 * 统计概览 + 状态标签筛选 + 渐变状态色条 + 精美空状态
 */
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList, cancelOrder, payOrder } from '@/api/order'
import type { Order } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const orders = ref<Order[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const activeStatus = ref<number | undefined>()
const loading = ref(false)

const statusTabs = [
  { label: '全部', value: undefined, icon: '📋' },
  { label: '待支付', value: 0, icon: '💳' },
  { label: '已支付', value: 1, icon: '✅' },
  { label: '已发货', value: 2, icon: '🚚' },
  { label: '已完成', value: 3, icon: '📦' },
  { label: '已取消', value: 4, icon: '❌' }
]

const statusMap: Record<number, { text: string; color: string; bg: string }> = {
  0: { text: '待支付', color: '#e1251b', bg: 'linear-gradient(135deg, #e1251b, #ff4e3a)' },
  1: { text: '已支付', color: '#2baa6e', bg: 'linear-gradient(135deg, #2baa6e, #34d058)' },
  2: { text: '已发货', color: '#ff6700', bg: 'linear-gradient(135deg, #ff6700, #ff9500)' },
  3: { text: '已完成', color: '#999', bg: 'linear-gradient(135deg, #999, #bbb)' },
  4: { text: '已取消', color: '#ccc', bg: 'linear-gradient(135deg, #ccc, #ddd)' },
  5: { text: '已关闭', color: '#ccc', bg: 'linear-gradient(135deg, #ccc, #ddd)' }
}

// 统计数据
const stats = computed(() => {
  const all = orders.value
  return {
    total: total.value,
    unpaid: all.filter(o => o.status === 0).length,
    paid: all.filter(o => o.status === 1).length,
    shipped: all.filter(o => o.status === 2).length,
    completed: all.filter(o => o.status === 3).length
  }
})

async function fetchOrders() {
  loading.value = true
  try {
    const res: any = await getOrderList({ page: page.value, size: size.value, status: activeStatus.value })
    orders.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function switchStatus(val: number | undefined) {
  activeStatus.value = val
  page.value = 1
  fetchOrders()
}

async function handlePay(orderNo: string) {
  await ElMessageBox.confirm('确认模拟支付？', '提示', { type: 'info' })
  await payOrder(orderNo)
  ElMessage.success('支付成功')
  fetchOrders()
}

async function handleCancel(orderNo: string) {
  await ElMessageBox.confirm('确认取消订单？库存将恢复', '提示', { type: 'warning' })
  await cancelOrder(orderNo)
  ElMessage.success('已取消')
  fetchOrders()
}

onMounted(fetchOrders)
</script>

<template>
  <div class="orders-page container">
    <!-- ========== 页面标题 ========== -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的订单</h2>
        <span class="page-sub">管理您的所有订单</span>
      </div>
    </div>

    <!-- ========== 统计概览 ========== -->
    <div class="stats-bar">
      <div class="stat-card">
        <div class="stat-icon" style="background:linear-gradient(135deg,#e1251b,#ff4e3a)">
          <el-icon :size="20" color="#fff"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-num">{{ stats.total }}</div>
          <div class="stat-label">全部订单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:linear-gradient(135deg,#ff6700,#ff9500)">
          <el-icon :size="20" color="#fff"><Wallet /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-num">{{ stats.unpaid }}</div>
          <div class="stat-label">待支付</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:linear-gradient(135deg,#2baa6e,#34d058)">
          <el-icon :size="20" color="#fff"><Van /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-num">{{ stats.shipped }}</div>
          <div class="stat-label">已发货</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:linear-gradient(135deg,#667eea,#764ba2)">
          <el-icon :size="20" color="#fff"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-num">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <!-- ========== 状态筛选标签 ========== -->
    <div class="status-tabs">
      <div
        v-for="tab in statusTabs"
        :key="tab.label"
        class="tab-item"
        :class="{ active: activeStatus === tab.value }"
        @click="switchStatus(tab.value)"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        {{ tab.label }}
      </div>
    </div>

    <!-- ========== 订单列表 ========== -->
    <div v-loading="loading" class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <!-- 状态色条 -->
        <div class="status-bar" :style="{ background: statusMap[order.status]?.bg }"></div>

        <div class="order-header">
          <div class="order-no">
            <el-icon><Document /></el-icon>
            订单号：{{ order.orderNo }}
          </div>
          <div class="order-status-badge" :style="{ background: statusMap[order.status]?.bg }">
            {{ statusMap[order.status]?.text }}
          </div>
        </div>

        <div class="order-body" @click="router.push(`/order/${order.orderNo}`)">
          <div class="order-info">
            <div class="info-row">
              <el-icon><Clock /></el-icon>
              <span>下单时间：{{ order.createdAt }}</span>
            </div>
            <div class="info-row">
              <el-icon><User /></el-icon>
              <span>收货人：{{ order.receiverName }} {{ order.receiverPhone }}</span>
            </div>
          </div>
          <div class="order-amount">
            <span class="amount-label">实付金额</span>
            <span class="price price-lg">¥{{ order.payAmount.toFixed(2) }}</span>
          </div>
        </div>

        <div class="order-footer">
          <el-button size="small" class="footer-btn" @click="router.push(`/order/${order.orderNo}`)">
            <el-icon><View /></el-icon> 查看详情
          </el-button>
          <el-button v-if="order.status === 0" type="primary" size="small" class="footer-btn pay-btn" @click="handlePay(order.orderNo)">
            <el-icon><Wallet /></el-icon> 立即支付
          </el-button>
          <el-button v-if="order.status === 0" size="small" class="footer-btn cancel-btn" @click="handleCancel(order.orderNo)">
            <el-icon><CircleClose /></el-icon> 取消订单
          </el-button>
        </div>
      </div>
    </div>

    <!-- ========== 空状态 ========== -->
    <div v-if="!loading && orders.length === 0" class="empty-state">
      <div class="empty-illustration">
        <div class="empty-box">
          <div class="box-lid"></div>
          <div class="box-body"></div>
          <div class="box-star">📦</div>
        </div>
        <div class="empty-sparkles">
          <span class="sparkle" v-for="i in 6" :key="i" :style="{ animationDelay: i * 0.3 + 's' }">✦</span>
        </div>
      </div>
      <p class="empty-text">暂无订单</p>
      <p class="empty-sub">快去挑选心仪的商品吧</p>
      <router-link to="/" class="empty-btn">去首页逛逛</router-link>
    </div>

    <div class="pagination" v-if="total > size">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="(p: number) => { page = p; fetchOrders() }" />
    </div>
  </div>
</template>

<style scoped>
.orders-page { padding: 20px 15px; }

/* ========== 页面标题 ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title { font-size: 24px; font-weight: bold; }
.page-sub { font-size: 13px; color: #999; margin-top: 4px; display: block; }

/* ========== 统计概览 ========== */
.stats-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-num { font-size: 22px; font-weight: bold; color: #333; }
.stat-label { font-size: 12px; color: #999; margin-top: 2px; }

/* ========== 状态筛选 ========== */
.status-tabs {
  display: flex;
  gap: 0;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 14px 0;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.tab-item:hover { color: var(--jd-red); background: #fff8f8; }
.tab-item.active {
  color: var(--jd-red);
  border-bottom-color: var(--jd-red);
  font-weight: bold;
  background: #fff5f5;
}
.tab-icon { font-size: 16px; }

/* ========== 订单卡片 ========== */
.order-card {
  position: relative;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
  overflow: hidden;
  transition: all 0.35s;
  animation: cardSlideIn 0.5s ease backwards;
}
.order-card:nth-child(1) { animation-delay: 0s; }
.order-card:nth-child(2) { animation-delay: 0.08s; }
.order-card:nth-child(3) { animation-delay: 0.16s; }
.order-card:nth-child(4) { animation-delay: 0.24s; }
.order-card:nth-child(5) { animation-delay: 0.32s; }
@keyframes cardSlideIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.order-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  border-radius: 12px;
  box-shadow: 0 0 20px rgba(225,37,27,0.1);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
  z-index: 0;
}
.order-card:hover::before { opacity: 1; }
.order-card:hover {
  box-shadow: 0 8px 28px rgba(0,0,0,0.08);
  transform: translateY(-3px);
}
.status-bar {
  height: 4px;
  width: 100%;
  position: relative;
  overflow: hidden;
}
.status-bar::after {
  content: '';
  position: absolute;
  top: 0; left: -100%; right: 0; bottom: 0;
  width: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.6), transparent);
  animation: statusShine 3s ease-in-out infinite;
}
@keyframes statusShine {
  0% { left: -100%; }
  50% { left: 100%; }
  100% { left: 100%; }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.order-no {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 6px;
}
.order-status-badge {
  padding: 4px 14px;
  border-radius: 20px;
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  animation: badgePulse 2s ease-in-out infinite;
}
@keyframes badgePulse {
  0%, 100% { box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
  50% { box-shadow: 0 2px 16px rgba(0,0,0,0.2); }
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  cursor: pointer;
  transition: background 0.2s;
}
.order-body:hover { background: #fafafa; }
.info-row {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.order-amount { text-align: right; }
.amount-label { font-size: 12px; color: #999; display: block; margin-bottom: 4px; }

.order-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}
.footer-btn {
  border-radius: 20px;
  font-size: 13px;
  transition: all 0.3s;
}
.pay-btn {
  background: linear-gradient(135deg, #e1251b, #ff4e3a) !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(225,37,27,0.2);
  position: relative;
  overflow: hidden;
}
.pay-btn::after {
  content: '';
  position: absolute;
  top: 0; left: -100%; right: 0; bottom: 0;
  width: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  transition: none;
}
.pay-btn:hover::after {
  animation: btnShine 0.6s ease forwards;
}
@keyframes btnShine {
  0% { left: -100%; }
  100% { left: 100%; }
}
.pay-btn:hover { box-shadow: 0 4px 16px rgba(225,37,27,0.3); transform: translateY(-1px); }
.cancel-btn:hover { color: #e1251b !important; border-color: #e1251b !important; }

/* ========== 空状态 ========== */
.empty-state { text-align: center; padding: 60px 0; }
.empty-illustration {
  position: relative;
  width: 140px;
  height: 140px;
  margin: 0 auto 24px;
}
.empty-box { position: relative; width: 100%; height: 100%; }
.box-lid {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  height: 24px;
  background: linear-gradient(135deg, #ff9500, #ffb340);
  border-radius: 8px 8px 0 0;
  z-index: 2;
}
.box-body {
  position: absolute;
  top: 44px;
  left: 20px;
  right: 20px;
  bottom: 20px;
  background: linear-gradient(135deg, #ff6700, #ff9500);
  border-radius: 0 0 8px 8px;
}
.box-star {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 48px;
  z-index: 3;
  animation: float 3s ease-in-out infinite;
}
.empty-sparkles {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  pointer-events: none;
}
.sparkle {
  position: absolute;
  color: #f5a623;
  font-size: 14px;
  animation: sparkleFloat 2s ease-in-out infinite;
}
.sparkle:nth-child(1) { top: 10%; left: 10%; }
.sparkle:nth-child(2) { top: 5%; right: 15%; }
.sparkle:nth-child(3) { bottom: 15%; left: 5%; }
.sparkle:nth-child(4) { top: 30%; right: 5%; }
.sparkle:nth-child(5) { bottom: 5%; right: 20%; }
.sparkle:nth-child(6) { top: 50%; left: 0; }
@keyframes sparkleFloat {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.6; }
  50% { transform: translateY(-8px) scale(1.2); opacity: 1; }
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

.pagination { display: flex; justify-content: center; padding: 20px 0; }
</style>
