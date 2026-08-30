<script setup lang="ts">
/**
 * 自定义分类SVG图标组件
 * 7个分类各有独立设计的线条风格SVG
 */
defineProps<{
  name: string
  size?: number
}>()
</script>

<template>
  <!-- 数码家电 -->
  <svg v-if="name === '数码家电'" class="cat-svg" :width="size || 20" :height="size || 20" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect x="8" y="6" width="32" height="24" rx="3" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <line x1="8" y1="30" x2="40" y2="30" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <rect x="18" y="30" width="12" height="4" rx="1" stroke="currentColor" stroke-width="2"/>
    <rect x="14" y="34" width="20" height="3" rx="1.5" stroke="currentColor" stroke-width="2"/>
    <circle cx="24" cy="18" r="5" stroke="currentColor" stroke-width="2"/>
    <path d="M22 16.5 L26 18 L22 19.5Z" fill="currentColor" opacity="0.6"/>
    <path d="M15 12 L18 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    <path d="M30 12 L33 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
  </svg>

  <!-- 服饰鞋包 -->
  <svg v-else-if="name === '服饰鞋包'" class="cat-svg" :width="size || 20" :height="size || 20" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M16 8 C16 8 14 14 14 20 C14 26 18 28 18 28 L18 38 C18 40 20 42 22 42 L26 42 C28 42 30 40 30 38 L30 28 C30 28 34 26 34 20 C34 14 32 8 32 8" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
    <path d="M16 8 L32 8" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <path d="M20 14 C20 14 22 16 24 14 C26 12 28 14 28 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    <circle cx="24" cy="22" r="2" stroke="currentColor" stroke-width="2"/>
    <path d="M12 18 L16 20" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <path d="M36 18 L32 20" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
  </svg>

  <!-- 家居日用 -->
  <svg v-else-if="name === '家居日用'" class="cat-svg" :width="size || 20" :height="size || 20" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M8 24 L24 8 L40 24" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
    <path d="M12 22 L12 40 L36 40 L36 22" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
    <rect x="20" y="30" width="8" height="10" rx="1" stroke="currentColor" stroke-width="2"/>
    <path d="M24 30 L24 34" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    <circle cx="24" cy="36" r="1" fill="currentColor"/>
    <path d="M16 18 L16 14 C16 12 18 10 20 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
  </svg>

  <!-- 食品饮料 -->
  <svg v-else-if="name === '食品饮料'" class="cat-svg" :width="size || 20" :height="size || 20" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M14 16 L14 38 C14 40 16 42 18 42 L30 42 C32 42 34 40 34 38 L34 16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
    <path d="M10 16 L38 16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <path d="M14 16 C14 10 18 6 24 6 C30 6 34 10 34 16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <ellipse cx="24" cy="26" rx="6" ry="4" stroke="currentColor" stroke-width="2"/>
    <path d="M24 22 L24 30" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-dasharray="2 2"/>
    <path d="M18 26 L30 26" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-dasharray="2 2"/>
    <circle cx="20" cy="24" r="1.5" stroke="currentColor" stroke-width="1.5"/>
    <circle cx="28" cy="28" r="1" stroke="currentColor" stroke-width="1.5"/>
  </svg>

  <!-- 美妆个护 -->
  <svg v-else-if="name === '美妆个护'" class="cat-svg" :width="size || 20" :height="size || 20" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect x="16" y="18" width="16" height="24" rx="4" stroke="currentColor" stroke-width="2.5"/>
    <path d="M20 18 L20 12 C20 10 22 8 24 8 C26 8 28 10 28 12 L28 18" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <circle cx="24" cy="30" r="4" stroke="currentColor" stroke-width="2"/>
    <path d="M24 28 L24 32" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    <path d="M22 30 L26 30" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    <path d="M10 24 C10 24 14 22 16 24" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    <path d="M38 24 C38 24 34 22 32 24" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    <path d="M10 30 C10 30 14 28 16 30" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    <path d="M38 30 C38 30 34 28 32 30" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
  </svg>

  <!-- 运动户外 -->
  <svg v-else-if="name === '运动户外'" class="cat-svg" :width="size || 20" :height="size || 20" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
    <circle cx="24" cy="24" r="16" stroke="currentColor" stroke-width="2.5"/>
    <path d="M24 8 C28 14 30 20 30 24 C30 28 28 34 24 40" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    <path d="M24 8 C20 14 18 20 18 24 C18 28 20 34 24 40" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    <path d="M10 18 L38 18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    <path d="M10 30 L38 30" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    <circle cx="24" cy="24" r="3" stroke="currentColor" stroke-width="2"/>
    <circle cx="24" cy="24" r="1" fill="currentColor"/>
  </svg>

  <!-- 母婴玩具 -->
  <svg v-else-if="name === '母婴玩具'" class="cat-svg" :width="size || 20" :height="size || 20" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
    <circle cx="24" cy="18" r="10" stroke="currentColor" stroke-width="2.5"/>
    <circle cx="20" cy="16" r="2" fill="currentColor"/>
    <circle cx="28" cy="16" r="2" fill="currentColor"/>
    <path d="M20 22 C20 22 22 25 24 22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    <path d="M18 10 C18 10 14 6 12 8" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <path d="M30 10 C30 10 34 6 36 8" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <path d="M16 28 L16 42" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <path d="M32 28 L32 42" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <path d="M12 42 L20 42" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <path d="M28 42 L36 42" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
    <path d="M18 34 L30 34" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    <circle cx="24" cy="34" r="2" stroke="currentColor" stroke-width="1.5"/>
  </svg>

  <!-- 默认/全部 -->
  <svg v-else class="cat-svg" :width="size || 20" :height="size || 20" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect x="6" y="6" width="15" height="15" rx="3" stroke="currentColor" stroke-width="2.5"/>
    <rect x="27" y="6" width="15" height="15" rx="3" stroke="currentColor" stroke-width="2.5"/>
    <rect x="6" y="27" width="15" height="15" rx="3" stroke="currentColor" stroke-width="2.5"/>
    <rect x="27" y="27" width="15" height="15" rx="3" stroke="currentColor" stroke-width="2.5"/>
    <circle cx="13.5" cy="13.5" r="2.5" fill="currentColor" opacity="0.4"/>
    <circle cx="34.5" cy="13.5" r="2.5" fill="currentColor" opacity="0.4"/>
    <circle cx="13.5" cy="34.5" r="2.5" fill="currentColor" opacity="0.4"/>
    <circle cx="34.5" cy="34.5" r="2.5" fill="currentColor" opacity="0.4"/>
  </svg>
</template>

<style scoped>
.cat-svg {
  flex-shrink: 0;
  transition: transform 0.3s;
}
</style>
