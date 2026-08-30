<script setup lang="ts">
/**
 * 商品卡片组件 - 华丽3D版
 * 鼠标跟随3D透视旋转 + 悬浮发光边框 + 图片放大
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
const glowX = ref(50)
const glowY = ref(50)
const isHovering = ref(false)

function onMouseMove(e: MouseEvent) {
  if (!cardRef.value) return
  const rect = cardRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  tiltX.value = ((y - centerY) / centerY) * -10
  tiltY.value = ((x - centerX) / centerX) * 10
  glowX.value = (x / rect.width) * 100
  glowY.value = (y / rect.height) * 100
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
    :style="{
      transform: `perspective(600px) rotateX(${tiltX}deg) rotateY(${tiltY}deg) ${isHovering ? 'translateZ(16px) scale(1.02)' : ''}`,
      '--glow-x': glowX + '%',
      '--glow-y': glowY + '%'
    }"
    :class="{ hovering: isHovering }"
    @mousemove="onMouseMove"
    @mouseleave="onMouseLeave"
  >
    <div class="card-image">
      <img v-if="image" :src="image" :alt="title" class="product-img" loading="lazy" @error="($event.target as HTMLImageElement).style.display='none'" />
      <div class="placeholder-img" :style="{ background: `linear-gradient(135deg, hsl(${id * 47 % 360}, 55%, 88%), hsl(${id * 47 % 360 + 30}, 55%, 80%))` }">
        <svg class="placeholder-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="12" y="16" width="40" height="32" rx="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
          <path d="M16 40 L24 30 L30 36 L42 22 L48 30" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.4"/>
          <circle cx="22" cy="26" r="4" stroke="currentColor" stroke-width="2" opacity="0.4"/>
        </svg>
      </div>

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
  border-radius: var(--jd-radius, 12px);
  overflow: hidden;
  color: var(--jd-text) !important;
  position: relative;
  transition: transform 0.15s ease-out, box-shadow 0.3s ease;
  box-shadow: var(--jd-shadow-sm, 0 1px 4px rgba(0,0,0,0.06));
  transform-style: preserve-3d;
  will-change: transform;
}
.product-card.hovering {
  box-shadow:
    0 20px 40px rgba(0,0,0,0.15),
    0 0 0 1px rgba(225,37,27,0.15),
    0 0 30px rgba(225,37,27,0.08);
}

/* 图片区 */
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
  transition: transform 0.5s cubic-bezier(0.23, 1, 0.32, 1);
}
.product-img {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.23, 1, 0.32, 1);
  z-index: 2;
}
.product-card.hovering .product-img { transform: scale(1.1); }
.product-card.hovering .placeholder-img { transform: scale(1.1); }

.placeholder-svg {
  width: 56px;
  height: 56px;
  color: rgba(255,255,255,0.6);
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
  pointer-events: none;
}
.product-card.hovering .hover-overlay { opacity: 1; }
.view-text {
  background: rgba(255,255,255,0.95);
  color: var(--jd-red, #e1251b);
  padding: 8px 22px;
  border-radius: var(--jd-radius-pill, 999px);
  font-size: 13px;
  font-weight: 600;
  transform: translateY(10px);
  transition: transform 0.4s cubic-bezier(0.23, 1, 0.32, 1);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.product-card.hovering .view-text { transform: translateY(0); }

.hot-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: var(--jd-red, #e1251b);
  color: #fff;
  padding: 3px 10px;
  border-radius: var(--jd-radius-sm, 8px);
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 3px;
  z-index: 5;
}
.discount-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: var(--jd-orange, #ff6700);
  color: #fff;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  z-index: 5;
}

/* 卡片内容 */
.card-body { padding: var(--sp-3, 12px) var(--sp-3, 12px) var(--sp-4, 16px); }
.card-title {
  font-size: 13px;
  line-height: 1.4;
  height: 38px;
  margin-bottom: var(--sp-2, 8px);
  color: var(--jd-text, #333);
}
.card-price {
  display: flex;
  align-items: baseline;
  margin-bottom: var(--sp-2, 8px);
}
.price-symbol { font-size: 13px; color: var(--jd-red, #e1251b); }
.price-decimal { font-size: 12px; color: var(--jd-red, #e1251b); }
.original-price {
  font-size: 12px;
  color: var(--jd-text-muted, #bbb);
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
  color: var(--jd-text-light, #999);
  display: flex;
  align-items: center;
  gap: 3px;
}
.card-action {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--jd-red, #e1251b);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.6) rotate(-20deg);
  transition: all 0.35s cubic-bezier(0.23, 1, 0.32, 1);
}
.product-card.hovering .card-action {
  opacity: 1;
  transform: scale(1) rotate(0deg);
}
.card-action:hover {
  background: var(--jd-red-hover, #c81623);
}
</style>
