<template>
  <div class="test-container">
    <h1>测试页面</h1>
    <p>如果看到这个页面，说明Vue路由和基本功能正常</p>
    <el-button type="primary">测试按钮</el-button>
  </div>
</template>

<script setup>

</script>

<style scoped>
.test-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

h1 {
  color: #67C23A;
}

p {
  font-size: 18px;
  color: #606266;
}
</style>
