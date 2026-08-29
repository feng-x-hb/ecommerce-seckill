<script setup lang="ts">
/**
 * 订单列表页（OrderListView.vue）
 * 京东风格：状态标签筛选 + 订单卡片列表 + 分页
 */
import { ref, onMounted } from 'vue'
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
  { label: '全部', value: undefined },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已发货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: '待支付', color: '#e1251b' },
  1: { text: '已支付', color: '#2baa6e' },
  2: { text: '已发货', color: '#ff6700' },
  3: { text: '已完成', color: '#999' },
  4: { text: '已取消', color: '#ccc' },
  5: { text: '已关闭', color: '#ccc' }
}

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
    <h2 class="page-title">我的订单</h2>

    <!-- 状态筛选标签 -->
    <div class="status-tabs card">
      <div
        v-for="tab in statusTabs"
        :key="tab.label"
        class="tab-item"
        :class="{ active: activeStatus === tab.value }"
        @click="switchStatus(tab.value)"
      >{{ tab.label }}</div>
    </div>

    <!-- 订单列表 -->
    <div v-loading="loading" class="order-list">
      <div v-if="!loading && orders.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无订单</p>
      </div>

      <div v-for="order in orders" :key="order.id" class="order-card card">
        <div class="order-header">
          <div class="order-no">订单号：{{ order.orderNo }}</div>
          <div class="order-status" :style="{ color: statusMap[order.status]?.color }">
            {{ statusMap[order.status]?.text }}
          </div>
        </div>
        <div class="order-body" @click="router.push(`/order/${order.orderNo}`)">
          <div class="order-info">
            <div class="order-time">下单时间：{{ order.createdAt }}</div>
            <div class="order-address">收货人：{{ order.receiverName }} {{ order.receiverPhone }}</div>
          </div>
          <div class="order-amount">
            <span class="order-label">实付：</span>
            <span class="price price-lg">¥{{ order.payAmount.toFixed(2) }}</span>
          </div>
        </div>
        <div class="order-footer">
          <el-button size="small" @click="router.push(`/order/${order.orderNo}`)">查看详情</el-button>
          <el-button v-if="order.status === 0" type="primary" size="small" @click="handlePay(order.orderNo)">立即支付</el-button>
          <el-button v-if="order.status === 0" size="small" @click="handleCancel(order.orderNo)">取消订单</el-button>
        </div>
      </div>
    </div>

    <div class="pagination" v-if="total > size">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="(p: number) => { page = p; fetchOrders() }" />
    </div>
  </div>
</template>

<style scoped>
.orders-page { padding: 20px 15px; }
.page-title { font-size: 22px; margin-bottom: 20px; font-weight: bold; }

.status-tabs {
  display: flex;
  gap: 0;
  padding: 0;
  margin-bottom: 20px;
  overflow: hidden;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 14px 0;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tab-item:hover { color: var(--jd-red); }
.tab-item.active { color: var(--jd-red); border-bottom-color: var(--jd-red); font-weight: bold; }

.order-card { margin-bottom: 16px; }
.order-header {
  display: flex;
  justify-content: space-between;
  padding: 14px 20px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.order-no { font-size: 13px; color: #999; }
.order-status { font-size: 14px; font-weight: bold; }

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  cursor: pointer;
}
.order-body:hover { background: #fafafa; }
.order-time, .order-address { font-size: 13px; color: #666; margin-bottom: 4px; }

.order-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
}

.empty-state { text-align: center; padding: 60px 0; }
.empty-icon { font-size: 50px; margin-bottom: 12px; }

.pagination { display: flex; justify-content: center; padding: 20px 0; }
</style>
