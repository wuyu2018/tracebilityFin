<template>
  <div class="page-container">
    <div class="page-header">
      <h3>投诉管理</h3>
      <el-popconfirm title="确定删除所选投诉？" @confirm="handleBatchDelete">
        <template #reference>
          <el-button type="danger" :disabled="!selectedIds.length">
            批量删除 ({{ selectedIds.length }})
          </el-button>
        </template>
      </el-popconfirm>
    </div>

    <el-table :data="list" v-loading="loading" @selection-change="onSelectionChange" border stripe>
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="securityCode" label="防伪码" width="220" show-overflow-tooltip />
      <el-table-column prop="complaintReason" label="投诉原因" min-width="200" show-overflow-tooltip />
      <el-table-column prop="complaintTime" label="投诉时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getComplaints, deleteComplaint, batchDeleteComplaints } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const selectedIds = ref([])

function onSelectionChange(selection) {
  selectedIds.value = selection.map(s => s.id)
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getComplaints()
    list.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { loading.value = false }
}

async function handleDelete(id) {
  try {
    await deleteComplaint(id)
    ElMessage.success('删除成功')
    fetchList()
  } catch { /* */ }
}

async function handleBatchDelete() {
  try {
    await batchDeleteComplaints(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchList()
  } catch { /* */ }
}

fetchList()
</script>

<style scoped>
.page-container { min-height: calc(100vh - 140px); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 20px; color: #303133; }
</style>
