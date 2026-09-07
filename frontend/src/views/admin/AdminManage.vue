<template>
  <div class="page-container">
    <div class="page-header">
      <h3>管理员管理</h3>
      <el-button type="primary" @click="openDialog" :disabled="!isSuperAdmin()">
        <el-icon><Plus /></el-icon>注册管理员
      </el-button>
    </div>

    <el-alert
      v-if="!isSuperAdmin()"
      title="仅有超级管理员可以管理管理员账号"
      type="warning"
      show-icon
      :closable="false"
      style="margin-bottom: 16px"
    />

    <el-table :data="admins" v-loading="loading" border stripe>
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="role" label="角色" width="140">
        <template #default="{ row }">
          <el-tag :type="row.role === 'SUPER_ADMIN' ? 'danger' : 'primary'" size="small">
            {{ row.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="agentType" label="Agent类型" width="100">
        <template #default="{ row }">
          {{ row.agentType || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="companyId" label="所属公司ID" width="120" />
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-popconfirm title="确定删除该管理员？" @confirm="handleDeleteAdmin(row.id)">
            <template #reference>
              <el-button size="small" type="danger" :disabled="row.username === auth.user?.username">
                删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="注册管理员" width="420px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="4-20位字母数字" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="8位以上，含字母+数字+特殊字符" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="管理员 (ADMIN)" value="ADMIN" />
            <el-option label="超级管理员 (SUPER_ADMIN)" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属公司" prop="companyId">
          <el-select v-model="form.companyId" placeholder="请选择公司" filterable style="width:100%" clearable>
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Agent类型">
          <el-select v-model="form.agentType" placeholder="可选" clearable style="width:100%">
            <el-option label="生产" value="PRODUCTION" />
            <el-option label="流通" value="CIRCULATION" />
            <el-option label="销售" value="SALES" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前管理员密码" prop="currentPassword">
          <el-input v-model="form.currentPassword" type="password" show-password placeholder="请输入当前登录密码以验证身份" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { registerAdmin, getCompanies, getAdmins, deleteAdmin } from '@/api/admin'
import { useAuth } from '@/stores/auth'
import { Plus } from '@element-plus/icons-vue'

const { auth, isSuperAdmin } = useAuth()

const companies = ref([])
const admins = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  role: 'ADMIN',
  agentType: '',
  companyId: '',
  currentPassword: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名' }, { min: 4, max: 20, message: '4-20位', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码' },
    { min: 8, message: '至少8位', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/, message: '需含字母+数字+特殊字符', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色' }],
  currentPassword: [{ required: true, message: '请输入当前管理员密码' }]
}

function resetForm() {
  formRef.value?.resetFields()
  form.username = ''
  form.password = ''
  form.role = 'ADMIN'
  form.agentType = ''
  form.companyId = ''
  form.currentPassword = ''
}

async function openDialog() {
  try {
    const res = await getCompanies()
    companies.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } catch { /* */ }
  resetForm()
  dialogVisible.value = true
}

async function fetchAdmins() {
  loading.value = true
  try {
    const res = await getAdmins()
    admins.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { loading.value = false }
}

async function handleDeleteAdmin(id) {
  try {
    await deleteAdmin(id)
    ElMessage.success('删除成功')
    fetchAdmins()
  } catch { /* */ }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await registerAdmin({
      username: form.username,
      password: form.password,
      role: form.role,
      agentType: form.agentType || undefined,
      companyId: form.companyId ? String(form.companyId) : undefined,
      currentPassword: form.currentPassword,
      currentAdminUsername: auth.user?.username
    })
    ElMessage.success('管理员注册成功')
    dialogVisible.value = false
    fetchAdmins()
  } finally { saving.value = false }
}

onMounted(fetchAdmins)
</script>

<style scoped>
.page-container { min-height: calc(100vh - 140px); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 20px; color: #303133; }
</style>
