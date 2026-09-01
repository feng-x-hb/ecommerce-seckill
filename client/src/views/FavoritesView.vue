<script setup lang="ts">
/**
 * 我的收藏页（FavoritesView.vue）
 * 路由：/favorites
 * 功能：展示收藏的商品列表，支持取消收藏
 */
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { getFavoriteList, toggleFavorite, batchDeleteFavorites } from '@/api/favorite'
import { getProductDetail } from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const favorites = ref<any[]>([])
const loading = ref(false)
const selectedIds = ref<Set<number>>(new Set())
const isDark = ref(document.documentElement.getAttribute('data-theme') === 'dark')
const allSelected = computed({
  get: () => favorites.value.length > 0 && favorites.value.every(f => selectedIds.value.has(f.id)),
  set: (val: boolean) => {
    if (val) { favorites.value.forEach(f => selectedIds.value.add(f.id)) }
    else { selectedIds.value.clear() }
  }
})

async function fetchFavorites() {
  loading.value = true
  try {
    const res: any = await getFavoriteList({ page: 1, size: 50 })
    const items = res.data.list || []
    const detailed = await Promise.all(
      items.map(async (fav: any) => {
        try {
          const productRes: any = await getProductDetail(fav.productId)
          return { ...fav, product: productRes.data }
        } catch {
          return { ...fav, product: null }
        }
      })
    )
    favorites.value = detailed
  } finally { loading.value = false }
}

async function handleRemove(fav: any) {
  try {
    await ElMessageBox.confirm('确定取消收藏？', '提示', { type: 'warning' })
    await toggleFavorite(fav.productId)
    ElMessage.success('已取消收藏')
    fetchFavorites()
  } catch { /* cancelled */ }
}

async function handleBatchRemove() {
  if (selectedIds.value.size === 0) return ElMessage.warning('请先选择要取消的商品')
  try {
    await ElMessageBox.confirm(`确定取消选中的 ${selectedIds.value.size} 个收藏？`, '提示', { type: 'warning' })
    await batchDeleteFavorites([...selectedIds.value])
    selectedIds.value.clear()
    ElMessage.success('已批量取消收藏')
    fetchFavorites()
  } catch { /* cancelled */ }
}

function toggleSelect(id: number) {
  if (selectedIds.value.has(id)) selectedIds.value.delete(id)
  else selectedIds.value.add(id)
}

onMounted(() => {
  const observer = new MutationObserver(() => {
    isDark.value = document.documentElement.getAttribute('data-theme') === 'dark'
  })
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
  onBeforeUnmount(() => observer.disconnect())
  fetchFavorites()
})
</script>

<template>
  <div class="fav-page" :class="{ dark: isDark }">
    <div class="fav-container container">
      <h1 class="page-title">❤️ 我的收藏</h1>

      <div v-if="loading" class="loading-wrap">
        <el-icon class="is-loading" :size="32" color="#e1251b"><Loading /></el-icon>
      </div>

      <div v-else-if="favorites.length === 0" class="empty-state">
        <div class="empty-icon">💝</div>
        <div class="empty-text">还没有收藏任何商品</div>
        <button class="go-btn" @click="router.push('/')">去逛逛</button>
      </div>

      <div v-else class="fav-grid">
        <div class="fav-toolbar">
          <label class="fav-check-all">
            <input type="checkbox" :checked="allSelected" @change="allSelected = !allSelected" />
            全选 ({{ selectedIds.size }}/{{ favorites.length }})
          </label>
          <el-button v-if="selectedIds.size > 0" type="danger" size="small" @click="handleBatchRemove">
            批量取消({{ selectedIds.size }})
          </el-button>
        </div>
        <template v-for="fav in favorites" :key="fav.id">
          <div v-if="fav.product" class="fav-card">
          <div class="fav-select">
            <input type="checkbox" :checked="selectedIds.has(fav.id)" @change="toggleSelect(fav.id)" />
          </div>
          <div class="fav-img" @click="router.push(`/product/${fav.productId}`)">
            <img :src="fav.product.mainImage" :alt="fav.product.title" />
          </div>
          <div class="fav-info">
            <div class="fav-title" @click="router.push(`/product/${fav.productId}`)">{{ fav.product.title }}</div>
            <div class="fav-price">¥{{ fav.product.price }}</div>
          </div>
          <button class="remove-btn" @click="handleRemove(fav)">取消收藏</button>
        </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fav-page { min-height: 100vh; background: var(--jd-bg); padding: 30px 0 60px; }
.fav-container { max-width: 800px; }
.page-title { font-size: 24px; font-weight: 600; margin-bottom: 24px; }

.loading-wrap { display: flex; justify-content: center; padding: 60px; }
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80px; }
.empty-icon { font-size: 64px; margin-bottom: 12px; }
.empty-text { color: #999; font-size: 15px; margin-bottom: 20px; }
.go-btn {
  background: linear-gradient(135deg, #e1251b, #ff6700); color: #fff; border: none;
  border-radius: 24px; padding: 10px 32px; cursor: pointer; font-size: 14px; transition: all 0.3s;
}
.go-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(225,37,27,0.35); }

.fav-grid { display: flex; flex-direction: column; gap: 14px; }
.fav-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; padding: 12px 16px; border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.fav-check-all { display: flex; align-items: center; gap: 8px; font-size: 13px; cursor: pointer; color: #666; }
.fav-card {
  display: flex; align-items: center; gap: 16px; background: #fff; border-radius: 12px;
  padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); transition: all 0.3s;
}
.fav-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); transform: translateY(-2px); }
.fav-select { display: flex; align-items: center; padding: 0 4px; }
.fav-img {
  width: 100px; height: 100px; border-radius: 8px; overflow: hidden; flex-shrink: 0; cursor: pointer;
}
.fav-img img { width: 100%; height: 100%; object-fit: cover; }
.fav-info { flex: 1; min-width: 0; }
.fav-title {
  font-size: 14px; color: #333; cursor: pointer; display: -webkit-box; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden; line-height: 1.5;
}
.fav-title:hover { color: #e1251b; }
.fav-price { font-size: 18px; font-weight: 600; color: #e1251b; margin-top: 8px; }
.remove-btn {
  background: none; border: 1px solid #ddd; border-radius: 20px; padding: 6px 16px;
  font-size: 12px; color: #999; cursor: pointer; transition: all 0.3s; white-space: nowrap;
}
.remove-btn:hover { border-color: #e1251b; color: #e1251b; }

.dark .fav-page { background: #121212; }
.dark .page-title { color: #e0e0e0; }
.dark .fav-toolbar { background: #1e1e1e; box-shadow: 0 2px 12px rgba(0,0,0,0.3); }
.dark .fav-check-all { color: #aaa; }
.dark .fav-card { background: #1e1e1e; box-shadow: 0 2px 12px rgba(0,0,0,0.3); }
.dark .fav-card:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.5); }
.dark .fav-title { color: #e0e0e0; }
.dark .fav-title:hover { color: #ff6700; }
.dark .empty-text { color: #888; }
</style>
