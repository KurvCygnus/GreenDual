<template>
  <div class="bill-overview">
    <div class="bill-card">
      <div class="bill-title">本月能耗概览</div>
      <div class="bill-stats">
        <div class="bill-stat-item">
          <div class="bill-stat-label">总能耗</div>
          <div class="bill-stat-value">
            {{ (totalConsumption || 0).toFixed(1) }}
            <small>kWh</small>
          </div>
        </div>
        <div class="bill-stat-item">
          <div class="bill-stat-label">总费用</div>
          <div class="bill-stat-value">
            ¥{{ formatMoney(totalCost || 0) }}
          </div>
        </div>
        <div class="bill-stat-item">
          <div class="bill-stat-label">节约能耗</div>
          <div class="bill-stat-value success">
            {{ (savedConsumption || 0).toFixed(1) }}
            <small>kWh</small>
          </div>
        </div>
        <div class="bill-stat-item">
          <div class="bill-stat-label">节约费用</div>
          <div class="bill-stat-value success">
            ¥{{ formatMoney(savedCost || 0) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  totalConsumption: { type: Number, default: 0 },
  totalCost: { type: Number, default: 0 },
  savedConsumption: { type: Number, default: 0 },
  savedCost: { type: Number, default: 0 },
})

const formatMoney = (val) => {
  const num = Number(val) || 0
  return num.toLocaleString("zh-CN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<style scoped>
.bill-overview {
  display: grid;
  gap: 20px;
}
.bill-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  color: #fff;
}
.bill-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}
.bill-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.bill-stat-item {
  text-align: center;
}
.bill-stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}
.bill-stat-value {
  font-size: 28px;
  font-weight: 700;
}
.bill-stat-value small {
  font-size: 14px;
  font-weight: 400;
  opacity: 0.8;
}
.bill-stat-value.success {
  color: #95de64;
}
</style>
