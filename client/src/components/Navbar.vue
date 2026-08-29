<script setup lang="ts">
/**
 * 顶部导航栏 - 精简版
 * 红色导航 + 搜索 + 头像下拉菜单
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const isSearchFocused = ref(false)

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/', query: { keyword: searchKeyword.value.trim() } })
  }
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<template>
  <div class="navbar">
    <!-- 主导航 -->
    <div class="main-nav">
      <div class="container main-nav-inner">
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
            <el-icon><HomeFilled /></el-icon> 首页
          </router-link>
          <router-link to="/seckill" class="nav-link seckill-link">
            <el-icon><Lightning /></el-icon> 秒杀
            <span class="hot-dot"></span>
          </router-link>
          <router-link to="/cart" class="nav-link">
            <el-icon><ShoppingCart /></el-icon> 购物车
          </router-link>
          <router-link to="/orders" class="nav-link">
            <el-icon><List /></el-icon> 我的订单
          </router-link>
        </div>

        <!-- 搜索框 -->
        <div class="nav-search" :class="{ focused: isSearchFocused }">
          <div class="search-icon">
            <el-icon :size="16"><Search /></el-icon>
          </div>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索商品..."
            class="search-input"
            @focus="isSearchFocused = true"
            @blur="isSearchFocused = false"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">
            <el-icon :size="16"><Search /></el-icon> 搜索
          </button>
        </div>

        <!-- 右侧：头像 + 购物车 -->
        <div class="nav-right">
          <!-- 购物车 -->
          <router-link to="/cart" class="icon-item" title="购物车">
            <div class="icon-circle">
              <el-icon :size="20"><ShoppingCart /></el-icon>
            </div>
            <span class="icon-text">购物车</span>
          </router-link>

          <!-- 头像下拉菜单 -->
          <el-dropdown v-if="userStore.userInfo" trigger="click" @command="(cmd: string) => { if (cmd === 'logout') handleLogout(); if (cmd === 'info') ElMessage.info('功能开发中') }">
            <div class="avatar-area">
              <div class="avatar-circle">
                <el-icon :size="22"><User /></el-icon>
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
                  <el-icon><Edit /></el-icon> 更改信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" style="color:#e1251b">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 未登录 -->
          <router-link v-else to="/login" class="login-link">
            <div class="avatar-circle">
              <el-icon :size="22"><User /></el-icon>
            </div>
            <span class="avatar-name">请登录</span>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.navbar { position: sticky; top: 0; z-index: 100; }

/* 主导航 */
.main-nav {
  background: linear-gradient(135deg, #e1251b 0%, #ff4e3a 50%, #e1251b 100%);
  box-shadow: 0 4px 20px rgba(225,37,27,0.3);
}
.main-nav-inner {
  display: flex;
  align-items: center;
  height: 64px;
  gap: 24px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  color: #fff !important;
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
.nav-links { display: flex; gap: 4px; }
.nav-link {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  color: rgba(255,255,255,0.9) !important;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
  position: relative;
}
.nav-link:hover {
  background: rgba(255,255,255,0.2);
  color: #fff !important;
  transform: translateY(-2px);
}
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
.nav-search {
  flex: 1;
  max-width: 440px;
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.95);
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.3s;
  border: 2px solid transparent;
}
.nav-search.focused {
  border-color: #ffd700;
  box-shadow: 0 0 20px rgba(255,215,0,0.4);
}
.search-icon {
  padding: 0 12px;
  color: #999;
}
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
  padding: 0 20px;
  background: linear-gradient(135deg, #ff6700, #e1251b);
  color: #fff;
  border: none;
  cursor: pointer;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s;
}
.search-btn:hover { filter: brightness(1.1); }

/* 右侧区域 */
.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

/* 图标 */
.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: rgba(255,255,255,0.9) !important;
  cursor: pointer;
}
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
}
.icon-item:hover .icon-circle {
  background: rgba(255,255,255,0.3);
  transform: translateY(-3px);
}
.icon-text { font-size: 11px; }

/* 头像区域 */
.avatar-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
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

/* 未登录 */
.login-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff !important;
  text-decoration: none;
}
</style>
