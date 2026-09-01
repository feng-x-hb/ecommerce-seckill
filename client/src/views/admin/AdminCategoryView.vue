<script setup lang="ts">
/**
 * 管理员：分类管理（AdminCategoryView.vue）
 */
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const categories = ref<any[]>([])
const loading = ref(false)
const showAdd = ref(false)
const form = ref({ name: '', parentId: 0, sort: 0 })
const editId = ref<number | null>(null)

async function fetchCategories() {
  loading.value = true
  try {
    const res: any = await request.get('/category/list')
    categories.value = res.data || []
  } catch { /* ignore */ } finally { loading.value = false }
}

function openAdd(parentId = 0) {
  form.value = { name: '', parentId, sort: 0 }
  editId.value = null
  showAdd.value = true
}

function openEdit(cat: any) {
  form.value = { name: cat.name, parentId: cat.parentId || 0, sort: cat.sort || 0 }
  editId.value = cat.id
  showAdd.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) return ElMessage.warning('名称不能为空')
  try {
    if (editId.value) {
      await request.put(`/category/${editId.value}`, form.value)
      ElMessage.success('修改成功')
    } else {
      await request.post('/category', form.value)
      ElMessage.success('添加成功')
    }
    showAdd.value = false
    fetchCategories()
  } catch { /* interceptor handles */ }
}

async function handleDelete(cat: any) {
  try {
    await ElMessageBox.confirm(`确定删除分类"${cat.name}"？`, '提示', { type: 'warning' })
    await request.delete(`/category/${cat.id}`)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch { /* cancelled */ }
}

onMounted(fetchCategories)
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>📂 分类管理</h2>
      <el-button type="primary" @click="openAdd()">新增顶级分类</el-button>
    </div>

    <div v-loading="loading">
      <div v-for="cat in categories" :key="cat.id" class="cat-group">
        <div class="cat-parent">
          <span class="cat-name">{{ cat.name }}</span>
          <span class="cat-sort">排序: {{ cat.sort }}</span>
          <div class="cat-actions">
            <el-button size="small" text @click="openAdd(cat.id)">添加子分类</el-button>
            <el-button size="small" text type="primary" @click="openEdit(cat)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(cat)">删除</el-button>
          </div>
        </div>
        <div v-if="cat.children?.length" class="cat-children">
          <div v-for="child in cat.children" :key="child.id" class="cat-child">
            <span class="cat-name">{{ child.name }}</span>
            <span class="cat-sort">排序: {{ child.sort }}</span>
            <div class="cat-actions">
              <el-button size="small" text type="primary" @click="openEdit(child)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDelete(child)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showAdd" :title="editId ? '编辑分类' : '新增分类'" width="400px">
      <el-form label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; margin: 0; }

.cat-group { background: #fff; border-radius: 8px; margin-bottom: 12px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.05); }
.cat-parent {
  display: flex; align-items: center; gap: 16px; padding: 14px 20px;
  background: #fafafa; font-weight: 500;
}
.cat-children { padding: 0 20px; }
.cat-child {
  display: flex; align-items: center; gap: 16px; padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.cat-child:last-child { border-bottom: none; }
.cat-name { flex: 1; font-size: 14px; }
.cat-sort { font-size: 12px; color: #999; }
.cat-actions { display: flex; gap: 4px; }
</style>
