<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <el-icon :size="36" color="#409eff"><Checked /></el-icon>
        <h2>食品溯源管理后台</h2>
        <p>管理员登录</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" clearable>
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="验证码" maxlength="5" style="flex:1">
              <template #prefix><el-icon><Key /></el-icon></template>
            </el-input>
            <div class="captcha-display" @click="refreshCaptcha">{{ captchaText }}</div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/stores/auth'
import { Checked, User, Lock, Key } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const { doLogin } = useAuth()

const formRef = ref(null)
const loading = ref(false)
const captchaValue = ref('')

const form = reactive({
  username: '',
  password: '',
  captcha: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 5, max: 5, message: '验证码为5位', trigger: 'blur' }
  ]
}

const captchaText = ref('')

function refreshCaptcha() {
  let code = ''
  for (let i = 0; i < 5; i++) {
    code += Math.floor(Math.random() * 10)
  }
  captchaValue.value = code
  captchaText.value = code
  form.captcha = ''
}

onMounted(refreshCaptcha)

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.captcha !== captchaValue.value) {
    ElMessage.error('验证码错误')
    refreshCaptcha()
    return
  }
  loading.value = true
  try {
    await doLogin(form.username, form.password, form.captcha)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/manage/dashboard'
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a3a5c 0%, #2d6a9f 100%);
}

.login-card {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h2 {
  margin: 10px 0 6px;
  font-size: 22px;
  color: #303133;
}

.login-header p {
  font-size: 14px;
  color: #909399;
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-display {
  flex-shrink: 0;
  width: 100px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 4px;
  color: #409eff;
  background: #ecf5ff;
  border-radius: 6px;
  cursor: pointer;
  user-select: none;
  font-style: italic;
  text-decoration: line-through;
}
</style>
