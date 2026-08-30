<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('iPhone 15 Pro')
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
    <div class="main-nav">
      <div class="main-nav-inner">
        <!-- Logo -->
        <router-link to="/" class="logo">
          <img src="/images/优购logo设计.png" alt="优购" class="logo-img" />
          <div class="logo-text">
            <span class="logo-name">优购</span>
            <span class="logo-slogan">品质生活</span>
          </div>
        </router-link>

        <!-- 导航链接 -->
        <div class="nav-links">
          <router-link to="/" class="nav-link">
            <span class="nav-icon"><el-icon :size="16"><HomeFilled /></el-icon></span>
            首页
          </router-link>
          <router-link to="/seckill" class="nav-link seckill-link">
            <span class="nav-icon seckill-icon"><el-icon :size="16"><Lightning /></el-icon></span>
            秒杀
            <span class="hot-dot"></span>
          </router-link>
          <router-link to="/orders" class="nav-link">
            <span class="nav-icon"><el-icon :size="16"><List /></el-icon></span>
            我的订单
          </router-link>
        </div>

        <!-- 搜索框 -->
        <div class="nav-search-wrapper">
          <div class="nav-search" :class="{ focused: isSearchFocused }">
            <div class="search-icon">
              <el-icon :size="16"><Search /></el-icon>
            </div>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="iPhone 15 Pro"
              class="search-input"
              @focus="isSearchFocused = true; showHistory = true"
              @blur="setTimeout(() => { showHistory = false; searchSuggestions = [] }, 200)"
              @keyup.enter="handleSearch()"
              @input="onSearchInput()"
            />
            <button class="search-btn" @click="handleSearch()">
              <el-icon :size="14"><Search /></el-icon>
            </button>
          </div>
          <!-- 搜索历史下拉 -->
          <div v-if="showHistory && searchHistory.length && !searchSuggestions.length" class="search-history">
            <div class="history-header">
              <span>搜索历史</span>
              <span class="clear-btn" @click="clearHistory">清空</span>
            </div>
            <div
              v-for="item in searchHistory"
              :key="item"
              class="history-item"
              @mousedown="handleSearch(item)"
            >
              <el-icon :size="14" style="margin-right:8px;color:#bbb"><Clock /></el-icon>
              {{ item }}
            </div>
          </div>
          <!-- 搜索补全下拉 -->
          <div v-if="searchSuggestions.length" class="search-history">
            <div
              v-for="item in searchSuggestions"
              :key="item"
              class="history-item"
              @mousedown="handleSearch(item)"
            >
              <el-icon :size="14" style="margin-right:8px;color:#e1251b"><Search /></el-icon>
              {{ item }}
            </div>
          </div>
        </div>

        <!-- 右侧：购物车 + 头像 -->
        <div class="nav-right">
          <router-link to="/cart" class="cart-icon" title="购物车">
            <el-badge :value="0" :hidden="true">
              <div class="icon-circle">
                <el-icon :size="20"><ShoppingCart /></el-icon>
              </div>
            </el-badge>
          </router-link>

          <el-dropdown v-if="userStore.userInfo" trigger="click" @command="(cmd: string) => { if (cmd === 'logout') handleLogout(); if (cmd === 'info') router.push('/profile'); if (cmd === 'favorites') router.push('/favorites'); if (cmd === 'coupons') router.push('/coupons') }">
            <div class="avatar-area">
              <div class="avatar-circle">
                <el-icon :size="20"><User /></el-icon>
              </div>
              <span class="avatar-name">{{ userStore.userInfo.nickname }}</span>
              <el-icon class="avatar-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <span style="color:#999;font-size:12px">登录账号：{{ userStore.userInfo.nickname }}</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="info">
                  <el-icon><Edit /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <el-icon><Star /></el-icon> 我的收藏
                </el-dropdown-item>
                <el-dropdown-item command="coupons">
                  <el-icon><Ticket /></el-icon> 优惠券
                </el-dropdown-item>
                <el-dropdown-item command="logout" style="color:#e1251b">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <router-link v-else to="/login" class="login-link">
            <div class="avatar-circle">
              <el-icon :size="20"><User /></el-icon>
            </div>
            <span class="avatar-name">请登录</span>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.navbar { position: sticky; top: 0; z-index: 100; width: 100%; }

.main-nav {
  background: linear-gradient(135deg, #e1251b 0%, #ff4e3a 50%, #e1251b 100%);
  box-shadow: 0 4px 20px rgba(225,37,27,0.3);
  width: 100%;
}
.main-nav-inner {
  display: flex;
  align-items: center;
  height: 64px;
  gap: 20px;
  width: 100%;
  margin: 0;
  padding: 0 20px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  color: #fff !important;
  margin-left: 24px;
}
.logo-img {
  width: 42px;
  height: 42px;
  object-fit: contain;
  border-radius: 10px;
  flex-shrink: 0;
}
.logo-text { display: flex; flex-direction: column; }
.logo-name { font-size: 22px; font-weight: bold; letter-spacing: 1px; line-height: 1.2; }
.logo-slogan { font-size: 10px; opacity: 0.8; letter-spacing: 2px; }

/* 导航链接 */
.nav-links { display: flex; gap: 2px; }
.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  color: rgba(255,255,255,0.85) !important;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  position: relative;
  letter-spacing: 0.5px;
}
.nav-link:hover {
  background: rgba(255,255,255,0.18);
  color: #fff !important;
  transform: translateY(-1px);
}
.nav-link.router-link-exact-active {
  background: rgba(255,255,255,0.22);
  color: #fff !important;
  font-weight: 600;
}
.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255,255,255,0.15);
  transition: all 0.3s;
}
.nav-link:hover .nav-icon { background: rgba(255,255,255,0.3); transform: scale(1.1); }
.seckill-icon { background: rgba(255,215,0,0.25); color: #ffd700; }
.seckill-link { font-weight: bold; }
.hot-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  background: #ffd700;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

/* 搜索框 */
.nav-search-wrapper { flex: 1; max-width: 460px; position: relative; }
.nav-search {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.95);
  border-radius: 24px;
  overflow: visible;
  transition: all 0.3s;
  border: 2px solid transparent;
}
.nav-search.focused {
  border-color: #ffd700;
  box-shadow: 0 0 20px rgba(255,215,0,0.4);
}
.search-icon { padding: 0 12px; color: #999; }
.search-input {
  flex: 1;
  height: 38px;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
  color: #333;
}
.search-btn {
  height: 38px;
  width: 52px;
  background: linear-gradient(135deg, #ff6700, #e1251b);
  color: #fff;
  border: none;
  border-radius: 0 22px 22px 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}
.search-btn:hover { filter: brightness(1.1); }

/* 搜索历史 */
.search-history {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
  padding: 8px 0;
  z-index: 200;
}
.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 14px;
  font-size: 12px;
  color: #999;
}
.clear-btn {
  cursor: pointer;
  color: #e1251b;
  font-size: 12px;
}
.clear-btn:hover { text-decoration: underline; }
.history-item {
  padding: 8px 14px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: background 0.2s;
}
.history-item:hover { background: #f5f5f5; color: #333; }

/* 右侧区域 */
.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  margin-left: auto;
}

.cart-icon { color: #fff !important; }
.icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255,255,255,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  border: 1px solid rgba(255,255,255,0.2);
  color: #fff;
}
.cart-icon:hover .icon-circle {
  background: rgba(255,255,255,0.3);
  transform: translateY(-3px);
}

/* 头像区域 */
.avatar-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px 4px 4px;
  border-radius: 24px;
  transition: background 0.2s;
}
.avatar-area:hover { background: rgba(255,255,255,0.15); }
.avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255,255,255,0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border: 2px solid rgba(255,255,255,0.4);
}
.avatar-name {
  color: #fff;
  font-size: 13px;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.avatar-arrow { color: rgba(255,255,255,0.7); font-size: 12px; }

.login-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff !important;
  text-decoration: none;
}
</style>
