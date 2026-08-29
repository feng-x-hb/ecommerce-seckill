<script setup lang="ts">
/**
 * 商品卡片组件 - 华丽版
 * 渐变背景 + 悬浮放大 + 发光边框 + 标签
 */
defineProps<{
  id: number
  title: string
  image: string
  price: number
  originalPrice?: number
  sales?: number
}>()
</script>

<template>
  <router-link :to="`/product/${id}`" class="product-card hover-lift glow-border">
    <div class="card-image">
      <div class="placeholder-img" :style="{ background: `linear-gradient(135deg, hsl(${id * 47 % 360}, 65%, 82%), hsl(${id * 47 % 360 + 30}, 65%, 72%))` }">
        <span class="placeholder-icon">{{ title.charAt(0) }}</span>
      </div>
      <div v-if="sales && sales > 50" class="hot-badge">
        <el-icon><Fire /></el-icon> 热卖
      </div>
      <div v-if="originalPrice && originalPrice > price" class="discount-badge">
        {{ Math.round((1 - price / originalPrice) * 100) }}% OFF
      </div>
    </div>
    <div class="card-body">
      <div class="card-title ellipsis-2">{{ title }}</div>
      <div class="card-price">
        <span class="price-symbol">¥</span>
        <span class="price price-lg">{{ price.toFixed(0) }}</span>
        <span class="price-decimal">.{{ (price % 1).toFixed(2).slice(2) }}</span>
        <span v-if="originalPrice && originalPrice > price" class="original-price">¥{{ originalPrice.toFixed(0) }}</span>
      </div>
      <div class="card-footer">
        <div class="card-sales" v-if="sales">
          <el-icon><TrendCharts /></el-icon>
          已售 {{ sales > 1000 ? (sales/1000).toFixed(1) + 'k' : sales }}
        </div>
        <div class="card-action">
          <el-icon><ShoppingCart /></el-icon>
        </div>
      </div>
    </div>
  </router-link>
</template>

<style scoped>
.product-card {
  display: block;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  color: var(--jd-text) !important;
}
.card-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
}
.placeholder-img {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.5s;
}
.product-card:hover .placeholder-img { transform: scale(1.08); }
.placeholder-icon {
  font-size: 48px;
  color: rgba(255,255,255,0.8);
  font-weight: bold;
  transition: transform 0.5s;
}
.product-card:hover .placeholder-icon { transform: scale(1.1); }

.hot-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: linear-gradient(135deg, #e1251b, #ff4e3a);
  color: #fff;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 3px;
}
.discount-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: linear-gradient(135deg, #ff6700, #ff9500);
  color: #fff;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: bold;
}

.card-body { padding: 14px; }
.card-title {
  font-size: 13px;
  line-height: 1.4;
  height: 38px;
  margin-bottom: 8px;
  color: #333;
}
.card-price {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}
.price-symbol { font-size: 13px; color: var(--jd-red); }
.price-decimal { font-size: 12px; color: var(--jd-red); }
.original-price {
  font-size: 12px;
  color: #ccc;
  text-decoration: line-through;
  margin-left: 6px;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-sales {
  font-size: 11px;
  color: #bbb;
  display: flex;
  align-items: center;
  gap: 3px;
}
.card-action {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--jd-red-light);
  color: var(--jd-red);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s;
}
.product-card:hover .card-action {
  opacity: 1;
  transform: translateX(0);
}
</style>
