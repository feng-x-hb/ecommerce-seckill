<script setup lang="ts">
import Navbar from '@/components/Navbar.vue'
import { useRoute } from 'vue-router'
import { ref, computed, onMounted } from 'vue'

const route = useRoute()
const isDark = ref(false)
const isSpinning = ref(false)

const hideNav = computed(() => {
  const name = route.name as string
  if (['Login', 'ForgotPassword', 'MerchantLogin', 'About', 'Contact', 'Merchant'].includes(name)) return true
  if (route.path.startsWith('/admin') || route.path.startsWith('/seller')) return true
  return false
})

const hideThemeToggle = computed(() => {
  const name = route.name as string
  // 登录页有自己的主题切换，不显示全局的
  return ['Login', 'ForgotPassword', 'MerchantLogin'].includes(name)
})

function toggleTheme() {
  isDark.value = !isDark.value
  isSpinning.value = true
  setTimeout(() => { isSpinning.value = false }, 500)
  const theme = isDark.value ? 'dark' : 'light'
  localStorage.setItem('theme', theme)
  document.documentElement.setAttribute('data-theme', theme)
}

onMounted(() => {
  const saved = localStorage.getItem('theme') || 'light'
  isDark.value = saved === 'dark'
  document.documentElement.setAttribute('data-theme', saved)
})
</script>

<template>
  <div id="app">
    <!-- 全局主题切换浮动按钮 -->
    <button
      v-if="!hideThemeToggle"
      class="global-theme-toggle"
      :class="{ dark: isDark }"
      @click="toggleTheme"
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

/* 全局主题切换按钮 - 左上角 */
.global-theme-toggle {
  position: fixed;
  top: 16px;
  left: 20px;
  z-index: 9999;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, #fff, #f8f8f8);
  color: #e1251b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  box-shadow: var(--jd-shadow, 0 2px 12px rgba(0,0,0,0.08));
}
.global-theme-toggle.dark {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  color: #ffd700;
  border-color: rgba(255, 215, 0, 0.3);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.4);
}
.global-theme-toggle:hover {
  transform: scale(1.1);
  box-shadow: var(--jd-shadow-hover, 0 4px 20px rgba(0,0,0,0.15));
}
.global-theme-toggle.dark:hover {
  box-shadow: 0 4px 20px rgba(255, 215, 0, 0.2);
}
.global-theme-toggle:active {
  transform: scale(0.9);
}
.toggle-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.4s;
}
.toggle-icon.rotate {
  animation: iconSpin 0.5s ease;
}
@keyframes iconSpin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
