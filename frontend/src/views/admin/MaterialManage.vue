<template>
  <div class="page-container">
    <div class="page-header">
      <h3>原料管理</h3>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="原料品种" name="variety">
        <div class="tab-header">
          <el-button type="primary" @click="openVarietyDialog()">
            <el-icon><Plus /></el-icon>新增品种
          </el-button>
        </div>
        <el-table :data="varieties" v-loading="varietyLoading" border stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="品种名称" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isActive ? 'success' : 'info'" size="small">
                {{ row.isActive ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="320">
            <template #default="{ row }">
              <el-button size="small" @click="openVarietyDialog(row)">编辑</el-button>
              <el-button
                size="small"
                :type="row.isActive ? 'warning' : 'success'"
                @click="toggleVarietyActive(row)"
              >
                {{ row.isActive ? '禁用' : '启用' }}
              </el-button>
              <el-popconfirm title="确定删除？" @confirm="deleteVariety(row.id)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="原料采购" name="purchase">
        <div class="tab-header">
          <el-button type="primary" @click="openPurchaseDialog()">
            <el-icon><Plus /></el-icon>新增采购记录
          </el-button>
        </div>
        <el-table :data="purchases" v-loading="purchaseLoading" border stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="原料品种" width="120">
            <template #default="{ row }">{{ row.material?.name || row.materialName || '-' }}</template>
          </el-table-column>
          <el-table-column prop="batchNumber" label="采购批次" />
          <el-table-column prop="supplierName" label="供应商" />
          <el-table-column prop="producerName" label="生产商" />
          <el-table-column prop="purchaseDate" label="采购日期" width="120" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="openPurchaseDialog(row)">编辑</el-button>
              <el-popconfirm title="确定删除？" @confirm="deletePurchase(row.id)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="varietyDialogVisible" :title="editingVarietyId ? '编辑品种' : '新增品种'" width="400px" @closed="resetVarietyForm">
      <el-form ref="varietyFormRef" :model="varietyForm" :rules="{ name: [{ required: true, message: '请输入品种名称' }] }" label-width="80px">
        <el-form-item label="品种名称" prop="name">
          <el-input v-model="varietyForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="varietyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveVariety">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="purchaseDialogVisible" :title="editingPurchaseId ? '编辑采购' : '新增采购'" width="520px" @closed="resetPurchaseForm">
      <el-form ref="purchaseFormRef" :model="purchaseForm" :rules="purchaseRules" label-width="80px">
        <el-form-item label="原料品种" prop="materialId">
          <el-select v-model="purchaseForm.materialId" placeholder="请选择" style="width:100%">
            <el-option v-for="v in varieties" :key="v.id" :label="v.name" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="采购批次" prop="batchNumber">
          <el-input v-model="purchaseForm.batchNumber" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierName">
          <el-input v-model="purchaseForm.supplierName" />
        </el-form-item>
        <el-form-item label="生产商" prop="producerName">
          <el-input v-model="purchaseForm.producerName" />
        </el-form-item>
        <el-form-item label="产地" prop="producerAddress">
          <el-input v-model="purchaseForm.producerAddress" />
        </el-form-item>
        <el-form-item label="采购日期" prop="purchaseDate">
          <el-date-picker v-model="purchaseForm.purchaseDate" type="datetime" placeholder="选择日期" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="purchaseForm.quantity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="purchaseForm.unit" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="purchaseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePurchase">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getMaterialVarieties, createMaterialVariety, updateMaterialVariety, deleteMaterialVariety,
  activateMaterialVariety, deactivateMaterialVariety,
  getMaterialPurchases, createMaterialPurchase, updateMaterialPurchase, deleteMaterialPurchase
} from '@/api/admin'
import { Plus } from '@element-plus/icons-vue'

const activeTab = ref('variety')
const varieties = ref([])
const purchases = ref([])
const varietyLoading = ref(false)
const purchaseLoading = ref(false)
const saving = ref(false)

const varietyDialogVisible = ref(false)
const editingVarietyId = ref(null)
const varietyFormRef = ref(null)
const varietyForm = reactive({ name: '' })

const purchaseDialogVisible = ref(false)
const editingPurchaseId = ref(null)
const purchaseFormRef = ref(null)
const purchaseForm = reactive({
  materialId: '', batchNumber: '', supplierName: '', producerName: '',
  producerAddress: '', purchaseDate: '', quantity: 0, unit: ''
})

const purchaseRules = {
  materialId: [{ required: true, message: '请选择原料品种' }],
  batchNumber: [{ required: true, message: '请输入采购批次' }],
  supplierName: [{ required: true, message: '请输入供应商' }]
}

async function fetchVarieties() {
  varietyLoading.value = true
  try {
    const res = await getMaterialVarieties()
    varieties.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { varietyLoading.value = false }
}

async function fetchPurchases() {
  purchaseLoading.value = true
  try {
    const res = await getMaterialPurchases()
    purchases.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { purchaseLoading.value = false }
}

watch(activeTab, (tab) => {
  if (tab === 'variety') fetchVarieties()
  else fetchPurchases()
})

function openVarietyDialog(row) {
  if (row) {
    editingVarietyId.value = row.id
    varietyForm.name = row.name
  } else {
    editingVarietyId.value = null
    varietyForm.name = ''
  }
  varietyDialogVisible.value = true
}

function resetVarietyForm() {
  varietyFormRef.value?.resetFields()
  varietyForm.name = ''
}

async function saveVariety() {
  const valid = await varietyFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingVarietyId.value) {
      await updateMaterialVariety(editingVarietyId.value, { name: varietyForm.name })
    } else {
      await createMaterialVariety({ name: varietyForm.name })
    }
    ElMessage.success('保存成功')
    varietyDialogVisible.value = false
    fetchVarieties()
  } finally { saving.value = false }
}

async function toggleVarietyActive(row) {
  try {
    if (row.isActive) {
      await deactivateMaterialVariety(row.id)
    } else {
      await activateMaterialVariety(row.id)
    }
    ElMessage.success('操作成功')
    fetchVarieties()
  } catch { /* */ }
}

async function deleteVariety(id) {
  try {
    await deleteMaterialVariety(id)
    ElMessage.success('删除成功')
    fetchVarieties()
  } catch { /* */ }
}

function openPurchaseDialog(row) {
  if (row) {
    editingPurchaseId.value = row.id
    purchaseForm.materialId = row.material?.id || row.materialId || ''
    purchaseForm.batchNumber = row.batchNumber || ''
    purchaseForm.supplierName = row.supplierName || ''
    purchaseForm.producerName = row.producerName || ''
    purchaseForm.producerAddress = row.producerAddress || ''
    purchaseForm.purchaseDate = row.purchaseDate || ''
    purchaseForm.quantity = row.quantity || 0
    purchaseForm.unit = row.unit || ''
  } else {
    editingPurchaseId.value = null
    resetPurchaseForm()
  }
  purchaseDialogVisible.value = true
}

function resetPurchaseForm() {
  purchaseFormRef.value?.resetFields()
  Object.assign(purchaseForm, {
    materialId: '', batchNumber: '', supplierName: '', producerName: '',
    producerAddress: '', purchaseDate: '', quantity: 0, unit: ''
  })
}

async function savePurchase() {
  const valid = await purchaseFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingPurchaseId.value) {
      await updateMaterialPurchase(editingPurchaseId.value, { ...purchaseForm })
    } else {
      await createMaterialPurchase({ ...purchaseForm })
    }
    ElMessage.success('保存成功')
    purchaseDialogVisible.value = false
    fetchPurchases()
  } finally { saving.value = false }
}

async function deletePurchase(id) {
  try {
    await deleteMaterialPurchase(id)
    ElMessage.success('删除成功')
    fetchPurchases()
  } catch { /* */ }
}

fetchVarieties()
fetchPurchases()
</script>

<style scoped>
.page-container { min-height: calc(100vh - 140px); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 20px; color: #303133; }
.tab-header { margin-bottom: 16px; }
</style>
