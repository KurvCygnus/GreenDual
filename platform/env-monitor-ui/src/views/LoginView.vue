<template>
  <div class="login-container">
    <!-- 动态背景元素 -->
    <div class="bg-animation">
      <div class="bg-bubble" v-for="n in 15" :key="n"></div>
      <!-- AI神经网络背景 -->
      <div class="ai-network">
        <div class="neural-node" v-for="n in 20" :key="'node-' + n"></div>
        <svg class="neural-connections">
          <line
            v-for="(line, index) in neuralLines"
            :key="'line-' + index"
            :x1="line.x1"
            :y1="line.y1"
            :x2="line.x2"
            :y2="line.y2"
            stroke="rgba(103, 194, 58, 0.2)"
            stroke-width="1"
          />
        </svg>
      </div>
    </div>

    <!-- 左侧装饰区域 -->
    <div class="login-left">
      <div class="brand-section">
        <div class="logo-wrapper">
          <div class="logo-ai-ring">
            <div class="logo-ai-ring-inner"></div>
          </div>
          <el-icon size="64" color="#fff" class="main-logo"
            ><Monitor
          /></el-icon>
        </div>
        <h1 class="brand-title">智绿云控</h1>
        <p class="brand-subtitle">AI协同的环境监测与智能节能管控平台</p>
        <div class="ai-badge">
          <el-icon size="16" color="#67C23A"><Cpu /></el-icon>
          <span>AI 智能驱动</span>
        </div>
        <div class="feature-list">
          <div class="feature-item">
            <div class="feature-icon-wrapper">
              <el-icon size="20" color="#67C23A"><Odometer /></el-icon>
            </div>
            <div class="feature-content">
              <span class="feature-title">环境温湿度实时监测</span>
              <span class="feature-desc">精准感知，毫秒级响应</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon-wrapper">
              <el-icon size="20" color="#67C23A"><Cpu /></el-icon>
            </div>
            <div class="feature-content">
              <span class="feature-title">AI 智能节能算法</span>
              <span class="feature-desc">自适应调节，降低30%能耗</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon-wrapper">
              <el-icon size="20" color="#67C23A"><Lightning /></el-icon>
            </div>
            <div class="feature-content">
              <span class="feature-title">智能路灯远程控制</span>
              <span class="feature-desc">云端管理，一键操控</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon-wrapper">
              <el-icon size="20" color="#67C23A"><TrendCharts /></el-icon>
            </div>
            <div class="feature-content">
              <span class="feature-title">数据可视化分析</span>
              <span class="feature-desc">多维报表，智能预警</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="login-header">
            <div class="avatar-wrapper">
              <el-icon size="40" color="#67C23A"><UserFilled /></el-icon>
            </div>
            <h2>欢迎登录</h2>
            <p>请使用您的账号密码登录系统</p>
          </div>
        </template>

        <el-form
          :model="loginForm"
          :rules="rules"
          ref="loginFormRef"
          label-position="top"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? "登录中..." : "登录" }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <p>© 2026 EcoSense. All rights reserved.</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  UserFilled,
  Monitor,
  Cpu,
  Odometer,
  Lightning,
  TrendCharts,
} from "@element-plus/icons-vue";
import { login } from "@/api/auth";

const router = useRouter();
const loginFormRef = ref(null);
const loading = ref(false);

// AI神经网络连接线数据
const neuralLines = ref([]);

// 生成神经网络连接线
const generateNeuralLines = () => {
  const lines = [];
  const nodeCount = 20;
  const connectionDensity = 0.3;

  for (let i = 0; i < nodeCount; i++) {
    for (let j = i + 1; j < nodeCount; j++) {
      if (Math.random() < connectionDensity) {
        lines.push({
          x1: Math.random() * 100 + "%",
          y1: Math.random() * 100 + "%",
          x2: Math.random() * 100 + "%",
          y2: Math.random() * 100 + "%",
        });
      }
    }
  }
  neuralLines.value = lines.slice(0, 30); // 限制线条数量
};

onMounted(() => {
  generateNeuralLines();
});

const loginForm = reactive({
  username: "",
  password: "",
});

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    {
      min: 2,
      max: 20,
      message: "用户名长度在 2 到 20 个字符",
      trigger: "blur",
    },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 3, max: 20, message: "密码长度在 3 到 20 个字符", trigger: "blur" },
  ],
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  try {
    await loginFormRef.value.validate();
    loading.value = true;

    const response = await login({
      username: loginForm.username,
      password: loginForm.password,
    });

    if (response.data.code === 1) {
      localStorage.setItem("token", response.data.data.token);
      localStorage.setItem("username", response.data.data.name);

      ElMessage.success("登录成功");
      router.push("/home");
    } else {
      ElMessage.error(response.data.msg || "登录失败，请检查用户名和密码");
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.msg || "用户名或密码错误");
    } else if (error.request) {
      ElMessage.error("无法连接到服务器，请检查网络或后端服务");
      console.error("网络错误:", error.request);
    } else {
      ElMessage.error("登录出错，请稍后重试");
      console.error("登录错误:", error);
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background: linear-gradient(
    135deg,
    #0d3328 0%,
    #1a5f3f 30%,
    #2d8a5a 60%,
    #4caf7c 100%
  );
}

.bg-animation {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.bg-bubble {
  position: absolute;
  bottom: -100px;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: rise 8s infinite ease-in;
}

.bg-bubble:nth-child(1) {
  left: 10%;
  animation-duration: 8s;
  animation-delay: 0s;
}
.bg-bubble:nth-child(2) {
  left: 20%;
  animation-duration: 10s;
  animation-delay: 2s;
}
.bg-bubble:nth-child(3) {
  left: 35%;
  animation-duration: 12s;
  animation-delay: 4s;
}
.bg-bubble:nth-child(4) {
  left: 50%;
  animation-duration: 9s;
  animation-delay: 1s;
}
.bg-bubble:nth-child(5) {
  left: 65%;
  animation-duration: 11s;
  animation-delay: 3s;
}
.bg-bubble:nth-child(6) {
  left: 80%;
  animation-duration: 7s;
  animation-delay: 5s;
}
.bg-bubble:nth-child(7) {
  left: 15%;
  animation-duration: 13s;
  animation-delay: 2.5s;
}
.bg-bubble:nth-child(8) {
  left: 45%;
  animation-duration: 8s;
  animation-delay: 4.5s;
}
.bg-bubble:nth-child(9) {
  left: 70%;
  animation-duration: 10s;
  animation-delay: 1.5s;
}
.bg-bubble:nth-child(10) {
  left: 25%;
  animation-duration: 12s;
  animation-delay: 3.5s;
}
.bg-bubble:nth-child(11) {
  left: 55%;
  animation-duration: 9s;
  animation-delay: 0.5s;
}
.bg-bubble:nth-child(12) {
  left: 85%;
  animation-duration: 11s;
  animation-delay: 2.5s;
}
.bg-bubble:nth-child(13) {
  left: 30%;
  animation-duration: 8s;
  animation-delay: 4s;
}
.bg-bubble:nth-child(14) {
  left: 60%;
  animation-duration: 10s;
  animation-delay: 1s;
}
.bg-bubble:nth-child(15) {
  left: 90%;
  animation-duration: 7s;
  animation-delay: 3s;
}

@keyframes rise {
  0% {
    bottom: -100px;
    transform: translateX(0);
    opacity: 0;
  }
  50% {
    opacity: 0.6;
  }
  100% {
    bottom: 120%;
    transform: translateX(-100px);
    opacity: 0;
  }
}

.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  z-index: 1;
}

.brand-section {
  text-align: center;
  color: white;
}

.logo-wrapper {
  margin-bottom: 30px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  margin-left: auto;
  margin-right: auto;
}

.logo-ai-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 2px solid rgba(103, 194, 58, 0.3);
  border-radius: 50%;
  animation: ringPulse 2s ease-in-out infinite;
}

.logo-ai-ring-inner {
  position: absolute;
  top: 10%;
  left: 10%;
  width: 80%;
  height: 80%;
  border: 2px solid rgba(103, 194, 58, 0.5);
  border-radius: 50%;
  animation: ringPulse 2s ease-in-out infinite 0.5s;
}

@keyframes ringPulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
}

.main-logo {
  animation: float 3s ease-in-out infinite;
  z-index: 1;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.brand-title {
  font-size: 56px;
  font-weight: 800;
  margin: 0 0 10px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 4px;
  background: linear-gradient(135deg, #ffffff 0%, #67c23a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-subtitle {
  font-size: 16px;
  margin: 0 0 20px 0;
  opacity: 0.9;
  font-weight: 400;
  letter-spacing: 2px;
  color: rgba(255, 255, 255, 0.9);
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: rgba(103, 194, 58, 0.2);
  border: 1px solid rgba(103, 194, 58, 0.5);
  border-radius: 20px;
  margin-bottom: 40px;
  font-size: 13px;
  color: #67c23a;
  backdrop-filter: blur(10px);
  animation: badgeGlow 2s ease-in-out infinite;
}

@keyframes badgeGlow {
  0%,
  100% {
    box-shadow: 0 0 5px rgba(103, 194, 58, 0.3);
  }
  50% {
    box-shadow: 0 0 20px rgba(103, 194, 58, 0.6);
  }
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
  max-width: 420px;
  margin: 0 auto;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  width: 100%;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(10px);
  border-color: rgba(103, 194, 58, 0.3);
}

.feature-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: rgba(103, 194, 58, 0.15);
  border-radius: 10px;
  flex-shrink: 0;
}

.feature-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.feature-title {
  font-size: 15px;
  font-weight: 600;
  color: #ffffff;
}

.feature-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

/* AI神经网络背景 */
.ai-network {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.neural-node {
  position: absolute;
  width: 6px;
  height: 6px;
  background: rgba(103, 194, 58, 0.6);
  border-radius: 50%;
  animation: nodePulse 3s ease-in-out infinite;
}

.neural-node:nth-child(1) {
  top: 10%;
  left: 15%;
  animation-delay: 0s;
}
.neural-node:nth-child(2) {
  top: 20%;
  left: 35%;
  animation-delay: 0.5s;
}
.neural-node:nth-child(3) {
  top: 15%;
  left: 55%;
  animation-delay: 1s;
}
.neural-node:nth-child(4) {
  top: 30%;
  left: 25%;
  animation-delay: 1.5s;
}
.neural-node:nth-child(5) {
  top: 25%;
  left: 75%;
  animation-delay: 0.3s;
}
.neural-node:nth-child(6) {
  top: 40%;
  left: 10%;
  animation-delay: 0.8s;
}
.neural-node:nth-child(7) {
  top: 45%;
  left: 45%;
  animation-delay: 1.2s;
}
.neural-node:nth-child(8) {
  top: 35%;
  left: 65%;
  animation-delay: 1.7s;
}
.neural-node:nth-child(9) {
  top: 55%;
  left: 20%;
  animation-delay: 0.2s;
}
.neural-node:nth-child(10) {
  top: 60%;
  left: 50%;
  animation-delay: 0.7s;
}
.neural-node:nth-child(11) {
  top: 50%;
  left: 80%;
  animation-delay: 1.1s;
}
.neural-node:nth-child(12) {
  top: 70%;
  left: 30%;
  animation-delay: 1.6s;
}
.neural-node:nth-child(13) {
  top: 75%;
  left: 60%;
  animation-delay: 0.4s;
}
.neural-node:nth-child(14) {
  top: 65%;
  left: 85%;
  animation-delay: 0.9s;
}
.neural-node:nth-child(15) {
  top: 85%;
  left: 15%;
  animation-delay: 1.3s;
}
.neural-node:nth-child(16) {
  top: 80%;
  left: 40%;
  animation-delay: 1.8s;
}
.neural-node:nth-child(17) {
  top: 90%;
  left: 70%;
  animation-delay: 0.1s;
}
.neural-node:nth-child(18) {
  top: 5%;
  left: 80%;
  animation-delay: 0.6s;
}
.neural-node:nth-child(19) {
  top: 8%;
  left: 45%;
  animation-delay: 1.4s;
}
.neural-node:nth-child(20) {
  top: 95%;
  left: 55%;
  animation-delay: 1.9s;
}

@keyframes nodePulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.4;
  }
  50% {
    transform: scale(1.5);
    opacity: 1;
  }
}

.neural-connections {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.login-right {
  flex: 0 0 450px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  z-index: 1;
}

.login-card {
  width: 100%;
  border-radius: 16px;
  overflow: hidden;
}

.login-header {
  text-align: center;
  padding: 10px 0;
}

.avatar-wrapper {
  margin-bottom: 15px;
}

.login-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.login-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.5);
}

.login-button:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  margin-top: 10px;
}

.login-footer p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 900px) {
  .login-left {
    display: none;
  }

  .login-right {
    flex: 1;
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .login-card {
    border-radius: 12px;
  }

  .brand-title {
    font-size: 36px;
  }

  .login-header h2 {
    font-size: 24px;
  }
}
</style>
