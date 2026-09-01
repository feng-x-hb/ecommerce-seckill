<script setup lang="ts">
/**
 * 管理员：优惠券管理（AdminCouponView.vue）
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

interface CouponTemplate {
  id: number
  name: string
  discount: number
  minAmount: number
  total: number
  remain: number
  startDate: string
  endDate: string
  status: number
  createdAt: string
  updatedAt: string
}

const loading = ref(false)
const tableData = ref<CouponTemplate[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

// 新增/编辑对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({
  name: '',
  discount: 0,
  minAmount: 0,
  total: 0,
  startDate: '',
  endDate: '',
  status: 1
})

// 发放对话框
const grantDialogVisible = ref(false)
const grantUserId = ref<number | null>(null)
const grantTemplateId = ref<number | null>(null)

async function fetchData() {
  loading.value = true
  try {
    const res: any = await request.get(`/admin/coupon/list?page=${page.value}&size=${size.value}`)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch { /* interceptor handles */ } finally { loading.value = false }
}

function resetForm() {
  form.name = ''
  form.discount = 0
  form.minAmount = 0
  form.total = 0
  form.startDate = ''
  form.endDate = ''
  form.status = 1
}

function handleCreate() {
  resetForm()
  isEdit.value = false
  editingId.value = null
  dialogVisible.value = true
}

function handleEdit(row: CouponTemplate) {
  isEdit.value = true
  editingId.value = row.id
  form.name = row.name
  form.discount = row.discount
  form.minAmount = row.minAmount
  form.total = row.total
  form.startDate = row.startDate?.slice(0, 16) || ''
  form.endDate = row.endDate?.slice(0, 16) || ''
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name.trim()) return ElMessage.warning('请输入优惠券名称')
  if (form.discount <= 0) return ElMessage.warning('折扣金额必须大于0')
  if (form.total <= 0) return ElMessage.warning('总量必须大于0')
  if (!form.startDate || !form.endDate) return ElMessage.warning('请选择有效期')

  const payload = { ...form }
  try {
    if (isEdit.value && editingId.value) {
      await request.put('/admin/coupon/update', { ...payload, id: editingId.value })
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/coupon/save', payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch { /* interceptor handles */ }
}

async function handleDelete(row: CouponTemplate) {
  try {
    await ElMessageBox.confirm(`确定删除优惠券「${row.name}」吗？此操作不可恢复。`, '提示', { type: 'warning' })
    await request.delete(`/admin/coupon/delete/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}

async function handleToggleStatus(row: CouponTemplate) {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${label}优惠券「${row.name}」吗？`, '提示', { type: 'info' })
    await request.put('/admin/coupon/update', { ...row, status: newStatus })
    ElMessage.success(`${label}成功`)
    fetchData()
  } catch { /* cancelled */ }
}

function handleGrant(row: CouponTemplate) {
  grantTemplateId.value = row.id
  grantUserId.value = null
  grantDialogVisible.value = true
}

async function confirmGrant() {
  if (!grantUserId.value || grantUserId.value <= 0) return ElMessage.warning('请输入有效的用户ID')
  try {
    await request.post(`/admin/coupon/grant?templateId=${grantTemplateId.value}&userId=${grantUserId.value}`)
    ElMessage.success('发放成功')
    grantDialogVisible.value = false
    fetchData()
  } catch { /* interceptor handles */ }
}

function handleSizeChange(val: number) {
  size.value = val
  page.value = 1
  fetchData()
}

function handleCurrentChange(val: number) {
  page.value = val
  fetchData()
}

onMounted(fetchData)
</script>

<template>
  <div class="admin-page">
    <div class="search-bar">
      <h2>优惠券管理</h2>
      <el-button type="primary" @click="handleCreate">新增优惠券</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="discount" label="折扣金额" width="100">
        <template #default="{ row }">¥{{ row.discount }}</template>
      </el-table-column>
      <el-table-column prop="minAmount" label="最低消费" width="100">
        <template #default="{ row }">¥{{ row.minAmount }}</template>
      </el-table-column>
      <el-table-column prop="total" label="总量" width="80" />
      <el-table-column prop="remain" label="剩余" width="80" />
      <el-table-column label="有效期" width="200">
        <template #default="{ row }">
          {{ row.startDate?.replace('T', ' ').slice(0, 16) }} ~ {{ row.endDate?.replace('T', ' ').slice(0, 16) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            active-text="上架"
            inactive-text="下架"
            inline-prompt
            @change="handleToggleStatus(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="handleGrant(row)">发放</el-button>
          <el-button type="warning" size="small" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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

    <!-- 新增/编辑优惠券对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑优惠券' : '新增优惠券'" width="520px">
      <el-form label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="如：满100减20" />
        </el-form-item>
        <el-form-item label="折扣金额">
          <el-input-number v-model="form.discount" :min="0.01" :precision="2" style="width: 200px" />
        </el-form-item>
        <el-form-item label="最低消费">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width: 200px" />
        </el-form-item>
        <el-form-item label="总量">
          <el-input-number v-model="form.total" :min="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-input v-model="form.startDate" type="datetime-local" style="width: 240px" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-input v-model="form.endDate" type="datetime-local" style="width: 240px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 发放优惠券对话框 -->
    <el-dialog v-model="grantDialogVisible" title="发放优惠券" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户ID">
          <el-input-number v-model="grantUserId" :min="1" placeholder="输入目标用户ID" style="width: 200px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grantDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmGrant">确认发放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.search-bar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.search-bar h2 { font-size: 20px; font-weight: 600; margin: 0; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
