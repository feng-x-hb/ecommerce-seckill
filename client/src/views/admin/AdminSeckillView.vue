<script setup lang="ts">
/**
 * 管理员秒杀活动管理页
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formData = ref({ title: '', startTime: '', endTime: '' })
const editingId = ref<number | null>(null)

// 秒杀商品对话框
const itemDialogVisible = ref(false)
const currentActivityId = ref<number | null>(null)
const activityItems = ref<any[]>([])
const skuList = ref<any[]>([])
const itemForm = ref({ skuId: null as number | null, seckillPrice: 0, seckillStock: 10, purchaseLimit: 3 })

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get(`/admin/seckill/activity/list?page=${page.value}&size=${size.value}`)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function handleCreate() {
  isEdit.value = false
  formData.value = { title: '', startTime: '', endTime: '' }
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editingId.value = row.id
  formData.value = { title: row.title, startTime: row.startTime?.slice(0, 16) || '', endTime: row.endTime?.slice(0, 16) || '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.value.title) { ElMessage.error('请输入活动名称'); return }
  if (isEdit.value) {
    await request.put(`/admin/seckill/activity/${editingId.value}`, formData.value)
    ElMessage.success('更新成功')
  } else {
    await request.post('/admin/seckill/activity', formData.value)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  fetchData()
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定要删除活动「${row.title}」吗？`, '提示', { type: 'warning' })
  await request.delete(`/admin/seckill/activity/${row.id}`)
  ElMessage.success('删除成功')
  fetchData()
}

async function manageItems(row: any) {
  currentActivityId.value = row.id
  itemDialogVisible.value = true
  await Promise.all([loadItems(row.id), loadSkuList()])
}

async function loadItems(activityId: number) {
  const res = await request.get(`/admin/seckill/activity/${activityId}/items`)
  activityItems.value = res.data || []
}

async function loadSkuList() {
  const res = await request.get('/admin/seckill/sku/list')
  skuList.value = res.data || []
}

async function addItem() {
  if (!itemForm.value.skuId) { ElMessage.error('请选择SKU'); return }
  await request.post(`/admin/seckill/activity/${currentActivityId.value}/item`, itemForm.value)
  ElMessage.success('添加成功')
  itemForm.value = { skuId: null, seckillPrice: 0, seckillStock: 10, purchaseLimit: 3 }
  loadItems(currentActivityId.value!)
}

async function removeItem(id: number) {
  await request.delete(`/admin/seckill/item/${id}`)
  ElMessage.success('移除成功')
  loadItems(currentActivityId.value!)
}

function handleSizeChange(val: number) { size.value = val; page.value = 1; fetchData() }
function handleCurrentChange(val: number) { page.value = val; fetchData() }

onMounted(fetchData)
</script>

<template>
  <div class="admin-page">
    <div class="search-bar">
      <el-button type="primary" @click="handleCreate">+ 新建活动</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="活动名称" min-width="180" />
      <el-table-column prop="startTime" label="开始时间" width="170">
        <template #default="{ row }">{{ row.startTime?.replace('T', ' ').slice(0, 16) }}</template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="170">
        <template #default="{ row }">{{ row.endTime?.replace('T', ' ').slice(0, 16) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'danger'" size="small">
            {{ row.status === 0 ? '未开始' : row.status === 1 ? '进行中' : '已结束' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="manageItems(row)">管理商品</el-button>
          <el-button type="warning" size="small" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page" v-model:page-size="size" :total="total"
        :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange" @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建/编辑活动对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '新建活动'" width="500px">
      <el-form label-width="80px">
        <el-form-item label="活动名称">
          <el-input v-model="formData.title" placeholder="如：限时秒杀专场" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-input v-model="formData.startTime" type="datetime-local" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-input v-model="formData.endTime" type="datetime-local" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 管理秒杀商品对话框 -->
    <el-dialog v-model="itemDialogVisible" title="管理秒杀商品" width="700px">
      <div class="item-section">
        <h4>当前秒杀商品</h4>
        <el-table :data="activityItems" size="small" border>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="skuId" label="SKU ID" width="80" />
          <el-table-column prop="seckillPrice" label="秒杀价" width="100">
            <template #default="{ row }">¥{{ row.seckillPrice }}</template>
          </el-table-column>
          <el-table-column prop="seckillStock" label="秒杀库存" width="90" />
          <el-table-column prop="purchaseLimit" label="限购" width="70" />
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button type="danger" size="small" link @click="removeItem(row.id)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-divider />

      <div class="item-section">
        <h4>添加秒杀商品</h4>
        <el-form :inline="true">
          <el-form-item label="选择SKU">
            <el-select v-model="itemForm.skuId" placeholder="选择商品规格" filterable style="width: 200px">
              <el-option v-for="sku in skuList" :key="sku.id" :label="`#${sku.id} ¥${sku.price} 库存:${sku.stock}`" :value="sku.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="秒杀价">
            <el-input-number v-model="itemForm.seckillPrice" :min="0.01" :precision="2" style="width: 120px" />
          </el-form-item>
          <el-form-item label="库存">
            <el-input-number v-model="itemForm.seckillStock" :min="1" style="width: 100px" />
          </el-form-item>
          <el-form-item label="限购">
            <el-input-number v-model="itemForm.purchaseLimit" :min="1" style="width: 80px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addItem">添加</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.item-section h4 { margin: 0 0 12px; color: #333; }
</style>
