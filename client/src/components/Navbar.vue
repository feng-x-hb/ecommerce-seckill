<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const isSearchFocused = ref(false)
const searchHistory = ref<string[]>([])
const showHistory = ref(false)
const searchSuggestions = ref<string[]>([])

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
    <!-- 第一行：Logo + 搜索 + 用户 -->
    <div class="nav-top">
      <div class="nav-top-inner">
        <!-- Logo -->
        <router-link to="/" class="logo">
          <img src="/images/优购logo设计.png" alt="优购" class="logo-img" />
          <div class="logo-text">
            <span class="logo-name">优购</span>
            <span class="logo-slogan">YouGou.com</span>
          </div>
        </router-link>

        <!-- 搜索框 -->
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
          <!-- 搜索历史 -->
          <div v-if="showHistory && searchHistory.length && !searchSuggestions.length" class="search-dropdown">
            <div class="dropdown-header">
              <span>搜索历史</span>
              <span class="clear-btn" @click="clearHistory">清空</span>
            </div>
            <div v-for="item in searchHistory" :key="item" class="dropdown-item" @mousedown="handleSearch(item)">
              <el-icon :size="14" color="#bbb"><Clock /></el-icon> {{ item }}
            </div>
          </div>
          <!-- 搜索补全 -->
          <div v-if="searchSuggestions.length" class="search-dropdown">
            <div v-for="item in searchSuggestions" :key="item" class="dropdown-item" @mousedown="handleSearch(item)">
              <el-icon :size="14" color="#e1251b"><Search /></el-icon> {{ item }}
            </div>
          </div>
        </div>

        <!-- 右侧按钮 -->
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
                  <span style="color:#999;font-size:12px">登录账号：{{ userStore.userInfo.nickname }}</span>
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
      </div>
    </div>

    <!-- 第二行：导航链接 -->
    <div class="nav-links-bar">
      <div class="nav-links-inner">
        <router-link to="/" class="nav-link"><el-icon :size="14"><HomeFilled /></el-icon> 首页</router-link>
        <router-link to="/seckill" class="nav-link seckill-link"><el-icon :size="14"><Lightning /></el-icon> 秒杀</router-link>
        <router-link to="/category/1" class="nav-link">数码家电</router-link>
        <router-link to="/category/2" class="nav-link">服饰鞋包</router-link>
        <router-link to="/category/3" class="nav-link">家居日用</router-link>
        <router-link to="/category/4" class="nav-link">食品饮料</router-link>
        <router-link to="/category/5" class="nav-link">美妆个护</router-link>
        <router-link to="/category/6" class="nav-link">运动户外</router-link>
        <router-link to="/category/7" class="nav-link">母婴玩具</router-link>
        <router-link to="/orders" class="nav-link">我的订单</router-link>
        <router-link to="/coupons" class="nav-link">领券中心</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.navbar { position: sticky; top: 0; z-index: 100; width: 100%; }

/* ========== 第一行：白底 header ========== */
.nav-top {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.nav-top-inner {
  display: flex;
  align-items: center;
  height: 60px;
  gap: 24px;
  width: 100%;
  box-sizing: border-box;
  padding: 0 15%;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  text-decoration: none;
  color: #333 !important;
}
.logo-img {
  width: 40px;
  height: 40px;
  object-fit: contain;
  border-radius: 8px;
}
.logo-text { display: flex; flex-direction: column; }
.logo-name { font-size: 20px; font-weight: 700; color: #e1251b; line-height: 1.2; }
.logo-slogan { font-size: 10px; color: #999; letter-spacing: 1px; }

/* 搜索框 */
.search-wrapper {
  flex: 1;
  max-width: 520px;
  position: relative;
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
  color: #333;
  padding: 0 14px;
}
.search-input::placeholder { color: #bbb; }
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
  background: #fff;
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
  color: #999;
}
.clear-btn { cursor: pointer; color: #e1251b; font-size: 12px; }
.clear-btn:hover { text-decoration: underline; }
.dropdown-item {
  padding: 8px 14px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}
.dropdown-item:hover { background: #f5f5f5; color: #333; }

/* 右侧按钮 */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: #666 !important;
  text-decoration: none;
  font-size: 11px;
  cursor: pointer;
  transition: color 0.2s;
  padding: 4px 8px;
  border-radius: 6px;
}
.action-item:hover { color: #e1251b !important; background: #fff5f5; }

/* ========== 第二行：导航链接 ========== */
.nav-links-bar {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.nav-links-inner {
  display: flex;
  align-items: center;
  height: 36px;
  gap: 0;
  width: 100%;
  box-sizing: border-box;
  padding: 0 15%;
  overflow-x: auto;
}
.nav-link {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  color: #666 !important;
  text-decoration: none;
  font-size: 13px;
  white-space: nowrap;
  transition: color 0.2s;
  border-radius: 4px;
}
.nav-link:hover { color: #e1251b !important; background: #fff5f5; }
.nav-link.router-link-exact-active { color: #e1251b !important; font-weight: 600; }
.seckill-link { color: #e1251b !important; font-weight: 600; }
</style>
