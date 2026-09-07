<template>
  <div class="verify-page">
    <div class="verify-inner">
      <div class="verify-card">
        <h2 class="verify-title">
          <el-icon :size="24"><Search /></el-icon>
          产品追溯查询
        </h2>

        <div class="search-bar">
          <el-input
            v-model="code"
            placeholder="请输入产品防伪码 / 追溯码"
            size="large"
            clearable
            @keyup.enter="doVerify"
          >
            <template #prefix>
              <el-icon><Ticket /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="large" :loading="loading" @click="doVerify">
            查询
          </el-button>
        </div>

        <div class="scan-tip">
          <el-icon><Camera /></el-icon>
          也可直接扫描产品包装上的二维码进行查询
        </div>
      </div>

      <div v-if="result" class="result-area">
        <div v-if="result.valid === false" class="result-fail">
          <el-result icon="error" title="验证失败" :sub-title="result.message || '该防伪码无效'">
            <template #extra>
              <el-button type="primary" @click="$router.push({ name: 'Complaint', query: { code } })">
                投诉该产品
              </el-button>
            </template>
          </el-result>
        </div>

        <template v-else-if="resultData">
          <el-alert
            v-if="resultData.queryTip"
            :title="resultData.queryTip"
            type="warning"
            show-icon
            :closable="false"
            style="margin-bottom: 20px"
          />

          <div class="trace-chain">
            <h3 class="trace-section-title">
              <el-icon><Goods /></el-icon>
              产品信息
            </h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="产品名称">{{ resultData.product?.name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="规格">{{ resultData.product?.specification || '-' }}</el-descriptions-item>
              <el-descriptions-item label="保质期">{{ resultData.product?.shelfLife || '-' }}</el-descriptions-item>
              <el-descriptions-item label="生产日期">{{ resultData.batch?.productionDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="批次号">{{ resultData.batch?.batchNumber || '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="resultData.product?.imageUrl" label="产品图片">
                <el-image :src="resultData.product.imageUrl" style="width:120px" fit="contain">
                  <template #error><el-icon><Picture /></el-icon></template>
                </el-image>
              </el-descriptions-item>
              <el-descriptions-item label="厂家电话">{{ resultData.product?.contactPhone || '-' }}</el-descriptions-item>
            </el-descriptions>

            <h3 class="trace-section-title">
              <el-icon><Box /></el-icon>
              原料信息
            </h3>
            <div v-if="resultData.materials && resultData.materials.length">
              <el-table :data="resultData.materials" border stripe>
                <el-table-column prop="materialName" label="原料名称" />
                <el-table-column prop="batchNumber" label="采购批次" />
                <el-table-column prop="supplierName" label="供应商" />
                <el-table-column prop="producerAddress" label="产地" />
                <el-table-column prop="purchaseDate" label="采购日期" />
              </el-table>
            </div>
            <el-empty v-else description="暂无原料信息" />

            <h3 class="trace-section-title">
              <el-icon><Checked /></el-icon>
              质检信息
            </h3>
            <div v-if="resultData.inspection">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="检验员">{{ resultData.inspection.inspectorName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="检验时间">{{ resultData.inspection.inspectionTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="检验结果" :span="2">
                  <el-tag :type="resultData.inspection.resultStatus === '合格' ? 'success' : 'info'">
                    {{ resultData.inspection.resultStatus || '待定' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="检验详情" :span="2">{{ resultData.inspection.resultDetail || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else description="暂无质检信息" />

            <h3 class="trace-section-title">
              <el-icon><OfficeBuilding /></el-icon>
              仓储信息
            </h3>
            <div v-if="resultData.storage">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="入库时间">{{ resultData.storage.storageTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="出库时间">{{ resultData.storage.outboundTime || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else description="暂无仓储信息" />

            <h3 class="trace-section-title">
              <el-icon><Van /></el-icon>
              运输销售信息
            </h3>
            <div v-if="resultData.transportSale">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="运输时间">{{ resultData.transportSale.transportTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="销售区域">{{ resultData.transportSale.salesRegion || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else description="暂无运输信息" />
          </div>

          <div class="complaint-link">
            <el-button type="warning" @click="$router.push({ name: 'Complaint', query: { code } })">
              <el-icon><WarningFilled /></el-icon>
              投诉此产品
            </el-button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { verifyCode } from '@/api/consumer'
import { Search, Ticket, Camera, Goods, Box, Checked, OfficeBuilding, Van, WarningFilled, Picture } from '@element-plus/icons-vue'

const route = useRoute()
const code = ref('')
const loading = ref(false)
const result = ref(null)
const resultData = ref(null)

onMounted(() => {
  if (route.query.code) {
    code.value = route.query.code
    doVerify()
  }
})

async function doVerify() {
  if (!code.value.trim()) {
    ElMessage.warning('请输入防伪码')
    return
  }
  loading.value = true
  result.value = null
  resultData.value = null
  try {
    const res = await verifyCode(code.value.trim())
    result.value = res
    if (res.data) {
      resultData.value = res.data
    }
  } catch {
    result.value = { valid: false, message: '查询失败，请稍后重试' }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.verify-page {
  padding: 40px 20px;
  min-height: calc(100vh - 200px);
  background: #f5f7fa;
}

.verify-inner {
  max-width: 960px;
  margin: 0 auto;
}

.verify-card {
  background: #fff;
  border-radius: 12px;
  padding: 36px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.verify-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
  font-size: 22px;
  color: #303133;
}

.search-bar {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.search-bar .el-input {
  width: 420px;
}

.scan-tip {
  margin-top: 16px;
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.result-area {
  margin-top: 24px;
}

.result-fail {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.trace-chain {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.trace-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 28px 0 16px;
  font-size: 18px;
  color: #303133;
}

.trace-section-title:first-child {
  margin-top: 0;
}

.complaint-link {
  text-align: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .search-bar { flex-direction: column; align-items: center; }
  .search-bar .el-input { width: 100%; }
  .verify-card { padding: 20px 16px; }
  .trace-chain { padding: 20px 16px; }
}
</style>
