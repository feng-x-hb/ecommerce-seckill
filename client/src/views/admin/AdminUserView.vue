<script setup lang="ts">
/**
 * 管理员：用户管理（AdminUserView.vue）
 */
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const roleFilter = ref<number | undefined>()

async function fetchUsers() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: 20 }
    if (roleFilter.value !== undefined) params.role = roleFilter.value
    const res: any = await request.get('/admin/user/list', { params })
    users.value = res.data.list || []
    total.value = res.data.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

async function toggleStatus(user: any) {
  const newStatus = user.status === 0 ? 1 : 0
  const label = newStatus === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${label}用户 ${user.username}？`, '提示', { type: 'warning' })
    await request.put(`/admin/user/${user.id}/status`, null, { params: { status: newStatus } })
    ElMessage.success(`已${label}`)
    fetchUsers()
  } catch { /* cancelled */ }
}

function handlePageChange(p: number) {
  page.value = p
  fetchUsers()
}

const roleMap: Record<number, string> = { 0: '买家', 1: '商家', 2: '管理员' }
const roleColor: Record<number, string> = { 0: '#1677ff', 1: '#fa8c16', 2: '#52c41a' }

onMounted(fetchUsers)
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>👥 用户管理</h2>
      <div class="filters">
        <el-select v-model="roleFilter" placeholder="全部角色" clearable @change="fetchUsers" style="width: 140px">
          <el-option label="买家" :value="0" />
          <el-option label="商家" :value="1" />
          <el-option label="管理员" :value="2" />
        </el-select>
      </div>
    </div>

    <el-table :data="users" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :style="{ background: roleColor[row.role], borderColor: roleColor[row.role] }" size="small">
            {{ roleMap[row.role] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="row.role !== 2" size="small" :type="row.status === 0 ? 'danger' : 'success'" text @click="toggleStatus(row)">
            {{ row.status === 0 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap" v-if="total > 20">
      <el-pagination :current-page="page" :page-size="20" :total="total" layout="prev, pager, next" @current-change="handlePageChange" />
    </div>
  </div>
</template>

<style scoped>
.admin-page { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; margin: 0; }
.filters { display: flex; gap: 12px; }
.pagination-wrap { display: flex; justify-content: center; padding-top: 20px; }
</style>
