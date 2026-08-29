<script setup lang="ts">
/**
 * 商家商品管理页 - 分页列表 + 添加/编辑/上下架/删除
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

// 分类列表
const categoryList = ref<any[]>([])

// 添加/编辑对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<any>({ title: '', subtitle: '', categoryId: null, price: 0, mainImage: '', detail: '' })
const editingId = ref<number | null>(null)

// SKU 对话框
const skuDialogVisible = ref(false)
const currentProductId = ref<number | null>(null)
const skuList = ref<any[]>([])
const skuForm = ref({ specs: '', price: 0, stock: 10 })

async function fetchData() {
  loading.value = true
  try {
    let url = `/seller/product/list?page=${page.value}&size=${size.value}`
    if (keyword.value) url += `&keyword=${encodeURIComponent(keyword.value)}`
    if (statusFilter.value !== null) url += `&status=${statusFilter.value}`
    const res = await request.get(url)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function loadCategories() {
  const res = await request.get('/category/list')
  // 展平树形分类
  const flat: any[] = []
  function walk(list: any[], parent: string) {
    for (const item of list) {
      flat.push({ id: item.id, name: parent ? `${parent} > ${item.name}` : item.name })
      if (item.children) walk(item.children, item.name)
    }
  }
  walk(res.data || [], '')
  categoryList.value = flat
}

function handleSearch() { page.value = 1; fetchData() }

function handleCreate() {
  isEdit.value = false
  form.value = { title: '', subtitle: '', categoryId: null, price: 0, mainImage: '', detail: '' }
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editingId.value = row.id
  form.value = { title: row.title, subtitle: row.subtitle || '', categoryId: row.categoryId, price: row.price, mainImage: row.mainImage || '', detail: row.detail || '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.title) { ElMessage.error('请输入商品名称'); return }
  if (isEdit.value) {
    await request.put(`/seller/product/${editingId.value}`, form.value)
    ElMessage.success('更新成功')
  } else {
    await request.post('/seller/product', form.value)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  fetchData()
}

async function toggleStatus(row: any) {
  const action = row.status === 1 ? 'offshelf' : 'onshelf'
  const label = row.status === 1 ? '下架' : '上架'
  await ElMessageBox.confirm(`确定要${label}「${row.title}」吗？`, '提示', { type: 'warning' })
  await request.put(`/seller/product/${row.id}/${action}`)
  ElMessage.success(`${label}成功`)
  fetchData()
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定要删除「${row.title}」吗？此操作不可恢复！`, '危险操作', { type: 'error' })
  await request.delete(`/seller/product/${row.id}`)
  ElMessage.success('删除成功')
  fetchData()
}

async function manageSku(row: any) {
  currentProductId.value = row.id
  skuDialogVisible.value = true
  const res = await request.get(`/seller/product/${row.id}/sku`)
  skuList.value = res.data || []
}

async function addSku() {
  if (!skuForm.value.specs) { ElMessage.error('请输入规格'); return }
  ElMessage.success('SKU 添加成功（演示）')
  skuForm.value = { specs: '', price: 0, stock: 10 }
}

function handleSizeChange(val: number) { size.value = val; page.value = 1; fetchData() }
function handleCurrentChange(val: number) { page.value = val; fetchData() }

onMounted(() => { fetchData(); loadCategories() })
</script>

<template>
  <div class="admin-page">
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索商品名称" clearable style="width: 260px" @keyup.enter="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 130px" @change="handleSearch">
        <el-option label="上架中" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleCreate">+ 添加商品</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="商品名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="100"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架中' : '已下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="manageSku(row)">规格</el-button>
          <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small" link @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button type="info" size="small" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <!-- 添加/编辑商品 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '添加商品'" width="600px">
      <el-form label-width="80px">
        <el-form-item label="商品名称"><el-input v-model="form.title" placeholder="请输入商品名称" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" placeholder="商品卖点（可选）" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" filterable style="width: 100%">
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 200px" /></el-form-item>
        <el-form-item label="主图URL"><el-input v-model="form.mainImage" placeholder="粘贴图片链接" /></el-form-item>
        <el-form-item label="详情"><el-input v-model="form.detail" type="textarea" :rows="4" placeholder="商品详情描述" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- SKU 管理 -->
    <el-dialog v-model="skuDialogVisible" title="管理商品规格" width="600px">
      <el-table :data="skuList" size="small" border style="margin-bottom: 16px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="specs" label="规格" />
        <el-table-column prop="price" label="价格" width="100"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
      </el-table>
      <el-form :inline="true">
        <el-form-item label="规格"><el-input v-model="skuForm.specs" placeholder='如 {"颜色":"黑色"}' style="width: 200px" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="skuForm.price" :min="0" :precision="2" style="width: 120px" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="skuForm.stock" :min="0" style="width: 100px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="addSku">添加</el-button></el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; align-items: center; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
