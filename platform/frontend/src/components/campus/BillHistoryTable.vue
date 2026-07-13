<template>
  <el-table :data="billHistory" style="width: 100%; margin-top: 20px" v-loading="loading">
    <el-table-column prop="billMonth" label="账单月份" width="120" />
    <el-table-column prop="totalConsumption" label="总能耗(kWh)" width="120">
      <template #default="scope">
        {{ (scope.row.totalConsumption || 0).toFixed(1) }}
      </template>
    </el-table-column>
    <el-table-column prop="totalCost" label="总费用(元)" width="120">
      <template #default="scope">
        ¥{{ formatMoney(scope.row.totalCost || 0) }}
      </template>
    </el-table-column>
    <el-table-column prop="savedConsumption" label="节约能耗(kWh)" width="130">
      <template #default="scope">
        <span style="color: #67c23a">{{ (scope.row.savedConsumption || 0).toFixed(1) }}</span>
      </template>
    </el-table-column>
    <el-table-column prop="savedCost" label="节约费用(元)" width="120">
      <template #default="scope">
        <span style="color: #67c23a">¥{{ formatMoney(scope.row.savedCost || 0) }}</span>
      </template>
    </el-table-column>
    <el-table-column prop="savingRate" label="节能率(%)" width="100" v-if="showSavingRate">
      <template #default="scope">
        <el-tag :type="getSavingTagType(scope.row.savingRate)">
          {{ (scope.row.savingRate || 0).toFixed(1) }}%
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="100">
      <template #default="scope">
        <el-button size="small" @click="$emit('detail', scope.row)">详情</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
defineProps({
  billHistory: { type: Array, default: () => [] },
  showSavingRate: { type: Boolean, default: true },
  loading: { type: Boolean, default: false },
})

defineEmits(["detail"])

const formatMoney = (val) => {
  const num = Number(val) || 0
  return num.toLocaleString("zh-CN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const getSavingTagType = (rate) => {
  const r = Number(rate) || 0
  if (r >= 30) return "success"
  if (r >= 15) return "warning"
  return "danger"
}
</script>
