<script setup lang="ts">
/**
 * 下单页（CheckoutView.vue）
 * 京东风格：收货信息 + 商品确认 + 优惠券选择 + 提交订单
 */
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { createOrder } from '@/api/order'
import { getMyCoupons } from '@/api/coupon'
import type { CartItem } from '@/types'
import { ElMessage } from 'element-plus'

const router = useRouter()
const items = ref<CartItem[]>([])
const receiverName = ref('')
const receiverPhone = ref('')
const receiverAddress = ref('')
const submitting = ref(false)

const totalPrice = ref(0)

// 优惠券相关
const myCoupons = ref<any[]>([])
const selectedCouponId = ref<number | null>(null)
const showCouponPicker = ref(false)

const selectedCoupon = computed(() => {
  return myCoupons.value.find(c => c.id === selectedCouponId.value) || null
})

const discountAmount = computed(() => {
  if (!selectedCoupon.value) return 0
  const minAmount = selectedCoupon.value.minAmount || 0
  if (totalPrice.value < minAmount) return 0
  return Math.min(selectedCoupon.value.discount, totalPrice.value)
})

const payAmount = computed(() => {
  return Math.max(0, totalPrice.value - discountAmount.value)
})

onMounted(async () => {
  const data = sessionStorage.getItem('checkoutItems')
  if (!data) { router.push('/cart'); return }
  items.value = JSON.parse(data)
  totalPrice.value = items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)

  // 加载可用优惠券
  try {
    const res: any = await getMyCoupons({ page: 1, size: 50 })
    // 只显示未使用且在有效期内的优惠券
    const now = new Date().toISOString().slice(0, 10)
    myCoupons.value = (res.data?.list || []).filter((c: any) =>
      c.status === 0 && c.endDate >= now
    )
  } catch { /* ignore */ }
})

function selectCoupon(coupon: any) {
  if (totalPrice.value < coupon.minAmount) {
    ElMessage.warning(`未达到最低消费 ¥${coupon.minAmount}`)
    return
  }
  selectedCouponId.value = coupon.id
  showCouponPicker.value = false
}

function clearCoupon() {
  selectedCouponId.value = null
}

async function handleSubmit() {
  if (!receiverName.value.trim()) return ElMessage.warning('请输入收货人姓名')
  if (!receiverPhone.value.trim()) return ElMessage.warning('请输入收货电话')
  if (!receiverAddress.value.trim()) return ElMessage.warning('请输入收货地址')

  submitting.value = true
  try {
    const res: any = await createOrder({
      skuItems: items.value.map(item => ({ skuId: item.skuId, quantity: item.quantity })),
      receiverName: receiverName.value,
      receiverPhone: receiverPhone.value,
      receiverAddress: receiverAddress.value,
      couponId: selectedCouponId.value
    })
    sessionStorage.removeItem('checkoutItems')
    ElMessage.success('下单成功')
    router.push(`/order/${res.data.orderNo}`)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="checkout-page container">
    <h2 class="page-title">确认订单</h2>

    <!-- 收货信息 -->
    <div class="section card">
      <h3 class="section-title">收货信息</h3>
      <div class="form-grid">
        <div class="form-item">
          <label>收货人</label>
          <el-input v-model="receiverName" placeholder="请输入收货人姓名" />
        </div>
        <div class="form-item">
          <label>联系电话</label>
          <el-input v-model="receiverPhone" placeholder="请输入手机号" />
        </div>
        <div class="form-item full">
          <label>收货地址</label>
          <el-input v-model="receiverAddress" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </div>
      </div>
    </div>

    <!-- 商品清单 -->
    <div class="section card">
      <h3 class="section-title">商品清单</h3>
      <div v-for="item in items" :key="item.id" class="checkout-item">
        <div class="item-image" :style="{ background: `linear-gradient(135deg, hsl(${item.productId * 47 % 360}, 55%, 88%), hsl(${item.productId * 47 % 360 + 30}, 55%, 80%))` }">
          <svg class="item-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="12" y="16" width="40" height="32" rx="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
            <path d="M16 40 L24 30 L30 36 L42 22 L48 30" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.4"/>
            <circle cx="22" cy="26" r="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
          </svg>
        </div>
        <div class="item-info">
          <div class="item-name ellipsis">{{ item.productName }}</div>
          <div class="item-spec">{{ item.specs }}</div>
        </div>
        <div class="item-price price">¥{{ item.price.toFixed(2) }}</div>
        <div class="item-qty">x{{ item.quantity }}</div>
        <div class="item-subtotal price">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
      </div>
    </div>

    <!-- 优惠券 -->
    <div class="section card coupon-section">
      <h3 class="section-title">优惠券</h3>
      <div v-if="selectedCoupon" class="coupon-selected">
        <div class="coupon-info">
          <span class="coupon-name">{{ selectedCoupon.name }}</span>
          <span class="coupon-detail">满{{ selectedCoupon.minAmount }}减{{ selectedCoupon.discount }}</span>
        </div>
        <div class="coupon-action">
          <span class="coupon-discount">-¥{{ discountAmount.toFixed(2) }}</span>
          <el-button text type="danger" size="small" @click="clearCoupon">取消</el-button>
        </div>
      </div>
      <div v-else class="coupon-empty" @click="showCouponPicker = true">
        <span v-if="myCoupons.length">选择优惠券（{{ myCoupons.length }}张可用）</span>
        <span v-else>暂无可用优惠券</span>
        <span class="arrow">›</span>
      </div>
    </div>

    <!-- 优惠券选择弹窗 -->
    <el-dialog v-model="showCouponPicker" title="选择优惠券" width="500px" :close-on-click-modal="true">
      <div v-if="!myCoupons.length" class="no-coupon">暂无可用优惠券</div>
      <div v-for="c in myCoupons" :key="c.id" class="coupon-card" :class="{ active: selectedCouponId === c.id, disabled: totalPrice < c.minAmount }" @click="selectCoupon(c)">
        <div class="coupon-left">
          <div class="coupon-amount">
            <span class="coupon-symbol">¥</span>
            <span class="coupon-value">{{ c.discount }}</span>
          </div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ c.name }}</div>
          <div class="coupon-condition">满{{ c.minAmount }}可用</div>
          <div class="coupon-expire">有效期至 {{ c.endDate }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 提交栏 -->
    <div class="submit-bar card">
      <div class="submit-info">
        <div class="submit-total">
          共 <strong>{{ items.reduce((s, i) => s + i.quantity, 0) }}</strong> 件商品
        </div>
        <div v-if="discountAmount > 0" class="submit-discount">
          优惠：<span class="price">-¥{{ discountAmount.toFixed(2) }}</span>
        </div>
        <div class="submit-amount">
          应付总额：<span class="price price-lg">¥{{ payAmount.toFixed(2) }}</span>
        </div>
      </div>
      <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
        提交订单
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.checkout-page { padding: 20px 15px; }
.page-title { font-size: 22px; margin-bottom: 20px; font-weight: bold; }

.section { margin-bottom: 20px; padding: 24px; }
.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 24px;
}
.form-item.full { grid-column: 1 / -1; }
.form-item label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

.checkout-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}
.checkout-item:last-child { border-bottom: none; }
.item-image {
  width: 60px;
  height: 60px;
  border-radius: var(--jd-radius-sm, 8px);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.item-svg { width: 36px; height: 36px; color: rgba(255,255,255,0.6); }
.item-info { flex: 1; }
.item-name { font-size: 14px; margin-bottom: 4px; }
.item-spec { font-size: 12px; color: #999; }
.item-price { width: 100px; text-align: right; }
.item-qty { width: 60px; text-align: center; color: #999; }
.item-subtotal { width: 100px; text-align: right; }

/* 优惠券 */
.coupon-section { cursor: pointer; }
.coupon-selected {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff8f0;
  padding: 14px 16px;
  border-radius: 8px;
  border: 1px dashed #ff9900;
}
.coupon-info { display: flex; flex-direction: column; gap: 4px; }
.coupon-name { font-size: 14px; font-weight: 500; color: #333; }
.coupon-detail { font-size: 12px; color: #999; }
.coupon-action { display: flex; align-items: center; gap: 12px; }
.coupon-discount { color: #e1251b; font-weight: 600; font-size: 16px; }

.coupon-empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
}
.coupon-empty:hover { background: #f0f0f0; }
.arrow { font-size: 20px; color: #ccc; }

/* 弹窗内优惠券卡片 */
.no-coupon { text-align: center; color: #999; padding: 24px; }
.coupon-card {
  display: flex;
  margin-bottom: 12px;
  border: 2px solid #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}
.coupon-card:hover { border-color: #ff9900; }
.coupon-card.active { border-color: #e1251b; background: #fff5f5; }
.coupon-card.disabled { opacity: 0.5; cursor: not-allowed; }

.coupon-left {
  width: 120px;
  background: linear-gradient(135deg, #ff9900, #ff6600);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.coupon-card.active .coupon-left {
  background: linear-gradient(135deg, #e1251b, #c41a1a);
}
.coupon-symbol { font-size: 14px; margin-right: 2px; }
.coupon-value { font-size: 28px; font-weight: bold; }

.coupon-right {
  flex: 1;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}
.coupon-right .coupon-name { font-size: 15px; font-weight: 600; color: #333; }
.coupon-condition { font-size: 12px; color: #999; }
.coupon-expire { font-size: 12px; color: #bbb; }

/* 提交栏 */
.submit-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  position: sticky;
  bottom: 0;
}
.submit-info { display: flex; align-items: baseline; gap: 20px; }
.submit-total { font-size: 14px; color: #666; }
.submit-discount { font-size: 13px; color: #e1251b; }
.submit-amount { font-size: 14px; color: #333; }
</style>
