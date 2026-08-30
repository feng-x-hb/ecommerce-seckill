<script setup lang="ts">
import Navbar from '@/components/Navbar.vue'
import { useRoute } from 'vue-router'
import { ref, computed, onMounted } from 'vue'

const route = useRoute()
const isDark = ref(false)
const isSpinning = ref(false)

// 拖拽相关
const btnRef = ref<HTMLElement>()
const pos = ref({ x: 20, y: 16 })
const isDragging = ref(false)
let dragOffset = { x: 0, y: 0 }
let hasMoved = false

const hideNav = computed(() => {
  const name = route.name as string
  if (['Login', 'ForgotPassword', 'MerchantLogin', 'About', 'Contact', 'Merchant'].includes(name)) return true
  if (route.path.startsWith('/admin') || route.path.startsWith('/seller')) return true
  return false
})

function toggleTheme() {
  if (hasMoved) return // 拖拽结束不触发切换
  isDark.value = !isDark.value
  isSpinning.value = true
  setTimeout(() => { isSpinning.value = false }, 500)
  const theme = isDark.value ? 'dark' : 'light'
  localStorage.setItem('theme', theme)
  document.documentElement.setAttribute('data-theme', theme)
}

function onPointerDown(e: PointerEvent) {
  if (!btnRef.value) return
  hasMoved = false
  isDragging.value = true
  const rect = btnRef.value.getBoundingClientRect()
  dragOffset.x = e.clientX - rect.left
  dragOffset.y = e.clientY - rect.top
  btnRef.value.setPointerCapture(e.pointerId)
}

function onPointerMove(e: PointerEvent) {
  if (!isDragging.value) return
  hasMoved = true
  const x = e.clientX - dragOffset.x
  const y = e.clientY - dragOffset.y
  // 限制在视口内
  const maxX = window.innerWidth - 44
  const maxY = window.innerHeight - 44
  pos.value.x = Math.max(0, Math.min(x, maxX))
  pos.value.y = Math.max(0, Math.min(y, maxY))
}

function onPointerUp() {
  isDragging.value = false
  localStorage.setItem('themeTogglePos', JSON.stringify(pos.value))
}

onMounted(() => {
  const saved = localStorage.getItem('theme') || 'light'
  isDark.value = saved === 'dark'
  document.documentElement.setAttribute('data-theme', saved)

  const savedPos = localStorage.getItem('themeTogglePos')
  if (savedPos) {
    pos.value = JSON.parse(savedPos)
  }
})
</script>

<template>
  <div id="app">
    <!-- 全局主题切换浮动按钮（可拖拽） -->
    <button
      ref="btnRef"
      class="global-theme-toggle"
      :class="{ dark: isDark, dragging: isDragging }"
      :style="{ left: pos.x + 'px', top: pos.y + 'px' }"
      @click="toggleTheme"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      :title="isDark ? '切换亮色模式' : '切换暗色模式'"
    >
      <span class="toggle-icon" :class="{ rotate: isSpinning }">
        <el-icon :size="18">
          <Sunny v-if="isDark" />
          <Moon v-else />
        </el-icon>
      </span>
    </button>

    <Navbar v-if="!hideNav" />
    <main :class="{ 'main-content': !hideNav }">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<style scoped>
.main-content {
  min-height: calc(100vh - 62px);
}

/* 全局主题切换按钮 - 可拖拽 */
.global-theme-toggle {
  position: fixed;
  z-index: 9999;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, #fff, #f8f8f8);
  color: #e1251b;
  cursor: grab;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: box-shadow 0.3s, transform 0.15s;
  box-shadow: var(--jd-shadow, 0 2px 12px rgba(0,0,0,0.08));
  touch-action: none;
  user-select: none;
}
.global-theme-toggle.dragging {
  cursor: grabbing;
  transform: scale(1.15);
  box-shadow: 0 8px 30px rgba(0,0,0,0.25);
  transition: box-shadow 0.2s;
}
.global-theme-toggle.dark {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  color: #ffd700;
  border-color: rgba(255, 215, 0, 0.3);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.4);
}
.global-theme-toggle:hover:not(.dragging) {
  transform: scale(1.1);
  box-shadow: var(--jd-shadow-hover, 0 4px 20px rgba(0,0,0,0.15));
}
.global-theme-toggle.dark:hover:not(.dragging) {
  box-shadow: 0 4px 20px rgba(255, 215, 0, 0.2);
}
.toggle-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.4s;
  pointer-events: none;
}
.toggle-icon.rotate {
  animation: iconSpin 0.5s ease;
}
@keyframes iconSpin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
