<template>
  <div class="complaint-page">
    <div class="complaint-inner">
      <div class="complaint-card">
        <h2 class="complaint-title">
          <el-icon :size="24"><WarningFilled /></el-icon>
          投诉反馈
        </h2>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          label-position="top"
          size="large"
          @submit.prevent="submit"
        >
          <el-form-item label="产品防伪码" prop="securityCode">
            <el-input
              v-model="form.securityCode"
              placeholder="请输入产品上的防伪码"
              clearable
            >
              <template #prefix>
                <el-icon><Ticket /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="投诉原因" prop="complaintReason">
            <el-input
              v-model="form.complaintReason"
              type="textarea"
              :rows="4"
              placeholder="请描述您遇到的问题"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submit">
              提交投诉
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitComplaint } from '@/api/consumer'
import { WarningFilled, Ticket } from '@element-plus/icons-vue'

const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  securityCode: '',
  productName: '',
  batchNumber: '',
  complaintReason: ''
})

const rules = {
  securityCode: [{ required: true, message: '请输入防伪码', trigger: 'blur' }],
  complaintReason: [{ required: true, message: '请描述投诉原因', trigger: 'blur' }]
}

onMounted(() => {
  if (route.query.code) {
    form.securityCode = route.query.code
  }
})

function resetForm() {
  formRef.value?.resetFields()
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await submitComplaint({
      antiFakeCode: form.securityCode,
      complaintReason: form.complaintReason
    })
    ElMessage.success('投诉已提交，我们会尽快处理')
    resetForm()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.complaint-page {
  padding: 40px 20px;
  min-height: calc(100vh - 200px);
  background: #f5f7fa;
}

.complaint-inner {
  max-width: 640px;
  margin: 0 auto;
}

.complaint-card {
  background: #fff;
  border-radius: 12px;
  padding: 36px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.complaint-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 28px;
  font-size: 22px;
  color: #303133;
}
</style>
