<template>
  <div class="page-container">
    <div class="page-header">
      <h3>质检管理</h3>
      <el-button type="primary" @click="openDialog">
        <el-icon><Plus /></el-icon>新增质检记录
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
      <el-table-column prop="sampleName" label="样品名称" />
      <el-table-column prop="sampleSpecification" label="样品规格" />
      <el-table-column label="检验结果" width="100">
        <template #default="{ row }">
          <el-tag :type="row.resultStatus === '合格' ? 'success' : 'danger'" size="small">
            {{ row.resultStatus === '合格' ? '合格' : '不合格' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="inspectorName" label="检验员" width="100" />
      <el-table-column prop="inspectionTime" label="检验时间" width="180" />
      <el-table-column prop="resultDetail" label="检验详情" min-width="160" show-overflow-tooltip />
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增质检记录" width="520px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="选择批次" prop="batchId">
          <BatchSelect v-model="form.batchId" />
        </el-form-item>
        <el-form-item label="样品名称" prop="sampleName">
          <el-input v-model="form.sampleName" />
        </el-form-item>
        <el-form-item label="样品规格" prop="sampleSpecification">
          <el-input v-model="form.sampleSpecification" />
        </el-form-item>
        <el-form-item label="样品数量" prop="sampleQuantity">
          <el-input-number v-model="form.sampleQuantity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="检验结果" prop="qualified">
          <el-select v-model="form.qualified" style="width:100%">
            <el-option label="合格" :value="true" />
            <el-option label="不合格" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="不合格原因" prop="failReason" v-if="!form.qualified">
          <el-input v-model="form.failReason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="检验员" prop="inspectorName">
          <el-input v-model="form.inspectorName" />
        </el-form-item>
        <el-form-item label="检验时间" prop="inspectionTime">
          <el-date-picker v-model="form.inspectionTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
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
import { getInspections, createInspection } from '@/api/admin'
import BatchSelect from '@/components/admin/BatchSelect.vue'
import { Plus } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  batchId: '', sampleName: '', sampleSpecification: '', sampleQuantity: 0,
  qualified: true, failReason: '', inspectorName: '' , inspectionTime: ''
})
const rules = {
  batchId: [{ required: true, message: '请选择批次' }],
  sampleName: [{ required: true, message: '请输入样品名称' }],
  qualified: [{ required: true, message: '请选择检验结果' }],
  inspectorName: [{ required: true, message: '请输入检验员' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getInspections()
    list.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { loading.value = false }
}

async function openDialog() {
  resetForm()
  dialogVisible.value = true
}
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { batchId: '', sampleName: '', sampleSpecification: '', sampleQuantity: 0, qualified: true, failReason: '', inspectorName: '', inspectionTime: '' })
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createInspection({ ...form })
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
