<script setup lang="ts">
/**
 * 购物车页（CartView.vue）
 * 京东风格：商品列表 + 勾选 + 改数量 + 小计 + 底部结算栏
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCartQuantity, deleteCartItem, updateCartChecked } from '@/api/cart'
import type { CartItem } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartItems = ref<CartItem[]>([])
const loading = ref(false)

async function fetchCart() {
  loading.value = true
  try {
    const res: any = await getCartList()
    cartItems.value = res.data
  } finally {
    loading.value = false
  }
}

// 全选状态
const allChecked = computed({
  get: () => cartItems.value.length > 0 && cartItems.value.every(item => item.checked === 1),
  set: (val: boolean) => {
    cartItems.value.forEach(item => {
      item.checked = val ? 1 : 0
      updateCartChecked(item.id, val ? 1 : 0)
    })
  }
})

// 已勾选商品
const checkedItems = computed(() => cartItems.value.filter(item => item.checked === 1))
const totalCount = computed(() => checkedItems.value.reduce((sum, item) => sum + item.quantity, 0))
const totalPrice = computed(() => checkedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

async function handleQuantityChange(item: CartItem, val: number | undefined) {
  if (val === undefined || val < 1) return
  await updateCartQuantity(item.id, val)
  item.quantity = val
}

async function handleDelete(item: CartItem) {
  await ElMessageBox.confirm(`确定删除「${item.productName}」？`, '提示', { type: 'warning' })
  await deleteCartItem(item.id)
  cartItems.value = cartItems.value.filter(i => i.id !== item.id)
  ElMessage.success('已删除')
}

async function toggleCheck(item: CartItem) {
  const newVal = item.checked === 1 ? 0 : 1
  await updateCartChecked(item.id, newVal)
  item.checked = newVal
}

function goCheckout() {
  if (checkedItems.value.length === 0) return ElMessage.warning('请至少选择一件商品')
  // 将已勾选项存到 sessionStorage，下单页读取
  sessionStorage.setItem('checkoutItems', JSON.stringify(checkedItems.value))
  router.push('/checkout')
}

onMounted(fetchCart)
</script>

<template>
  <div class="cart-page container">
    <h2 class="page-title">我的购物车</h2>

    <div v-loading="loading" class="cart-content">
      <!-- 空购物车 -->
      <div v-if="!loading && cartItems.length === 0" class="empty-cart">
        <div class="empty-icon">🛒</div>
        <p>购物车是空的</p>
        <el-button type="primary" @click="router.push('/')">去逛逛</el-button>
      </div>

      <template v-else>
        <!-- 表头 -->
        <div class="cart-header">
          <div class="col-check">
            <input type="checkbox" :checked="allChecked" @change="allChecked = !allChecked" />
            全选
          </div>
          <div class="col-info">商品信息</div>
          <div class="col-price">单价</div>
          <div class="col-quantity">数量</div>
          <div class="col-subtotal">小计</div>
          <div class="col-action">操作</div>
        </div>

        <!-- 商品列表 -->
        <div v-for="item in cartItems" :key="item.id" class="cart-row">
          <div class="col-check">
            <input type="checkbox" :checked="item.checked === 1" @change="toggleCheck(item)" />
          </div>
          <div class="col-info">
            <router-link :to="`/product/${item.productId}`" class="item-link">
              <div class="item-image" :style="{ background: `hsl(${item.productId * 47 % 360}, 60%, 85%)` }">
                <span>{{ item.productName.charAt(0) }}</span>
              </div>
              <div class="item-detail">
                <div class="item-name ellipsis-2">{{ item.productName }}</div>
                <div class="item-spec">{{ item.specs }}</div>
              </div>
            </router-link>
          </div>
          <div class="col-price price">¥{{ item.price.toFixed(2) }}</div>
          <div class="col-quantity">
            <el-input-number
              :model-value="item.quantity"
              :min="1"
              :max="99"
              size="small"
              @change="(val: number | undefined) => handleQuantityChange(item, val)"
            />
          </div>
          <div class="col-subtotal price price-lg">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          <div class="col-action">
            <el-button type="danger" link @click="handleDelete(item)">删除</el-button>
          </div>
        </div>

        <!-- 底部结算栏 -->
        <div class="cart-footer">
          <div class="footer-left">
            <label class="check-all">
              <input type="checkbox" :checked="allChecked" @change="allChecked = !allChecked" />
              全选
            </label>
          </div>
          <div class="footer-right">
            <span>已选 <strong>{{ totalCount }}</strong> 件商品</span>
            <span class="footer-total">合计：<span class="price price-lg">¥{{ totalPrice.toFixed(2) }}</span></span>
            <el-button type="primary" size="large" @click="goCheckout" :disabled="checkedItems.length === 0">
              去结算
            </el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.cart-page { padding: 20px 15px; }
.page-title { font-size: 22px; margin-bottom: 20px; font-weight: bold; }

.cart-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: var(--jd-shadow);
  padding: 20px;
  min-height: 300px;
}

.empty-cart {
  text-align: center;
  padding: 80px 0;
}
.empty-icon { font-size: 60px; margin-bottom: 16px; }

.cart-header {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 2px solid #eee;
  font-size: 13px;
  color: #999;
}
.col-check { width: 120px; display: flex; align-items: center; gap: 8px; }
.col-info { flex: 1; }
.col-price { width: 100px; text-align: center; }
.col-quantity { width: 140px; text-align: center; }
.col-subtotal { width: 100px; text-align: center; }
.col-action { width: 80px; text-align: center; }

.cart-row {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}
.cart-row:hover { background: #fafafa; }

.item-link {
  display: flex;
  gap: 12px;
  color: var(--jd-text) !important;
}
.item-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: rgba(255,255,255,0.8);
  flex-shrink: 0;
}
.item-name { font-size: 14px; margin-bottom: 6px; }
.item-spec { font-size: 12px; color: #999; background: #f5f5f5; padding: 2px 8px; border-radius: 4px; display: inline-block; }

.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-top: 2px solid #eee;
  margin-top: 12px;
}
.check-all { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 13px; }
.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.footer-total { margin-left: 12px; font-size: 14px; }
</style>
