<template>
  <div class="intro-page">
    <div class="intro-inner">
      <h2 class="intro-heading">系统介绍</h2>

      <div class="intro-section">
        <h3>关于食品溯源平台</h3>
        <p>
          本平台是一套基于区块链技术的食品安全全链路溯源系统，覆盖从原料采购、生产加工、
          仓储运输到终端销售的全过程，为消费者提供透明、可信的产品溯源服务。
          消费者可通过扫描产品防伪码或输入追溯码，查询产品的全链路信息。
        </p>
      </div>

      <div class="intro-section">
        <h3>核心功能</h3>
        <el-row :gutter="20">
          <el-col :md="8" :sm="12" v-for="f in features" :key="f.title">
            <div class="intro-card">
              <el-icon :size="32" :color="f.color"><component :is="f.icon" /></el-icon>
              <h4>{{ f.title }}</h4>
              <p>{{ f.desc }}</p>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="intro-section">
        <h3>溯源流程</h3>
        <div class="steps-desktop">
          <el-steps :active="4" align-center>
            <el-step title="扫描防伪码" description="扫描产品包装上的防伪二维码" />
            <el-step title="验证真伪" description="系统验证防伪码真实性" />
            <el-step title="追溯信息" description="查看原料/生产/物流全链路信息" />
            <el-step title="区块链存证" description="关键数据上链，不可篡改" />
          </el-steps>
        </div>
        <div class="steps-mobile">
          <ol class="steps-list">
            <li><strong>扫描防伪码</strong> — 扫描产品包装上的防伪二维码</li>
            <li><strong>验证真伪</strong> — 系统验证防伪码真实性</li>
            <li><strong>追溯信息</strong> — 查看原料/生产/物流全链路信息</li>
            <li><strong>区块链存证</strong> — 关键数据上链，不可篡改</li>
          </ol>
        </div>
      </div>

      <div class="intro-section">
        <h3>技术特点</h3>
        <ul class="tech-list">
          <li>区块链存证：关键溯源数据上链存储，确保数据不可篡改</li>
          <li>防伪码机制：每件产品绑定唯一防伪码，一物一码</li>
          <li>多智能体协同：生产/流通/销售环节多Agent协同保障数据完整性</li>
          <li>全链路覆盖：从原料到终端销售，全环节可追溯</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Search, Checked, Connection, WarningFilled } from '@element-plus/icons-vue'

const features = [
  { icon: Search, color: '#409eff', title: '防伪验证', desc: '一物一码，验证产品真伪' },
  { icon: Checked, color: '#67c23a', title: '全链溯源', desc: '原料到销售全程可查' },
  { icon: Connection, color: '#e6a23c', title: '区块链存证', desc: '数据上链不可篡改' },
  { icon: WarningFilled, color: '#f56c6c', title: '投诉反馈', desc: '质量问题快速响应' },
  { icon: Search, color: '#909399', title: '透明公开', desc: '溯源信息透明可查' },
  { icon: Checked, color: '#409eff', title: '智能协同', desc: '多Agent系统协同保障' }
]
</script>

<style scoped>
.intro-page {
  padding: 40px 20px 60px;
  min-height: calc(100vh - 200px);
}

.intro-inner {
  max-width: 960px;
  margin: 0 auto;
}

.intro-heading {
  font-size: 28px;
  text-align: center;
  margin-bottom: 36px;
  color: #303133;
}

.intro-section {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
}

.intro-section h3 {
  font-size: 18px;
  margin-bottom: 14px;
  color: #303133;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.intro-section p {
  font-size: 15px;
  line-height: 1.8;
  color: #606266;
}

.intro-card {
  text-align: center;
  padding: 24px 16px;
  border-radius: 10px;
  background: #f9fafc;
  margin-bottom: 16px;
  transition: transform 0.2s;
}

.intro-card:hover {
  transform: translateY(-2px);
}

.intro-card h4 {
  margin: 12px 0 6px;
  font-size: 16px;
  color: #303133;
}

.intro-card p {
  font-size: 13px;
}

.tech-list {
  padding-left: 20px;
}

.tech-list li {
  font-size: 15px;
  line-height: 2;
  color: #606266;
}

.steps-desktop { display: block; }
.steps-mobile { display: none; }
.steps-list { padding-left: 20px; }
.steps-list li { font-size: 14px; line-height: 2; color: #606266; }
@media (max-width: 768px) {
  .steps-desktop { display: none; }
  .steps-mobile { display: block; }
}
</style>
