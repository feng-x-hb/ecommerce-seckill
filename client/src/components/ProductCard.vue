<script setup lang="ts">
/**
 * 商品卡片组件 - 华丽版 v3
 * 3D倾斜悬浮 + 渐变发光边框 + 图片放大叠加 + 光泽扫过
 */
import { ref } from 'vue'

defineProps<{
  id: number
  title: string
  image: string
  price: number
  originalPrice?: number
  sales?: number
}>()

const cardRef = ref<HTMLElement>()
const tiltX = ref(0)
const tiltY = ref(0)
const isHovering = ref(false)

function onMouseMove(e: MouseEvent) {
  if (!cardRef.value) return
  const rect = cardRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  tiltX.value = ((y - centerY) / centerY) * -8
  tiltY.value = ((x - centerX) / centerX) * 8
  isHovering.value = true
}

function onMouseLeave() {
  tiltX.value = 0
  tiltY.value = 0
  isHovering.value = false
}
</script>

<template>
  <router-link
    :to="`/product/${id}`"
    class="product-card"
    ref="cardRef"
    :style="{ transform: `perspective(800px) rotateX(${tiltX}deg) rotateY(${tiltY}deg) ${isHovering ? 'translateZ(20px)' : ''}` }"
    @mousemove="onMouseMove"
    @mouseleave="onMouseLeave"
  >
    <!-- 发光边框层 -->
    <div class="glow-border-layer" :class="{ active: isHovering }"></div>

    <div class="card-image">
      <img v-if="image" :src="image" :alt="title" class="product-img" loading="lazy" @error="($event.target as HTMLImageElement).style.display='none'" />
      <div class="placeholder-img" :style="{ background: `linear-gradient(135deg, hsl(${id * 47 % 360}, 65%, 82%), hsl(${id * 47 % 360 + 30}, 65%, 72%))` }">
        <span class="placeholder-icon">{{ title.charAt(0) }}</span>
      </div>

      <!-- 光泽扫过 -->
      <div class="shimmer-overlay"></div>

      <!-- 悬浮遮罩 -->
      <div class="hover-overlay">
        <span class="view-text">查看详情</span>
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
  border-radius: 14px;
  overflow: hidden;
  color: var(--jd-text) !important;
  position: relative;
  transition: transform 0.2s ease-out, box-shadow 0.3s;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  transform-style: preserve-3d;
}
.product-card:hover {
  box-shadow: 0 20px 50px rgba(0,0,0,0.12), 0 0 30px rgba(225,37,27,0.1);
}

/* 发光边框 */
.glow-border-layer {
  position: absolute;
  top: -2px; left: -2px; right: -2px; bottom: -2px;
  border-radius: 16px;
  background: linear-gradient(135deg, #e1251b, #ff6700, #f5a623, #e1251b);
  background-size: 300% 300%;
  z-index: 0;
  opacity: 0;
  transition: opacity 0.4s;
}
.glow-border-layer.active {
  opacity: 1;
  animation: gradientShift 3s ease infinite;
}
@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 图片区 */
.card-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  z-index: 1;
}
.placeholder-img {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.5s;
}
.product-img {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
  z-index: 2;
}
.product-card:hover .product-img { transform: scale(1.1); }
.product-card:hover .placeholder-img { transform: scale(1.1); }
.placeholder-icon {
  font-size: 48px;
  color: rgba(255,255,255,0.8);
  font-weight: bold;
  transition: transform 0.5s;
}
.product-card:hover .placeholder-icon { transform: scale(1.15) rotate(5deg); }

/* 光泽扫过 */
.shimmer-overlay {
  position: absolute;
  top: 0; left: -100%; right: 0; bottom: 0;
  width: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  z-index: 3;
  transition: none;
  pointer-events: none;
}
.product-card:hover .shimmer-overlay {
  animation: shimmerSweep 0.8s ease forwards;
}
@keyframes shimmerSweep {
  0% { left: -100%; }
  100% { left: 100%; }
}

/* 悬浮遮罩 */
.hover-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 4;
  opacity: 0;
  transition: opacity 0.3s;
  backdrop-filter: blur(2px);
}
.product-card:hover .hover-overlay { opacity: 1; }
.view-text {
  background: rgba(255,255,255,0.95);
  color: #e1251b;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: bold;
  transform: translateY(10px);
  transition: transform 0.3s;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}
.product-card:hover .view-text { transform: translateY(0); }

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
  z-index: 5;
  box-shadow: 0 2px 8px rgba(225,37,27,0.3);
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
  z-index: 5;
  box-shadow: 0 2px 8px rgba(255,103,0,0.3);
}

.card-body { padding: 14px; position: relative; z-index: 1; }
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
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e1251b, #ff4e3a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: translateX(10px) scale(0.8);
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(225,37,27,0.3);
}
.product-card:hover .card-action {
  opacity: 1;
  transform: translateX(0) scale(1);
}
.card-action:hover {
  transform: scale(1.15) !important;
  box-shadow: 0 4px 16px rgba(225,37,27,0.5);
}
</style>
