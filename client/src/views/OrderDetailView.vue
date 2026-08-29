<script setup lang="ts">
/**
 * 订单详情页（OrderDetailView.vue）
 * 京东风格：订单状态 + 商品快照 + 收货信息 + 操作按钮
 */
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder, cancelOrder } from '@/api/order'
import type { OrderDetail } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref<OrderDetail | null>(null)
const loading = ref(true)

const statusMap: Record<number, { text: string; color: string; icon: string }> = {
  0: { text: '待支付', color: '#e1251b', icon: '⏳' },
  1: { text: '已支付', color: '#2baa6e', icon: '✅' },
  2: { text: '已发货', color: '#ff6700', icon: '🚚' },
  3: { text: '已完成', color: '#999', icon: '📦' },
  4: { text: '已取消', color: '#ccc', icon: '❌' },
  5: { text: '已关闭', color: '#ccc', icon: '🔒' }
}

async function fetchDetail() {
  loading.value = true
  try {
    const res: any = await getOrderDetail(route.params.orderNo as string)
    order.value = res.data
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  await ElMessageBox.confirm('确认模拟支付？', '提示', { type: 'info' })
  await payOrder(route.params.orderNo as string)
  ElMessage.success('支付成功')
  fetchDetail()
}

async function handleCancel() {
  await ElMessageBox.confirm('确认取消订单？库存将恢复', '提示', { type: 'warning' })
  await cancelOrder(route.params.orderNo as string)
  ElMessage.success('已取消')
  fetchDetail()
}

onMounted(fetchDetail)
</script>

<template>
  <div class="order-detail-page container" v-loading="loading">
    <template v-if="order">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <router-link to="/orders">我的订单</router-link>
        <span class="sep">></span>
        <span>订单详情</span>
      </div>

      <!-- 订单状态卡片 -->
      <div class="status-card card">
        <div class="status-icon" :style="{ color: statusMap[order.status]?.color }">
          {{ statusMap[order.status]?.icon }}
        </div>
        <div class="status-info">
          <div class="status-text" :style="{ color: statusMap[order.status]?.color }">
            {{ statusMap[order.status]?.text }}
          </div>
          <div class="status-hint" v-if="order.status === 0">请在30分钟内完成支付</div>
          <div class="status-hint" v-else-if="order.payTime">支付时间：{{ order.payTime }}</div>
        </div>
        <div class="status-actions" v-if="order.status === 0">
          <el-button type="primary" size="large" @click="handlePay">立即支付</el-button>
          <el-button size="large" @click="handleCancel">取消订单</el-button>
        </div>
      </div>

      <!-- 收货信息 -->
      <div class="section card">
        <h3 class="section-title">收货信息</h3>
        <div class="info-grid">
          <div><span class="label">收货人：</span>{{ order.receiverName }}</div>
          <div><span class="label">联系电话：</span>{{ order.receiverPhone }}</div>
          <div class="full"><span class="label">收货地址：</span>{{ order.receiverAddress }}</div>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="section card">
        <h3 class="section-title">商品清单</h3>
        <div v-for="(item, index) in order.items" :key="index" class="detail-item">
          <div class="item-image" :style="{ background: `hsl(${item.skuId * 47 % 360}, 60%, 85%)` }">
            <span>{{ item.productName.charAt(0) }}</span>
          </div>
          <div class="item-info">
            <div class="item-name">{{ item.productName }}</div>
            <div class="item-spec">{{ item.specDesc }}</div>
          </div>
          <div class="item-price price">¥{{ item.price.toFixed(2) }}</div>
          <div class="item-qty">x{{ item.quantity }}</div>
          <div class="item-subtotal price">¥{{ item.subTotal.toFixed(2) }}</div>
        </div>
      </div>

      <!-- 订单金额 -->
      <div class="section card">
        <h3 class="section-title">订单金额</h3>
        <div class="amount-row">
          <span>商品总价</span>
          <span>¥{{ order.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="amount-row" v-if="order.discountAmount > 0">
          <span>优惠金额</span>
          <span class="price">-¥{{ order.discountAmount.toFixed(2) }}</span>
        </div>
        <div class="amount-row total">
          <span>实付金额</span>
          <span class="price price-lg">¥{{ order.payAmount.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="section card">
        <h3 class="section-title">订单信息</h3>
        <div class="info-grid">
          <div><span class="label">订单号：</span>{{ order.orderNo }}</div>
          <div><span class="label">创建时间：</span>{{ order.createdAt }}</div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.order-detail-page { padding: 20px 15px; max-width: 900px; }

.breadcrumb { font-size: 13px; color: #999; margin-bottom: 16px; }
.breadcrumb a { color: #666; }
.breadcrumb a:hover { color: var(--jd-red); }
.sep { margin: 0 6px; color: #ccc; }

.status-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 30px;
  margin-bottom: 20px;
}
.status-icon { font-size: 48px; }
.status-text { font-size: 22px; font-weight: bold; margin-bottom: 4px; }
.status-hint { font-size: 13px; color: #999; }
.status-actions { margin-left: auto; display: flex; gap: 10px; }

.section { margin-bottom: 20px; padding: 24px; }
.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  font-size: 14px;
}
.info-grid .full { grid-column: 1 / -1; }
.label { color: #999; }

.detail-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}
.detail-item:last-child { border-bottom: none; }
.item-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: rgba(255,255,255,0.8);
  flex-shrink: 0;
}
.item-info { flex: 1; }
.item-name { font-size: 14px; margin-bottom: 4px; }
.item-spec { font-size: 12px; color: #999; }
.item-price, .item-subtotal { width: 100px; text-align: right; }
.item-qty { width: 60px; text-align: center; color: #999; }

.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}
.amount-row.total {
  border-top: 1px solid #eee;
  margin-top: 8px;
  padding-top: 12px;
  font-weight: bold;
}
</style>
