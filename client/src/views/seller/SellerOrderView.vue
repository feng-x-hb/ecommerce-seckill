<script setup lang="ts">
/**
 * 商家订单管理页
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref<number | null>(null)

const detailVisible = ref(false)
const detail = ref<any>(null)

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待支付', type: 'info' },
  1: { label: '已支付', type: 'warning' },
  2: { label: '已发货', type: 'primary' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'danger' },
  5: { label: '已关闭', type: 'info' },
}

async function fetchData() {
  loading.value = true
  try {
    let url = `/seller/order/list?page=${page.value}&size=${size.value}`
    if (statusFilter.value !== null) url += `&status=${statusFilter.value}`
    const res = await request.get(url)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function handleSearch() { page.value = 1; fetchData() }

async function viewDetail(orderNo: string) {
  const res = await request.get(`/seller/order/${orderNo}`)
  detail.value = res.data
  detailVisible.value = true
}

async function handleShip(orderNo: string) {
  await ElMessageBox.confirm(`确定订单 ${orderNo} 已发货？`, '发货确认', { type: 'info' })
  await request.put(`/seller/order/${orderNo}/ship`)
  ElMessage.success('已标记为发货')
  fetchData()
}

function handleSizeChange(val: number) { size.value = val; page.value = 1; fetchData() }
function handleCurrentChange(val: number) { page.value = val; fetchData() }

onMounted(fetchData)
</script>

<template>
  <div class="admin-page">
    <div class="search-bar">
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px" @change="handleSearch">
        <el-option v-for="(info, code) in statusMap" :key="code" :label="info.label" :value="Number(code)" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="payAmount" label="实付金额" width="110"><template #default="{ row }">¥{{ row.payAmount }}</template></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="(statusMap[row.status]?.type as any) || 'info'" size="small">{{ statusMap[row.status]?.label || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="170"><template #default="{ row }">{{ row.createdAt?.replace('T', ' ').slice(0, 19) }}</template></el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="viewDetail(row.orderNo)">详情</el-button>
          <el-button v-if="row.status === 1" type="success" size="small" link @click="handleShip(row.orderNo)">发货</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="650px">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="(statusMap[detail.status]?.type as any) || 'info'" size="small">{{ statusMap[detail.status]?.label }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ detail.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detail.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detail.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px">商品明细</h4>
        <el-table :data="detail.items" size="small" border>
          <el-table-column prop="productName" label="商品" min-width="160" show-overflow-tooltip />
          <el-table-column prop="specDesc" label="规格" width="120" show-overflow-tooltip />
          <el-table-column prop="price" label="单价" width="90"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column prop="subTotal" label="小计" width="90"><template #default="{ row }">¥{{ row.subTotal }}</template></el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; align-items: center; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
