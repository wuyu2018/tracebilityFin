<template>
  <div class="home">
    <section class="hero">
      <div class="hero-inner">
        <h1 class="hero-title">食品安全 · 全链溯源</h1>
        <p class="hero-desc">扫描产品防伪码或输入追溯码，即可查询从原料采购到生产加工、仓储运输至销售终端的全链路信息</p>
        <div class="hero-search">
          <el-input
            v-model="code"
            placeholder="请输入产品防伪码 / 追溯码"
            size="large"
            clearable
            @keyup.enter="goVerify"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="large" @click="goVerify">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </div>
      </div>
    </section>

    <section class="features">
      <div class="features-inner">
        <div class="feature-card" v-for="f in features" :key="f.title">
          <el-icon :size="40" :color="f.color"><component :is="f.icon" /></el-icon>
          <h3>{{ f.title }}</h3>
          <p>{{ f.desc }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Checked, Document, Connection } from '@element-plus/icons-vue'

const router = useRouter()
const code = ref('')

const goVerify = () => {
  if (!code.value.trim()) {
    router.push('/verify')
    return
  }
  router.push({ path: '/verify', query: { code: code.value.trim() } })
}

const features = [
  { icon: Search, color: '#409eff', title: '防伪验证', desc: '验证产品防伪码真伪，保障消费安全' },
  { icon: Document, color: '#67c23a', title: '全链追溯', desc: '从原料到销售，全链路透明可查' },
  { icon: Connection, color: '#e6a23c', title: '区块链存证', desc: '溯源数据上链存储，不可篡改' },
  { icon: Checked, color: '#f56c6c', title: '投诉反馈', desc: '在线提交质量问题，快速响应处理' }
]
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1a3a5c 0%, #2d6a9f 100%);
  padding: 80px 20px;
  text-align: center;
  color: #fff;
}

.hero-inner {
  max-width: 700px;
  margin: 0 auto;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 16px;
}

.hero-desc {
  font-size: 16px;
  opacity: 0.85;
  line-height: 1.6;
  margin-bottom: 36px;
}

.hero-search {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.hero-search .el-input {
  width: 420px;
}

.features {
  padding: 60px 20px;
  background: #fff;
}

.features-inner {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  text-align: center;
  padding: 32px 20px;
  border-radius: 12px;
  background: #f9fafc;
  transition: transform 0.2s, box-shadow 0.2s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.feature-card h3 {
  margin: 16px 0 8px;
  font-size: 18px;
  color: #303133;
}

.feature-card p {
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .hero-title { font-size: 28px; }
  .hero-search { flex-direction: column; align-items: center; }
  .hero-search .el-input { width: 100%; }
  .features-inner { grid-template-columns: repeat(2, 1fr); }
}
</style>
