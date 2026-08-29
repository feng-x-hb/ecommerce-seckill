<script setup lang="ts">
/**
 * 管理员商品管理页
 * 分页列表 + 上架/下架 + 删除 + 搜索
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const statusFilter = ref<number | null>(null)

async function fetchData() {
  loading.value = true
  try {
    let url = `/admin/product/list?page=${page.value}&size=${size.value}`
    if (keyword.value) url += `&keyword=${encodeURIComponent(keyword.value)}`
    if (statusFilter.value !== null) url += `&status=${statusFilter.value}`
    const res = await request.get(url)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() { page.value = 1; fetchData() }

async function toggleStatus(row: any) {
  const action = row.status === 1 ? 'offshelf' : 'onshelf'
  const label = row.status === 1 ? '下架' : '上架'
  await ElMessageBox.confirm(`确定要${label}「${row.title}」吗？`, '提示', { type: 'warning' })
  await request.put(`/admin/product/${row.id}/${action}`)
  ElMessage.success(`${label}成功`)
  fetchData()
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定要删除「${row.title}」吗？此操作不可恢复！`, '危险操作', { type: 'error' })
  await request.delete(`/admin/product/${row.id}`)
  ElMessage.success('删除成功')
  fetchData()
}

function handleSizeChange(val: number) { size.value = val; page.value = 1; fetchData() }
function handleCurrentChange(val: number) { page.value = val; fetchData() }

onMounted(fetchData)
</script>

<template>
  <div class="admin-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索商品名称" clearable style="width: 260px" @keyup.enter="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px" @change="handleSearch">
        <el-option label="上架中" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架中' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small" link @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped>
.admin-page { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; align-items: center; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
