<template>
  <el-select
    :model-value="modelValue"
    v-bind="$attrs"
    filterable
    remote
    :remote-method="search"
    :loading="loading"
    placeholder="请选择批次"
    style="width:100%"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-option
      v-for="item in options"
      :key="item.id"
      :label="`${item.batchNumber} - ${item.productName || '未知产品'}`"
      :value="item.id"
    />
  </el-select>
</template>

<script setup>
import { ref } from 'vue'
import { getBatchSelectOptions } from '@/api/admin'

defineProps({ modelValue: { type: [String, Number], default: '' } })
const emit = defineEmits(['update:modelValue'])

const options = ref([])
const loading = ref(false)

async function search(keyword) {
  loading.value = true
  try {
    const res = await getBatchSelectOptions({ keyword })
    options.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { loading.value = false }
}

search('')
</script>
