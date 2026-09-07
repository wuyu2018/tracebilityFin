<template>
  <div class="page-container">
    <div class="page-header">
      <h3>仓储管理</h3>
      <el-button type="primary" @click="openDialog">
        <el-icon><Plus /></el-icon>新增仓储记录
      </el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="批次" min-width="200">
        <template #default="{ row }">
          <span v-if="row.batchNumber">{{ row.batchNumber }}<br><small style="color:#909399">{{ row.productName || '' }}</small></span>
          <span v-else>{{ row.batch?.batchNumber || row.batchId || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="storageTime" label="入库时间" width="180" />
      <el-table-column prop="outboundTime" label="出库时间" width="180" />
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="warehouseLocation" label="仓储位置" min-width="160" />
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增仓储记录" width="460px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="选择批次" prop="batchId">
          <BatchSelect v-model="form.batchId" />
        </el-form-item>
        <el-form-item label="入库时间" prop="storageTime">
          <el-date-picker v-model="form.storageTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="出库时间">
          <el-date-picker v-model="form.outboundTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" />
        </el-form-item>
        <el-form-item label="仓储位置" prop="warehouseLocation">
          <el-input v-model="form.warehouseLocation" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getStorages, createStorage } from '@/api/admin'
import BatchSelect from '@/components/admin/BatchSelect.vue'
import { Plus } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({ batchId: '', storageTime: '', outboundTime: '', quantity: 0, unit: '', warehouseLocation: '' })
const rules = {
  batchId: [{ required: true, message: '请选择批次' }],
  storageTime: [{ required: true, message: '请选择入库时间' }],
  warehouseLocation: [{ required: true, message: '请输入仓储位置' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getStorages()
    list.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { loading.value = false }
}

async function openDialog() {
  resetForm()
  dialogVisible.value = true
}
function resetForm() { formRef.value?.resetFields(); form.batchId = ''; form.storageTime = ''; form.outboundTime = ''; form.quantity = 0; form.unit = ''; form.warehouseLocation = '' }

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createStorage({ ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

fetchList()
</script>

<style scoped>
.page-container { min-height: calc(100vh - 140px); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 20px; color: #303133; }
</style>
