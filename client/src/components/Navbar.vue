<script setup lang="ts">
import { ref, onMounted, onUpdated, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')
const isSearchFocused = ref(false)
const searchHistory = ref<string[]>([])
const showHistory = ref(false)
const searchSuggestions = ref<string[]>([])

// 滑动背景指示器
const navLinksRef = ref<HTMLElement | null>(null)
const indicatorStyle = ref({ left: '0px', width: '0px', opacity: 0 })

function updateIndicator() {
  nextTick(() => {
    const container = navLinksRef.value
    if (!container) return
    const activeEl = container.querySelector('.nav-link-active') as HTMLElement
    if (!activeEl) { indicatorStyle.value.opacity = 0; return }
    const containerRect = container.getBoundingClientRect()
    const activeRect = activeEl.getBoundingClientRect()
    indicatorStyle.value = {
      left: (activeRect.left - containerRect.left + container.scrollLeft) + 'px',
      width: activeRect.width + 'px',
      opacity: 1
    }
  })
}

watch(() => route.path, () => { updateIndicator() }, { immediate: true })
onMounted(() => { updateIndicator() })
onUpdated(() => { updateIndicator() })

onMounted(() => {
  const saved = localStorage.getItem('searchHistory')
  if (saved) searchHistory.value = JSON.parse(saved)
})

function saveHistory(kw: string) {
  if (!kw.trim()) return
  searchHistory.value = [kw, ...searchHistory.value.filter(h => h !== kw)].slice(0, 10)
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

function handleSearch(kw?: string) {
  const keyword = kw || searchKeyword.value.trim()
  if (!keyword) return
  searchKeyword.value = keyword
  saveHistory(keyword)
  showHistory.value = false
  searchSuggestions.value = []
  router.push({ path: '/search', query: { keyword } })
}

let suggestTimer: ReturnType<typeof setTimeout>
function onSearchInput() {
  clearTimeout(suggestTimer)
  const kw = searchKeyword.value.trim()
  if (!kw) { searchSuggestions.value = []; return }
  suggestTimer = setTimeout(async () => {
    try {
      const res: any = await request.get('/product/suggest', { params: { keyword: kw } })
      searchSuggestions.value = res.data || []
    } catch { searchSuggestions.value = [] }
  }, 300)
}

function clearHistory() {
  searchHistory.value = []
  localStorage.removeItem('searchHistory')
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<template>
  <div class="navbar">
    <div class="nav-top">
      <div class="nav-top-inner">

        <!-- Logo — 左侧 5%，跨两行 -->
        <router-link to="/" class="logo">
          <img src="/images/优购logo设计.png" alt="优购" class="logo-img" />
          <div class="logo-text">
            <span class="logo-name">优购</span>
            <span class="logo-slogan">YouGou.com</span>
          </div>
        </router-link>

        <!-- 搜索框 — 中心，上半行 -->
        <div class="search-wrapper">
          <div class="search-box" :class="{ focused: isSearchFocused }">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索商品"
              class="search-input"
              @focus="isSearchFocused = true; showHistory = true"
              @blur="setTimeout(() => { showHistory = false; searchSuggestions = [] }, 200)"
              @keyup.enter="handleSearch()"
              @input="onSearchInput()"
            />
            <button class="search-btn" @click="handleSearch()">搜索</button>
          </div>
          <div v-if="showHistory && searchHistory.length && !searchSuggestions.length" class="search-dropdown">
            <div class="dropdown-header">
              <span>搜索历史</span>
              <span class="clear-btn" @click="clearHistory">清空</span>
            </div>
            <div v-for="item in searchHistory" :key="item" class="dropdown-item" @mousedown="handleSearch(item)">
              <el-icon :size="14" color="#bbb"><Clock /></el-icon> {{ item }}
            </div>
          </div>
          <div v-if="searchSuggestions.length" class="search-dropdown">
            <div v-for="item in searchSuggestions" :key="item" class="dropdown-item" @mousedown="handleSearch(item)">
              <el-icon :size="14" color="#e1251b"><Search /></el-icon> {{ item }}
            </div>
          </div>
        </div>

        <!-- 购物车/用户 — 右侧 5%，跨两行 -->
        <div class="nav-actions">
          <router-link to="/cart" class="action-item" title="购物车">
            <el-icon :size="22"><ShoppingCart /></el-icon>
            <span>购物车</span>
          </router-link>

          <el-dropdown v-if="userStore.userInfo" trigger="click" @command="(cmd: string) => { if (cmd === 'logout') handleLogout(); if (cmd === 'info') router.push('/profile'); if (cmd === 'favorites') router.push('/favorites'); if (cmd === 'coupons') router.push('/coupons') }">
            <div class="action-item">
              <el-icon :size="22"><User /></el-icon>
              <span>{{ userStore.userInfo.nickname }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <span style="color:#999;font-size:12px">登录账号：{{ userStore.userInfo.username }}</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="info"><el-icon><Edit /></el-icon> 个人中心</el-dropdown-item>
                <el-dropdown-item command="favorites"><el-icon><Star /></el-icon> 我的收藏</el-dropdown-item>
                <el-dropdown-item command="coupons"><el-icon><Ticket /></el-icon> 优惠券</el-dropdown-item>
                <el-dropdown-item command="logout" style="color:#e1251b"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <router-link v-else to="/login" class="action-item">
            <el-icon :size="22"><User /></el-icon>
            <span>请登录</span>
          </router-link>
        </div>

        <!-- 导航链接 — 下半行 15%~85% -->
        <div class="nav-links-row" ref="navLinksRef">
          <!-- 滑动背景指示器 -->
          <div class="nav-indicator" :style="indicatorStyle"></div>
          <router-link to="/" class="nav-link" :class="{ 'nav-link-active': route.path === '/' }"><el-icon :size="14"><HomeFilled /></el-icon> 首页</router-link>
          <router-link to="/seckill" class="nav-link seckill-link" :class="{ 'nav-link-active': route.path === '/seckill' }"><el-icon :size="14"><Lightning /></el-icon> 秒杀</router-link>
          <router-link to="/category/1" class="nav-link" :class="{ 'nav-link-active': route.path === '/category/1' }">数码家电</router-link>
          <router-link to="/category/2" class="nav-link" :class="{ 'nav-link-active': route.path === '/category/2' }">服饰鞋包</router-link>
          <router-link to="/category/3" class="nav-link" :class="{ 'nav-link-active': route.path === '/category/3' }">家居日用</router-link>
          <router-link to="/category/4" class="nav-link" :class="{ 'nav-link-active': route.path === '/category/4' }">食品饮料</router-link>
          <router-link to="/category/5" class="nav-link" :class="{ 'nav-link-active': route.path === '/category/5' }">美妆个护</router-link>
          <router-link to="/category/6" class="nav-link" :class="{ 'nav-link-active': route.path === '/category/6' }">运动户外</router-link>
          <router-link to="/category/7" class="nav-link" :class="{ 'nav-link-active': route.path === '/category/7' }">母婴玩具</router-link>
          <router-link to="/orders" class="nav-link" :class="{ 'nav-link-active': route.path === '/orders' }">我的订单</router-link>
          <router-link to="/coupons" class="nav-link" :class="{ 'nav-link-active': route.path === '/coupons' }">领券中心</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.navbar { position: sticky; top: 0; z-index: 100; width: 100%; }

/* 整个 navbar 是一个 96px 高的相对定位容器 */
.nav-top {
  position: relative;
  width: 100%;
  height: 144px;
  background: var(--jd-white, #fff);
  border-bottom: 1px solid var(--jd-border-light, #f0f0f0);
}
.nav-top-inner {
  position: relative;
  width: 100%;
  height: 100%;
}

/* ========== Logo — 左 5%，跨两行 ========== */
.logo {
  position: absolute;
  left: 5%;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: var(--jd-text, #333) !important;
  z-index: 2;
  transition: transform 0.25s ease;
}
.logo:hover { transform: translateY(-50%) scale(1.08); }
.logo-img {
  width: 72px;
  height: 72px;
  object-fit: contain;
  border-radius: 10px;
}
.logo-text { display: flex; flex-direction: column; }
.logo-name { font-size: 30px; font-weight: 700; color: #e1251b; line-height: 1.2; }
.logo-slogan { font-size: 12px; color: var(--jd-text-light, #999); letter-spacing: 1px; }

/* ========== 搜索框 — 水平居中，上半行 ========== */
.search-wrapper {
  position: absolute;
  left: 50%;
  top: 36px;
  transform: translate(-50%, -50%);
  width: 30%;
  min-width: 320px;
  max-width: 520px;
  z-index: 10;
}
.search-box {
  display: flex;
  align-items: center;
  border: 2px solid #ff6700;
  border-radius: 22px;
  overflow: hidden;
  transition: all 0.3s;
}
.search-box.focused {
  border-color: #e1251b;
  box-shadow: 0 0 0 2px rgba(225,37,27,0.1);
}
.search-input {
  flex: 1;
  height: 36px;
  border: none;
  background: transparent;
  font-size: 13px;
  outline: none;
  color: var(--jd-text, #333);
  padding: 0 14px;
}
.search-input::placeholder { color: var(--jd-text-light, #bbb); }
.search-btn {
  height: 36px;
  padding: 0 20px;
  background: linear-gradient(135deg, #ff6700, #e1251b);
  color: #fff;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}
.search-btn:hover { filter: brightness(1.05); }

/* 搜索下拉 */
.search-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: var(--jd-white, #fff);
  border-radius: 10px;
  box-shadow: 0 6px 24px rgba(0,0,0,0.12);
  padding: 6px 0;
  z-index: 200;
}
.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 14px;
  font-size: 12px;
  color: var(--jd-text-light, #999);
}
.clear-btn { cursor: pointer; color: #e1251b; font-size: 12px; }
.clear-btn:hover { text-decoration: underline; }
.dropdown-item {
  padding: 8px 14px;
  font-size: 13px;
  color: var(--jd-text-secondary, #666);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}
.dropdown-item:hover { background: var(--jd-bg-light, #f5f5f5); color: var(--jd-text, #333); }

/* ========== 购物车/用户 — 右 5%，跨两行 ========== */
.nav-actions {
  position: absolute;
  right: 5%;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 20px;
  z-index: 2;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: var(--jd-text-secondary, #666) !important;
  text-decoration: none;
  font-size: 11px;
  cursor: pointer;
  transition: color 0.2s, transform 0.25s ease;
  padding: 4px 8px;
  border-radius: 6px;
}
.action-item:hover { color: #e1251b !important; background: rgba(225,37,27,0.08); transform: scale(1.1); }

/* ========== 导航链接 — 下半行，15%~85% ========== */
.nav-links-row {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 54px;
  display: flex;
  align-items: center;
  margin: 0 5% 0 25%;
  border-top: 1px solid var(--jd-border-light, #f0f0f0);
  overflow-x: auto;
  overflow-y: hidden;
}
.nav-indicator {
  position: absolute;
  bottom: 6px;
  height: 32px;
  background: linear-gradient(135deg, #ff6700, #e1251b);
  border-radius: 16px;
  transition: left 0.35s cubic-bezier(0.4, 0, 0.2, 1), width 0.35s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.25s;
  z-index: 0;
  pointer-events: none;
}
.nav-link {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  color: var(--jd-text-secondary, #666) !important;
  text-decoration: none;
  font-size: 13px;
  white-space: nowrap;
  transition: color 0.25s;
  border-radius: 4px;
}
.nav-link:hover { color: #fff !important; }
.nav-link-active { color: #fff !important; font-weight: 600; }
.seckill-link { color: #e1251b !important; font-weight: 600; }
.seckill-link.nav-link-active { color: #fff !important; }
</style>
