<template>
  <div class="page-container">
    <div class="page-header">
      <h3>运输销售管理</h3>
      <el-button type="primary" @click="openDialog">
        <el-icon><Plus /></el-icon>新增记录
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
      <el-table-column prop="transportCompany" label="运输公司" />
      <el-table-column prop="vehicleNumber" label="车牌号" width="120" />
      <el-table-column prop="time" label="运输时间" width="180" />
      <el-table-column prop="salesRegion" label="销售区域" />
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="environmentTemperature" label="环境温度" width="100" />
      <el-table-column prop="productTemperature" label="产品温度" width="100" />
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增运输销售记录" width="520px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="选择批次" prop="batchId">
          <BatchSelect v-model="form.batchId" />
        </el-form-item>
        <el-form-item label="运输公司" prop="transportCompany">
          <el-input v-model="form.transportCompany" />
        </el-form-item>
        <el-form-item label="车牌号" prop="vehicleNumber">
          <el-input v-model="form.vehicleNumber" />
        </el-form-item>
        <el-form-item label="运输时间" prop="time">
          <el-date-picker v-model="form.time" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="销售区域" prop="salesRegion">
          <el-input v-model="form.salesRegion" />
        </el-form-item>
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" />
        </el-form-item>
        <el-form-item label="收货人电话">
          <el-input v-model="form.receiverContact" />
        </el-form-item>
        <el-form-item label="环境温度">
          <el-input-number v-model="form.environmentTemperature" :min="-50" :max="100" style="width:100%" />
        </el-form-item>
        <el-form-item label="产品温度">
          <el-input-number v-model="form.productTemperature" :min="-50" :max="100" style="width:100%" />
        </el-form-item>
        <el-form-item label="记录人">
          <el-input v-model="form.recorderName" />
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
import { getTransportSales, createTransportSale } from '@/api/admin'
import BatchSelect from '@/components/admin/BatchSelect.vue'
import { Plus } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  batchId: '', transportCompany: '', vehicleNumber: '', time: '', salesRegion: '',
  receiverName: '', receiverContact: '', environmentTemperature: null,
  productTemperature: null, recorderName: ''
})
const rules = {
  batchId: [{ required: true, message: '请选择批次' }],
  transportCompany: [{ required: true, message: '请输入运输公司' }],
  vehicleNumber: [{ required: true, message: '请输入车牌号' }],
  salesRegion: [{ required: true, message: '请输入销售区域' }],
  receiverName: [{ required: true, message: '请输入收货人' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getTransportSales()
    list.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { loading.value = false }
}

async function openDialog() {
  resetForm()
  dialogVisible.value = true
}
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    batchId: '', transportCompany: '', vehicleNumber: '', time: '', salesRegion: '',
    receiverName: '', receiverContact: '', environmentTemperature: null,
    productTemperature: null, recorderName: ''
  })
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createTransportSale({ ...form })
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
