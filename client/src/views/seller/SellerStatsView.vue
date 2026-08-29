<script setup lang="ts">
/**
 * 商家数据概览页
 */
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const stats = ref<any>({})

async function loadStats() {
  const res = await request.get('/seller/product/stats')
  stats.value = res.data || {}
}

onMounted(loadStats)
</script>

<template>
  <div class="stats-page">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #e1251b, #ff4e3a);">📦</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalProducts || 0 }}</div>
          <div class="stat-label">全部商品</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61);">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.onShelf || 0 }}</div>
          <div class="stat-label">上架中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #ff6700, #ff8533);">📈</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalSales || 0 }}</div>
          <div class="stat-label">总销量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff);">💰</div>
        <div class="stat-info">
          <div class="stat-value">¥{{ stats.totalRevenue || 0 }}</div>
          <div class="stat-label">总销售额</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-page { padding: 0; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 20px; }
.stat-card {
  background: #fff; border-radius: 12px; padding: 28px 24px;
  display: flex; align-items: center; gap: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.stat-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 28px; flex-shrink: 0; }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: 700; color: #333; line-height: 1.2; }
.stat-label { font-size: 14px; color: #999; margin-top: 4px; }
</style>
