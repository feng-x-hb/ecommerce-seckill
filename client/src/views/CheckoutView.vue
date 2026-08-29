<script setup lang="ts">
/**
 * 下单页（CheckoutView.vue）
 * 京东风格：收货信息 + 商品确认 + 提交订单
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createOrder } from '@/api/order'
import type { CartItem } from '@/types'
import { ElMessage } from 'element-plus'

const router = useRouter()
const items = ref<CartItem[]>([])
const receiverName = ref('')
const receiverPhone = ref('')
const receiverAddress = ref('')
const submitting = ref(false)

const totalPrice = ref(0)

onMounted(() => {
  const data = sessionStorage.getItem('checkoutItems')
  if (!data) { router.push('/cart'); return }
  items.value = JSON.parse(data)
  totalPrice.value = items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

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
      receiverAddress: receiverAddress.value
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
        <div class="item-image" :style="{ background: `hsl(${item.productId * 47 % 360}, 60%, 85%)` }">
          <span>{{ item.productName.charAt(0) }}</span>
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

    <!-- 提交栏 -->
    <div class="submit-bar card">
      <div class="submit-total">
        共 <strong>{{ items.reduce((s, i) => s + i.quantity, 0) }}</strong> 件商品，
        应付总额：<span class="price price-lg">¥{{ totalPrice.toFixed(2) }}</span>
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
.item-price { width: 100px; text-align: right; }
.item-qty { width: 60px; text-align: center; color: #999; }
.item-subtotal { width: 100px; text-align: right; }

.submit-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  position: sticky;
  bottom: 0;
}
.submit-total { font-size: 14px; color: #666; }
</style>
