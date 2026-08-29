<script setup lang="ts">
/**
 * 商家后台主布局 - 黑底侧边栏
 */
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapse = ref(false)

const menuItems = [
  { path: '/seller/products', label: '商品管理', icon: '📦' },
  { path: '/seller/orders', label: '订单管理', icon: '📋' },
  { path: '/seller/stats', label: '数据概览', icon: '📊' },
]

const activeMenu = computed(() => route.path)

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<template>
  <div class="seller-layout">
    <aside class="seller-sidebar" :class="{ collapsed: isCollapse }">
      <div class="sidebar-header">
        <img src="/images/优购logo设计.png" alt="优购" class="sidebar-logo" />
        <span v-if="!isCollapse" class="sidebar-title">商家中心</span>
      </div>
      <nav class="sidebar-menu">
        <router-link v-for="item in menuItems" :key="item.path" :to="item.path" class="menu-item" :class="{ active: activeMenu === item.path }">
          <span class="menu-icon">{{ item.icon }}</span>
          <span v-if="!isCollapse" class="menu-label">{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button class="collapse-btn" @click="isCollapse = !isCollapse">{{ isCollapse ? '▶' : '◀' }}</button>
      </div>
    </aside>

    <div class="seller-main">
      <header class="seller-header">
        <div class="header-left">
          <h3 class="page-title">{{ menuItems.find(m => m.path === activeMenu)?.label || '商家中心' }}</h3>
        </div>
        <div class="header-right">
          <el-dropdown @command="(cmd: string) => { if (cmd === 'logout') handleLogout() }">
            <div class="admin-avatar">
              <div class="avatar-circle-sm">商</div>
              <span class="admin-name">{{ userStore.userInfo?.nickname || '商家' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled><span style="color:#999;font-size:12px">商家账号</span></el-dropdown-item>
                <el-dropdown-item divided command="logout" style="color:#e1251b"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="seller-content"><router-view /></main>
    </div>
  </div>
</template>

<style scoped>
.seller-layout { display: flex; min-height: 100vh; background: #f5f7fa; }

.seller-sidebar {
  width: 220px; min-width: 220px; background: #1a1a1a; color: #fff;
  display: flex; flex-direction: column;
  transition: width 0.3s, min-width 0.3s; overflow: hidden;
}
.seller-sidebar.collapsed { width: 64px; min-width: 64px; }

.sidebar-header { padding: 20px; display: flex; align-items: center; gap: 12px; border-bottom: 1px solid #333; overflow: hidden; white-space: nowrap; }
.sidebar-logo { width: 36px; height: 36px; min-width: 36px; object-fit: contain; border-radius: 8px; }
.sidebar-title { font-size: 16px; font-weight: 600; white-space: nowrap; color: #fff; }

.sidebar-menu { flex: 1; padding: 12px 0; }
.menu-item { display: flex; align-items: center; gap: 12px; padding: 14px 20px; color: rgba(255,255,255,0.6); text-decoration: none; transition: all 0.2s; border-left: 3px solid transparent; white-space: nowrap; overflow: hidden; }
.menu-item:hover { background: rgba(255,255,255,0.08); color: #fff; }
.menu-item.active { background: rgba(225,37,27,0.2); color: #fff; border-left-color: #e1251b; }
.menu-icon { font-size: 18px; min-width: 18px; }
.menu-label { font-size: 14px; white-space: nowrap; }

.sidebar-footer { padding: 12px; border-top: 1px solid #333; }
.collapse-btn { width: 100%; padding: 8px; background: rgba(255,255,255,0.08); border: none; border-radius: 6px; color: rgba(255,255,255,0.6); cursor: pointer; font-size: 12px; transition: background 0.2s; }
.collapse-btn:hover { background: rgba(255,255,255,0.15); color: #fff; }

.seller-main { flex: 1; min-width: 0; display: flex; flex-direction: column; overflow-x: hidden; }

.seller-header { height: 60px; background: #1a1a1a; border-bottom: 1px solid #333; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; position: sticky; top: 0; z-index: 50; flex-shrink: 0; }
.header-left { display: flex; align-items: center; }
.page-title { font-size: 16px; font-weight: 600; color: #fff; }
.header-right { display: flex; align-items: center; gap: 16px; }
.admin-avatar { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 12px; border-radius: 20px; transition: background 0.2s; }
.admin-avatar:hover { background: rgba(255,255,255,0.1); }
.avatar-circle-sm { width: 32px; height: 32px; border-radius: 50%; background: linear-gradient(135deg, #ff6700, #ff8533); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; }
.admin-name { font-size: 14px; color: #fff; }

.seller-content { flex: 1; padding: 24px; }
</style>
