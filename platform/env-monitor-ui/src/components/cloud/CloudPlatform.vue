<template>
  <div class="cloud-platform">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon size="20" color="#67C23A"><Cloudy /></el-icon>
          <span>云平台对接</span>
          <el-button
            type="primary"
            size="small"
            @click="connectCloud"
            :loading="connecting"
          >
            <el-icon><Connection /></el-icon>
            {{ isConnected ? "断开连接" : "连接云平台" }}
          </el-button>
        </div>
      </template>

      <div class="cloud-content">
        <div class="cloud-config">
          <el-form
            :model="cloudForm"
            :rules="rules"
            ref="cloudFormRef"
            label-width="120px"
          >
            <el-form-item label="云平台类型" prop="platform">
              <el-select v-model="cloudForm.platform" size="large">
                <el-option label="阿里云IoT" value="aliyun" />
                <el-option label="华为云IoT" value="huaweicloud" />
                <el-option label="腾讯云IoT" value="tencentcloud" />
                <el-option label="百度云IoT" value="baiduyun" />
              </el-select>
            </el-form-item>

            <el-form-item label="产品名称" prop="productName">
              <el-input
                v-model="cloudForm.productName"
                size="large"
                placeholder="请输入产品名称"
              />
            </el-form-item>

            <el-form-item label="设备名称" prop="deviceName">
              <el-input
                v-model="cloudForm.deviceName"
                size="large"
                placeholder="请输入设备名称"
              />
            </el-form-item>

            <el-form-item label="Access Key" prop="accessKey">
              <el-input
                v-model="cloudForm.accessKey"
                size="large"
                placeholder="请输入Access Key"
              />
            </el-form-item>

            <el-form-item label="Secret Key" prop="secretKey">
              <el-input
                v-model="cloudForm.secretKey"
                type="password"
                size="large"
                placeholder="请输入Secret Key"
                show-password
              />
            </el-form-item>

            <el-form-item label="区域" prop="region">
              <el-input
                v-model="cloudForm.region"
                size="large"
                placeholder="请输入区域，如：cn-shanghai"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" size="large" @click="saveConfig"
                >保存配置</el-button
              >
              <el-button size="large" @click="resetConfig">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="cloud-status" v-if="isConnected">
          <el-card class="status-card" shadow="hover">
            <template #header>
              <div class="status-header">
                <el-icon size="18" color="#67C23A"><Check /></el-icon>
                <span>云平台连接状态</span>
              </div>
            </template>

            <div class="status-info">
              <div class="info-item">
                <span class="label">连接状态:</span>
                <el-tag type="success">已连接</el-tag>
              </div>
              <div class="info-item">
                <span class="label">云平台:</span>
                <span class="value">{{
                  getPlatformName(cloudForm.platform)
                }}</span>
              </div>
              <div class="info-item">
                <span class="label">连接时间:</span>
                <span class="value">{{ connectTime }}</span>
              </div>
              <div class="info-item">
                <span class="label">数据同步:</span>
                <el-switch v-model="dataSync" @change="toggleSync" />
              </div>
              <div class="info-item">
                <span class="label">同步状态:</span>
                <span class="value" :class="{ syncing: dataSync }">
                  {{ dataSync ? "正在同步" : "已暂停" }}
                </span>
              </div>
              <div class="info-item">
                <span class="label">同步频率:</span>
                <el-select
                  v-model="syncInterval"
                  size="small"
                  @change="updateSyncInterval"
                >
                  <el-option label="1分钟" value="60000" />
                  <el-option label="5分钟" value="300000" />
                  <el-option label="15分钟" value="900000" />
                  <el-option label="30分钟" value="1800000" />
                  <el-option label="1小时" value="3600000" />
                </el-select>
              </div>
            </div>

            <div class="sync-stats">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-value">{{ syncCount }}</div>
                    <div class="stat-label">同步次数</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-value">{{ syncSuccessCount }}</div>
                    <div class="stat-label">成功次数</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-value">{{ syncFailedCount }}</div>
                    <div class="stat-label">失败次数</div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </div>

        <div class="cloud-features">
          <el-card class="features-card" shadow="hover">
            <template #header>
              <div class="features-header">
                <el-icon size="18" color="#67C23A"><Star /></el-icon>
                <span>云平台功能</span>
              </div>
            </template>

            <div class="features-grid">
              <el-col :span="8" class="feature-item">
                <div class="feature-icon" style="background: #67c23a">
                  <el-icon size="24" color="#fff"><Cpu /></el-icon>
                </div>
                <h4>远程监控</h4>
                <p>随时随地查看设备状态和环境数据</p>
              </el-col>
              <el-col :span="8" class="feature-item">
                <div class="feature-icon" style="background: #409eff">
                  <el-icon size="24" color="#fff"><Cloudy /></el-icon>
                </div>
                <h4>数据存储</h4>
                <p>将环境数据存储到云端，永久保存</p>
              </el-col>
              <el-col :span="8" class="feature-item">
                <div class="feature-icon" style="background: #e6a23c">
                  <el-icon size="24" color="#fff"><Bell /></el-icon>
                </div>
                <h4>智能告警</h4>
                <p>设置阈值，异常时自动发送告警</p>
              </el-col>
              <el-col :span="8" class="feature-item">
                <div class="feature-icon" style="background: #f56c6c">
                  <el-icon size="24" color="#fff"><Cpu /></el-icon>
                </div>
                <h4>AI分析</h4>
                <p>利用云端AI能力进行数据分析</p>
              </el-col>
              <el-col :span="8" class="feature-item">
                <div class="feature-icon" style="background: #909399">
                  <el-icon size="24" color="#fff"><Cpu /></el-icon>
                </div>
                <h4>移动端应用</h4>
                <p>通过手机App远程管理设备</p>
              </el-col>
              <el-col :span="8" class="feature-item">
                <div class="feature-icon" style="background: #e0a800">
                  <el-icon size="24" color="#fff"><Cpu /></el-icon>
                </div>
                <h4>设备管理</h4>
                <p>批量管理设备，远程配置参数</p>
              </el-col>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Cloudy,
  Connection,
  Check,
  Star,
  Bell,
  Cpu,
} from "@element-plus/icons-vue";

const cloudFormRef = ref(null);
const connecting = ref(false);
const isConnected = ref(false);
const dataSync = ref(false);
const syncInterval = ref(300000); // 默认5分钟
const connectTime = ref("");
const syncCount = ref(0);
const syncSuccessCount = ref(0);
const syncFailedCount = ref(0);

// 云平台配置表单
const cloudForm = reactive({
  platform: "aliyun",
  productName: "",
  deviceName: "",
  accessKey: "",
  secretKey: "",
  region: "cn-shanghai",
});

// 表单验证规则
const rules = {
  platform: [
    { required: true, message: "请选择云平台类型", trigger: "change" },
  ],
  productName: [{ required: true, message: "请输入产品名称", trigger: "blur" }],
  deviceName: [{ required: true, message: "请输入设备名称", trigger: "blur" }],
  accessKey: [{ required: true, message: "请输入Access Key", trigger: "blur" }],
  secretKey: [{ required: true, message: "请输入Secret Key", trigger: "blur" }],
  region: [{ required: true, message: "请输入区域", trigger: "blur" }],
};

// 方法
const getPlatformName = (platform) => {
  const names = {
    aliyun: "阿里云IoT",
    huaweicloud: "华为云IoT",
    tencentcloud: "腾讯云IoT",
    baiduyun: "百度云IoT",
  };
  return names[platform] || platform;
};

const connectCloud = async () => {
  if (isConnected.value) {
    // 断开连接
    ElMessageBox.confirm("确定要断开云平台连接吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }).then(() => {
      isConnected.value = false;
      dataSync.value = false;
      connectTime.value = "";
      ElMessage.success("已断开云平台连接");
    });
    return;
  }

  // 连接云平台
  if (!cloudFormRef.value) return;

  await cloudFormRef.value.validate(async (valid) => {
    if (valid) {
      connecting.value = true;
      try {
        // 模拟连接过程
        await new Promise((resolve) => setTimeout(resolve, 2000));

        isConnected.value = true;
        dataSync.value = true;
        connectTime.value = new Date().toLocaleString();

        ElMessage.success("云平台连接成功");

        // 模拟数据同步
        startSync();
      } catch (error) {
        ElMessage.error("云平台连接失败");
      } finally {
        connecting.value = false;
      }
    }
  });
};

const saveConfig = () => {
  if (!cloudFormRef.value) return;

  cloudFormRef.value.validate((valid) => {
    if (valid) {
      // 保存配置到本地存储
      localStorage.setItem("cloudConfig", JSON.stringify(cloudForm));
      ElMessage.success("配置保存成功");
    }
  });
};

const resetConfig = () => {
  cloudForm.platform = "aliyun";
  cloudForm.productName = "";
  cloudForm.deviceName = "";
  cloudForm.accessKey = "";
  cloudForm.secretKey = "";
  cloudForm.region = "cn-shanghai";
  ElMessage.info("配置已重置");
};

const toggleSync = (value) => {
  if (value) {
    startSync();
  } else {
    stopSync();
  }
};

const updateSyncInterval = () => {
  if (dataSync.value) {
    stopSync();
    startSync();
  }
};

let syncTimer = null;

const startSync = () => {
  if (syncTimer) {
    clearInterval(syncTimer);
  }

  syncTimer = setInterval(() => {
    syncData();
  }, syncInterval.value);
};

const stopSync = () => {
  if (syncTimer) {
    clearInterval(syncTimer);
    syncTimer = null;
  }
};

const syncData = async () => {
  try {
    // 模拟数据同步
    await new Promise((resolve) => setTimeout(resolve, 1000));

    syncCount.value++;

    // 模拟成功率90%
    if (Math.random() > 0.1) {
      syncSuccessCount.value++;
    } else {
      syncFailedCount.value++;
    }
  } catch (error) {
    syncCount.value++;
    syncFailedCount.value++;
  }
};

// 初始化
const initConfig = () => {
  const savedConfig = localStorage.getItem("cloudConfig");
  if (savedConfig) {
    try {
      const config = JSON.parse(savedConfig);
      Object.assign(cloudForm, config);
    } catch (error) {
      console.error("解析配置失败:", error);
    }
  }
};

initConfig();
</script>

<style scoped>
.cloud-platform {
  margin: 20px 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cloud-content {
  margin-top: 20px;
}

.cloud-config {
  margin-bottom: 20px;
}

.cloud-status {
  margin-bottom: 20px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-info {
  margin: 20px 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 10px;
}

.info-item .label {
  width: 100px;
  color: #909399;
}

.info-item .value {
  flex: 1;
  color: #606266;
}

.info-item .value.syncing {
  color: #67c23a;
  font-weight: 500;
}

.sync-stats {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.features-card {
  margin-top: 20px;
}

.features-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.features-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 20px;
}

.feature-item {
  flex: 1;
  min-width: 200px;
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
}

.feature-item h4 {
  margin: 0 0 10px;
  color: #2c3e50;
  font-size: 16px;
}

.feature-item p {
  margin: 0;
  color: #909399;
  font-size: 14px;
  line-height: 1.4;
}

@media (max-width: 768px) {
  .cloud-config .el-form-item {
    margin-bottom: 15px;
  }

  .features-grid {
    flex-direction: column;
  }

  .feature-item {
    min-width: auto;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .info-item .label {
    width: auto;
  }
}
</style>
