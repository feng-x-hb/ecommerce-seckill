<script setup lang="ts">
import Navbar from '@/components/Navbar.vue'
import { useRoute } from 'vue-router'
import { computed, onMounted } from 'vue'

const route = useRoute()
const isLoginPage = computed(() => route.name === 'Login' || route.name === 'ForgotPassword' || route.name === 'MerchantLogin')
const hideNav = computed(() => {
  const name = route.name as string
  if (['Login', 'ForgotPassword', 'MerchantLogin', 'About', 'Contact', 'Merchant'].includes(name)) return true
  if (route.path.startsWith('/admin') || route.path.startsWith('/seller')) return true
  return false
})

onMounted(() => {
  const saved = localStorage.getItem('theme') || 'light'
  document.documentElement.setAttribute('data-theme', saved)
})
</script>

<template>
  <div id="app">
    <Navbar v-if="!hideNav" />
    <main :class="{ 'main-content': !hideNav }">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.main-content {
  min-height: calc(100vh - 62px);
}
</style>
