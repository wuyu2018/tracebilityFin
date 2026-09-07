<template>
  <div class="page-container" v-loading="loading">
    <div class="detail-header">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <h3>批次详情 — {{ detail.batch?.batchNumber || '-' }}</h3>
    </div>

    <el-row :gutter="20">
      <el-col :md="12" :sm="24">
        <el-card header="产品信息" shadow="hover">
          <el-descriptions :column="1" border v-if="detail.product">
            <el-descriptions-item label="产品名称">{{ detail.product.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="规格">{{ detail.product.specification || '-' }}</el-descriptions-item>
            <el-descriptions-item label="保质期">{{ detail.product.shelfLife || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.product.contactPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系邮箱">{{ detail.product.contactEmail || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无" />
        </el-card>
      </el-col>
      <el-col :md="12" :sm="24">
        <el-card header="批次信息" shadow="hover">
          <el-descriptions :column="1" border v-if="detail.batch">
            <el-descriptions-item label="批次号">{{ detail.batch.batchNumber || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生产日期">{{ detail.batch.productionDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="保质期">{{ detail.batch.shelfLife || '-' }}</el-descriptions-item>
            <el-descriptions-item label="数量">{{ detail.batch.quantity }} {{ detail.batch.unit }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detail.batch.createdAt || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无" />
        </el-card>
      </el-col>
    </el-row>

    <el-card header="原料清单" shadow="hover" style="margin-top: 20px;">
      <el-table v-if="detail.materials && detail.materials.length" :data="detail.materials" border stripe>
        <el-table-column prop="materialName" label="原料名称" />
        <el-table-column prop="batchNumber" label="采购批次" />
        <el-table-column prop="supplierName" label="供应商" />
        <el-table-column prop="producerName" label="生产商" />
        <el-table-column prop="producerAddress" label="产地" min-width="120" />
        <el-table-column prop="purchaseDate" label="采购日期" width="160" />
        <el-table-column label="数量" width="100">
          <template #default="{ row }">{{ row.quantity }} {{ row.unit }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无原料记录" />
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :md="12" :sm="24">
        <el-card header="质检信息" shadow="hover">
          <el-descriptions :column="1" border v-if="detail.inspection">
            <el-descriptions-item label="样品名称">{{ detail.inspection.sampleName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="样品数量">{{ detail.inspection.sampleQuantity }}</el-descriptions-item>
            <el-descriptions-item label="样品规格">{{ detail.inspection.sampleSpecification || '-' }}</el-descriptions-item>
            <el-descriptions-item label="检验员">{{ detail.inspection.inspectorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="检验时间">{{ detail.inspection.inspectionTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="检验结果">
              <el-tag :type="detail.inspection.resultStatus === '合格' ? 'success' : 'danger'" size="small">
                {{ detail.inspection.resultStatus || '-' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="检验详情">{{ detail.inspection.resultDetail || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无质检记录" />
        </el-card>
      </el-col>
      <el-col :md="12" :sm="24">
        <el-card header="仓储信息" shadow="hover">
          <el-descriptions :column="1" border v-if="detail.storage">
            <el-descriptions-item label="入库时间">{{ detail.storage.storageTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="出库时间">{{ detail.storage.outboundTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="仓储位置">{{ detail.storage.warehouseLocation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="数量">{{ detail.storage.quantity }} {{ detail.storage.unit }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无仓储记录" />
        </el-card>
      </el-col>
    </el-row>

    <el-card header="运输销售信息" shadow="hover" style="margin-top: 20px;">
      <el-descriptions :column="2" border v-if="detail.transport">
        <el-descriptions-item label="运输公司">{{ detail.transport.transportCompany || '-' }}</el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ detail.transport.vehicleNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="运输时间">{{ detail.transport.time || '-' }}</el-descriptions-item>
        <el-descriptions-item label="销售区域">{{ detail.transport.salesRegion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detail.transport.receiverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货人电话">{{ detail.transport.receiverContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="环境温度">{{ detail.transport.environmentTemperature != null ? detail.transport.environmentTemperature + '℃' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品温度">{{ detail.transport.productTemperature != null ? detail.transport.productTemperature + '℃' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="记录人">{{ detail.transport.recorderName || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无运输记录" />
    </el-card>

    <el-card header="防伪码统计" shadow="hover" style="margin-top: 20px;">
      <el-row :gutter="20" v-if="detail.codes">
        <el-col :span="6">
          <el-statistic title="总计" :value="detail.codes.total" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="已激活">
            <template #default>
              <span class="stat-green">{{ detail.codes.active }}</span>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="未激活">
            <template #default>
              <span class="stat-gray">{{ detail.codes.inactive }}</span>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="已冻结">
            <template #default>
              <span class="stat-orange">{{ detail.codes.frozen }}</span>
            </template>
          </el-statistic>
        </el-col>
      </el-row>
      <el-empty v-else description="暂无防伪码" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getBatchDetail } from '@/api/admin'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(false)
const detail = ref({})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getBatchDetail(route.params.id)
    detail.value = res || {}
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-container { min-height: calc(100vh - 140px); }
.detail-header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.detail-header h3 { font-size: 20px; color: #303133; margin: 0; }
.stat-green { color: #67c23a; font-size: 28px; font-weight: 700; }
.stat-gray { color: #909399; font-size: 28px; font-weight: 700; }
.stat-orange { color: #e6a23c; font-size: 28px; font-weight: 700; }
</style>
