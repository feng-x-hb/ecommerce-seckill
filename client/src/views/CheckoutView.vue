<script setup lang="ts">
/**
 * 下单页（CheckoutView.vue）
 * 每个商品独立选择优惠券，不可叠加
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

// 优惠券：每个商品独立选择
const myCoupons = ref<any[]>([])
const itemCouponMap = ref<Record<number, number | null>>({})  // skuId → couponId
const couponPickerTarget = ref<number | null>(null)  // 当前打开选券弹窗的 skuId

const totalOriginal = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

const totalDiscount = computed(() => {
  let total = 0
  for (const item of items.value) {
    const couponId = itemCouponMap.value[item.skuId]
    if (!couponId) continue
    const coupon = myCoupons.value.find(c => c.id === couponId)
    if (!coupon) continue
    const subTotal = item.price * item.quantity
    if (subTotal >= coupon.minAmount) {
      total += Math.min(coupon.discount, subTotal)
    }
  }
  return total
})

const payAmount = computed(() => Math.max(0, totalOriginal.value - totalDiscount.value))

function getItemDiscount(item: CartItem): number {
  const couponId = itemCouponMap.value[item.skuId]
  if (!couponId) return 0
  const coupon = myCoupons.value.find(c => c.id === couponId)
  if (!coupon) return 0
  const subTotal = item.price * item.quantity
  if (subTotal < coupon.minAmount) return 0
  return Math.min(coupon.discount, subTotal)
}

function getItemCoupon(item: CartItem): any | null {
  const couponId = itemCouponMap.value[item.skuId]
  if (!couponId) return null
  return myCoupons.value.find(c => c.id === couponId) || null
}

// 已被其他商品选中的优惠券 id 列表
const usedCouponIds = computed(() => {
  const ids: number[] = []
  for (const item of items.value) {
    const cid = itemCouponMap.value[item.skuId]
    if (cid) ids.push(cid)
  }
  return ids
})

function getCouponsForItem(item: CartItem) {
  const subTotal = item.price * item.quantity
  return myCoupons.value.filter(c => {
    if (usedCouponIds.value.includes(c.id) && itemCouponMap.value[item.skuId] !== c.id) return false
    return true
  }).map(c => ({
    ...c,
    disabled: subTotal < c.minAmount
  }))
}

function selectItemCoupon(item: CartItem, couponId: number | null) {
  itemCouponMap.value[item.skuId] = couponId
  couponPickerTarget.value = null
}

function clearItemCoupon(item: CartItem) {
  itemCouponMap.value[item.skuId] = null
}

onMounted(async () => {
  const data = sessionStorage.getItem('checkoutItems')
  if (!data) { router.push('/cart'); return }
  items.value = JSON.parse(data)
  // 初始化每个商品的券为空
  items.value.forEach(item => { itemCouponMap.value[item.skuId] = null })

  try {
    const res: any = await getMyCoupons({ page: 1, size: 50 })
    const now = new Date().toISOString().slice(0, 10)
    myCoupons.value = (res.data?.list || []).filter((c: any) =>
      c.status === 0 && c.endDate >= now
    )
  } catch { /* ignore */ }
})

async function handleSubmit() {
  if (!receiverName.value.trim()) return ElMessage.warning('请输入收货人姓名')
  if (!receiverPhone.value.trim()) return ElMessage.warning('请输入收货电话')
  if (!receiverAddress.value.trim()) return ElMessage.warning('请输入收货地址')

  submitting.value = true
  try {
    const itemCoupons: Record<string, number> = {}
    for (const item of items.value) {
      const cid = itemCouponMap.value[item.skuId]
      if (cid) itemCoupons[item.skuId] = cid
    }

    const res: any = await createOrder({
      skuItems: items.value.map(item => ({ skuId: item.skuId, quantity: item.quantity })),
      receiverName: receiverName.value,
      receiverPhone: receiverPhone.value,
      receiverAddress: receiverAddress.value,
      itemCoupons
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

    <!-- 商品清单 + 每商品选券 -->
    <div class="section card">
      <h3 class="section-title">商品清单</h3>
      <div v-for="item in items" :key="item.id" class="checkout-item">
        <div class="item-image" :style="{ background: `linear-gradient(135deg, hsl(${item.productId * 47 % 360}, 55%, 88%), hsl(${item.productId * 47 % 360 + 30}, 55%, 80%))` }">
          <svg class="item-svg" viewBox="0 0 64 64" fill="none">
            <rect x="12" y="16" width="40" height="32" rx="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
            <path d="M16 40 L24 30 L30 36 L42 22 L48 30" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"/>
            <circle cx="22" cy="26" r="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
          </svg>
        </div>
        <div class="item-info">
          <div class="item-name ellipsis">{{ item.productName }}</div>
          <div class="item-spec">{{ item.specs }}</div>
          <!-- 每商品优惠券选择 -->
          <div class="item-coupon-row">
            <div v-if="getItemCoupon(item)" class="item-coupon-active">
              <span class="item-coupon-tag">🎫 {{ getItemCoupon(item).name }}</span>
              <span class="item-coupon-discount">-¥{{ getItemDiscount(item).toFixed(0) }}</span>
              <el-button text type="danger" size="small" @click="clearItemCoupon(item)">取消</el-button>
            </div>
            <div v-else class="item-coupon-pick" @click="couponPickerTarget = item.skuId">
              <span>选择优惠券</span>
              <span class="arrow">›</span>
            </div>
          </div>
        </div>
        <div class="item-price price">¥{{ item.price.toFixed(2) }}</div>
        <div class="item-qty">x{{ item.quantity }}</div>
        <div class="item-subtotal">
          <div v-if="getItemDiscount(item) > 0" class="item-sub-original">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          <div class="price">¥{{ (item.price * item.quantity - getItemDiscount(item)).toFixed(2) }}</div>
        </div>
      </div>
    </div>

    <!-- 优惠券选择弹窗（按商品） -->
    <el-dialog v-model="couponPickerTarget" title="选择优惠券" width="480px" :close-on-click-modal="true">
      <template v-if="couponPickerTarget !== null">
        <div v-if="!myCoupons.length" class="no-coupon">暂无可用优惠券，去<a href="/coupons">领券中心</a>领取</div>
        <div v-for="c in getCouponsForItem(items.find(i => i.skuId === couponPickerTarget)!)" :key="c.id"
          class="coupon-card"
          :class="{ active: itemCouponMap[couponPickerTarget] === c.id, disabled: c.disabled }"
          @click="!c.disabled && selectItemCoupon(items.find(i => i.skuId === couponPickerTarget)!, c.id)">
          <div class="coupon-left">
            <div class="coupon-amount"><span class="coupon-symbol">¥</span><span class="coupon-value">{{ c.discount }}</span></div>
          </div>
          <div class="coupon-right">
            <div class="coupon-name">{{ c.name }}</div>
            <div class="coupon-condition">满{{ c.minAmount }}可用</div>
            <div class="coupon-expire">有效期至 {{ c.endDate }}</div>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 提交栏 -->
    <div class="submit-bar card">
      <div class="submit-info">
        <div class="submit-total">共 <strong>{{ items.reduce((s, i) => s + i.quantity, 0) }}</strong> 件商品</div>
        <div v-if="totalDiscount > 0" class="submit-discount">优惠：<span class="price">-¥{{ totalDiscount.toFixed(2) }}</span></div>
        <div class="submit-amount">应付总额：<span class="price price-lg">¥{{ payAmount.toFixed(2) }}</span></div>
      </div>
      <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">提交订单</el-button>
    </div>
  </div>
</template>

<style scoped>
.checkout-page { padding: 20px 15px; }
.page-title { font-size: 22px; margin-bottom: 20px; font-weight: bold; }
.section { margin-bottom: 20px; padding: 24px; }
.section-title { font-size: 16px; font-weight: bold; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid #eee; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px 24px; }
.form-item.full { grid-column: 1 / -1; }
.form-item label { display: block; font-size: 13px; color: #666; margin-bottom: 6px; }

.checkout-item { display: flex; align-items: flex-start; gap: 16px; padding: 16px 0; border-bottom: 1px solid #f5f5f5; }
.checkout-item:last-child { border-bottom: none; }
.item-image { width: 60px; height: 60px; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.item-svg { width: 36px; height: 36px; color: rgba(255,255,255,0.6); }
.item-info { flex: 1; }
.item-name { font-size: 14px; margin-bottom: 4px; }
.item-spec { font-size: 12px; color: #999; }
.item-price { width: 100px; text-align: right; }
.item-qty { width: 60px; text-align: center; color: #999; }
.item-subtotal { width: 100px; text-align: right; }
.item-sub-original { font-size: 12px; color: #bbb; text-decoration: line-through; }

/* 每商品优惠券 */
.item-coupon-row { margin-top: 8px; }
.item-coupon-active {
  display: inline-flex; align-items: center; gap: 8px;
  background: #fff8f0; border: 1px dashed #ff9900;
  padding: 4px 10px; border-radius: 6px; font-size: 12px;
}
.item-coupon-tag { color: #ff6600; font-weight: 500; }
.item-coupon-discount { color: #e1251b; font-weight: 600; }
.item-coupon-pick {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 10px; background: #fafafa; border: 1px dashed #ddd;
  border-radius: 6px; font-size: 12px; color: #999; cursor: pointer;
  transition: all 0.2s;
}
.item-coupon-pick:hover { background: #f0f0f0; border-color: #ff9900; color: #ff6600; }
.arrow { font-size: 16px; }

/* 弹窗内优惠券卡片 */
.no-coupon { text-align: center; color: #999; padding: 24px; }
.no-coupon a { color: #e1251b; text-decoration: none; }
.coupon-card { display: flex; margin-bottom: 12px; border: 2px solid #f0f0f0; border-radius: 10px; overflow: hidden; cursor: pointer; transition: all 0.2s; }
.coupon-card:hover:not(.disabled) { border-color: #ff9900; transform: scale(1.02); }
.coupon-card.active { border-color: #e1251b; background: #fff5f5; }
.coupon-card.disabled { opacity: 0.4; cursor: not-allowed; }
.coupon-left { width: 110px; background: linear-gradient(135deg, #ff9900, #ff6600); display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
.coupon-card.active .coupon-left { background: linear-gradient(135deg, #e1251b, #c41a1a); }
.coupon-symbol { font-size: 14px; margin-right: 2px; }
.coupon-value { font-size: 28px; font-weight: bold; }
.coupon-right { flex: 1; padding: 14px 16px; display: flex; flex-direction: column; justify-content: center; gap: 4px; }
.coupon-right .coupon-name { font-size: 15px; font-weight: 600; color: #333; }
.coupon-condition { font-size: 12px; color: #999; }
.coupon-expire { font-size: 12px; color: #bbb; }

/* 提交栏 */
.submit-bar { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px; position: sticky; bottom: 0; }
.submit-info { display: flex; align-items: baseline; gap: 20px; }
.submit-total { font-size: 14px; color: #666; }
.submit-discount { font-size: 13px; color: #e1251b; }
.submit-amount { font-size: 14px; color: #333; }
</style>
