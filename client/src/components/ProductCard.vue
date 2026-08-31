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
      transform: `perspective(600px) rotateX(${tiltX}deg) rotateY(${tiltY}deg) ${isHovering ? 'translateZ(16px) scale(1.06)' : ''}`,
      '--glow-x': glowX + '%',
      '--glow-y': glowY + '%'
    }"
    :class="{ hovering: isHovering }"
    @mousemove="onMouseMove"
    @mouseenter="isHovering = true"
    @mouseleave="onMouseLeave"
  >
    <div class="card-image">
      <img v-if="image && !image.startsWith('/images/products/')" :src="image" :alt="title" class="product-img" loading="lazy" @error="($event.target as HTMLImageElement).style.display='none'" />
      <div class="placeholder-img" :style="{ background: `linear-gradient(135deg, hsl(${id * 47 % 360}, 55%, 88%), hsl(${id * 47 % 360 + 30}, 55%, 80%))` }">
        <div class="placeholder-content">
          <div class="placeholder-icon">{{ title.charAt(0) }}</div>
          <div class="placeholder-title">{{ title.length > 8 ? title.slice(0, 8) : title }}</div>
        </div>
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
  transition: transform 0.2s ease-out, box-shadow 0.3s ease;
  box-shadow: var(--jd-shadow-sm, 0 1px 4px rgba(0,0,0,0.06));
  transform-style: preserve-3d;
  will-change: transform;
}
.product-card:hover {
  transform: perspective(600px) translateZ(8px) scale(1.06) !important;
  box-shadow:
    0 20px 40px rgba(0,0,0,0.15),
    0 0 0 1px rgba(225,37,27,0.15),
    0 0 30px rgba(225,37,27,0.08);
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
  padding-top: 70%;
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
.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.placeholder-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255,255,255,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: rgba(255,255,255,0.9);
  backdrop-filter: blur(4px);
}
.placeholder-title {
  font-size: 13px;
  font-weight: 500;
  color: rgba(255,255,255,0.85);
  text-align: center;
  max-width: 80%;
  line-height: 1.3;
  text-shadow: 0 1px 3px rgba(0,0,0,0.1);
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
.card-body { padding: 8px; }
.card-title {
  font-size: 12px;
  line-height: 1.3;
  height: 30px;
  margin-bottom: 4px;
  color: var(--jd-text, #333);
}
.card-price {
  display: flex;
  align-items: baseline;
  margin-bottom: 4px;
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
