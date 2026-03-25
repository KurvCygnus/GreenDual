<template>
  <div class="home-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <el-icon size="28" color="#67C23A"><Monitor /></el-icon>
          <span>环境监测系统</span>
        </div>

        <el-menu
          :default-active="activeIndex"
          class="el-menu-vertical"
          background-color="#1a5f1a"
          text-color="#fff"
          active-text-color="#67C23A"
          @select="handleSelect"
        >
          <el-menu-item index="1">
            <el-icon><Odometer /></el-icon>
            <span>环境监测</span>
          </el-menu-item>

          <el-menu-item index="2">
            <el-icon><Document /></el-icon>
            <span>监测记录</span>
          </el-menu-item>

          <el-menu-item index="3">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>

          <el-menu-item index="4">
            <el-icon><Cpu /></el-icon>
            <span>AI监测</span>
          </el-menu-item>

          <el-menu-item index="5">
            <el-icon><Cpu /></el-icon>
            <span>设备管理</span>
          </el-menu-item>

          <el-menu-item index="7">
            <el-icon><Sunny /></el-icon>
            <span>路灯管理</span>
          </el-menu-item>

          <el-menu-item index="6">
            <el-icon><Cloudy /></el-icon>
            <span>云平台</span>
          </el-menu-item>
        </el-menu>

        <div class="user-info">
          <el-avatar :size="40" :icon="UserFilled" />
          <div class="user-detail">
            <span class="username">{{ username }}</span>
            <el-tag size="small" type="success">{{ roleText }}</el-tag>
          </div>
          <el-button type="danger" size="small" @click="logout" circle>
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-header class="main-header">
          <div class="header-title">
            <h2>{{ currentTitle }}</h2>
          </div>

          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索设备..."
              :prefix-icon="Search"
              clearable
              style="width: 250px; margin-right: 10px"
            />
            <el-button type="success" :icon="Plus" @click="handleAdd"
              >添加设备</el-button
            >
            <el-button type="primary" :icon="Refresh" @click="refreshData"
              >刷新</el-button
            >
          </div>
        </el-header>

        <el-main class="main-content">
          <keep-alive>
            <div v-if="activeIndex === '1'" key="monitor" class="monitor-panel">
              <!-- 统计卡片 -->
              <el-row :gutter="20" class="stats-row">
                <el-col :span="6">
                  <el-card class="stat-card" shadow="hover">
                    <div class="stat-icon" style="background: #67c23a">
                      <el-icon size="32" color="#fff"><Odometer /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ deviceCount }}</div>
                      <div class="stat-label">监测设备</div>
                    </div>
                  </el-card>
                </el-col>

                <el-col :span="6">
                  <el-card class="stat-card" shadow="hover">
                    <div class="stat-icon" style="background: #409eff">
                      <el-icon size="32" color="#fff"><MostlyCloudy /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ avgTemperature }}°C</div>
                      <div class="stat-label">平均温度</div>
                    </div>
                  </el-card>
                </el-col>

                <el-col :span="6">
                  <el-card class="stat-card" shadow="hover">
                    <div class="stat-icon" style="background: #e6a23c">
                      <el-icon size="32" color="#fff"><Watermelon /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ avgHumidity }}%</div>
                      <div class="stat-label">平均湿度</div>
                    </div>
                  </el-card>
                </el-col>

                <el-col :span="6">
                  <el-card class="stat-card" shadow="hover">
                    <div class="stat-icon" style="background: #f56c6c">
                      <el-icon size="32" color="#fff"><Sunny /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ avgLight }}lux</div>
                      <div class="stat-label">平均光照</div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>

              <!-- 数据可视化 -->
              <EnvironmentChart />

              <!-- 设备列表 -->
              <el-card class="device-list-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <span>设备列表</span>
                    <el-radio-group v-model="viewMode" size="small">
                      <el-radio-button label="card">卡片视图</el-radio-button>
                      <el-radio-button label="table">表格视图</el-radio-button>
                    </el-radio-group>
                  </div>
                </template>

                <!-- 卡片视图 -->
                <el-row v-if="viewMode === 'card'" :gutter="20">
                  <el-col
                    v-for="device in deviceList"
                    :key="device.id"
                    :xs="24"
                    :sm="12"
                    :md="8"
                    :lg="6"
                    style="margin-bottom: 20px"
                  >
                    <el-card
                      class="device-card"
                      :class="{ offline: !device.onLine }"
                    >
                      <div class="device-header">
                        <el-icon
                          size="24"
                          :color="device.onLine ? '#67C23A' : '#909399'"
                          ><Cpu
                        /></el-icon>
                        <span class="device-name">{{ device.name }}</span>
                        <el-tag
                          :type="device.onLine ? 'success' : 'info'"
                          size="small"
                        >
                          {{ device.onLine ? "在线" : "离线" }}
                        </el-tag>
                      </div>

                      <div class="device-data">
                        <div class="data-item">
                          <el-icon><MostlyCloudy /></el-icon>
                          <span>温度: {{ device.temperature }}°C</span>
                        </div>
                        <div class="data-item">
                          <el-icon><Watermelon /></el-icon>
                          <span>湿度: {{ device.humidity }}%</span>
                        </div>
                        <div class="data-item">
                          <el-icon><Sunny /></el-icon>
                          <span>光照: {{ device.lightIntensity }}lux</span>
                        </div>
                      </div>

                      <div class="device-actions">
                        <el-button
                          type="primary"
                          size="small"
                          @click="viewDetail(device)"
                          >详情</el-button
                        >
                        <el-button
                          type="warning"
                          size="small"
                          @click="editDevice(device)"
                          >编辑</el-button
                        >
                        <el-button
                          type="danger"
                          size="small"
                          @click="deleteDevice(device)"
                          >删除</el-button
                        >
                      </div>
                    </el-card>
                  </el-col>
                </el-row>

                <!-- 表格视图 -->
                <el-table v-else :data="deviceList" style="width: 100%">
                  <el-table-column prop="id" label="设备ID" width="120" />
                  <el-table-column prop="name" label="设备名称" />
                  <el-table-column prop="temperature" label="温度(°C)" />
                  <el-table-column prop="humidity" label="湿度(%)" />
                  <el-table-column prop="lightIntensity" label="光照(lux)" />
                  <el-table-column prop="onLine" label="状态">
                    <template #default="scope">
                      <el-tag :type="scope.row.onLine ? 'success' : 'info'">
                        {{ scope.row.onLine ? "在线" : "离线" }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="200">
                    <template #default="scope">
                      <el-button
                        type="primary"
                        size="small"
                        @click="viewDetail(scope.row)"
                        >详情</el-button
                      >
                      <el-button
                        type="warning"
                        size="small"
                        @click="editDevice(scope.row)"
                        >编辑</el-button
                      >
                      <el-button
                        type="danger"
                        size="small"
                        @click="deleteDevice(scope.row)"
                        >删除</el-button
                      >
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </div>

            <!-- 其他模块 -->
            <div
              v-else-if="activeIndex === '2'"
              key="record"
              class="record-panel"
            >
              <MonitorRecord />
            </div>
            <div v-else-if="activeIndex === '3'" key="user" class="user-panel">
              <UserManagement />
            </div>
            <div
              v-else-if="activeIndex === '4'"
              key="ai"
              class="ai-monitor-panel"
            >
              <AIPrediction />
            </div>

            <div
              v-else-if="activeIndex === '5'"
              key="device"
              class="device-management-panel"
            >
              <DeviceTypeManager />
            </div>

            <div
              v-else-if="activeIndex === '6'"
              key="cloud"
              class="cloud-platform-panel"
            >
              <CloudPlatform />
            </div>

            <div
              v-else-if="activeIndex === '7'"
              key="lamp"
              class="lamp-management-panel"
            >
              <LampManager />
            </div>
          </keep-alive>
        </el-main>
      </el-container>
    </el-container>

    <!-- 添加设备对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加设备"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="100px"
      >
        <el-form-item label="设备ID" prop="id">
          <el-input
            v-model="addForm.id"
            placeholder="请输入设备ID，如：ENV001"
          />
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="addForm.deviceType"
            placeholder="请选择设备类型"
            style="width: 100%"
          >
            <el-option label="环境监测" value="环境监测" />
            <el-option label="路灯" value="路灯" />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址" prop="ip">
          <el-input
            v-model="addForm.ip"
            placeholder="请输入IP地址，如：192.168.1.100"
          />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="addForm.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="温度(°C)" prop="temperature">
          <el-input-number
            v-model="addForm.temperature"
            :min="-50"
            :max="100"
            :precision="1"
          />
        </el-form-item>
        <el-form-item label="湿度(%)" prop="humidity">
          <el-input-number v-model="addForm.humidity" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="光照(lux)" prop="lightIntensity">
          <el-input-number
            v-model="addForm.lightIntensity"
            :min="0"
            :max="100000"
          />
        </el-form-item>
        <el-form-item label="在线状态" prop="onLine">
          <el-switch
            v-model="addForm.onLine"
            active-text="在线"
            inactive-text="离线"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddDevice" :loading="addLoading"
          >确定</el-button
        >
      </template>
    </el-dialog>

    <!-- 查看设备详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="设备详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备ID">{{
          detailDevice.id
        }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{
          detailDevice.name
        }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{
          detailDevice.deviceType
        }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{
          detailDevice.ip
        }}</el-descriptions-item>
        <el-descriptions-item label="端口">{{
          detailDevice.port
        }}</el-descriptions-item>
        <el-descriptions-item label="在线状态">
          <el-tag :type="detailDevice.onLine ? 'success' : 'info'">
            {{ detailDevice.onLine ? "在线" : "离线" }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="温度"
          >{{ detailDevice.temperature }}°C</el-descriptions-item
        >
        <el-descriptions-item label="湿度"
          >{{ detailDevice.humidity }}%</el-descriptions-item
        >
        <el-descriptions-item label="光照"
          >{{ detailDevice.lightIntensity }}lux</el-descriptions-item
        >
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailDialogVisible = false"
          >关闭</el-button
        >
      </template>
    </el-dialog>

    <!-- 编辑设备对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑设备"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="addFormRules"
        label-width="100px"
      >
        <el-form-item label="设备ID">
          <el-input v-model="editForm.id" disabled />
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="editForm.deviceType"
            placeholder="请选择设备类型"
            style="width: 100%"
          >
            <el-option label="环境监测" value="环境监测" />
            <el-option label="路灯" value="路灯" />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址" prop="ip">
          <el-input v-model="editForm.ip" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="editForm.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="温度(°C)" prop="temperature">
          <el-input-number
            v-model="editForm.temperature"
            :min="-50"
            :max="100"
            :precision="1"
          />
        </el-form-item>
        <el-form-item label="湿度(%)" prop="humidity">
          <el-input-number v-model="editForm.humidity" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="光照(lux)" prop="lightIntensity">
          <el-input-number
            v-model="editForm.lightIntensity"
            :min="0"
            :max="100000"
          />
        </el-form-item>
        <el-form-item label="在线状态" prop="onLine">
          <el-switch
            v-model="editForm.onLine"
            active-text="在线"
            inactive-text="离线"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitEditDevice"
          :loading="editLoading"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  UserFilled,
  SwitchButton,
  Search,
  Plus,
  Refresh,
  Odometer,
  MostlyCloudy,
  Watermelon,
  Sunny,
  Cloudy,
  Document,
  User,
} from "@element-plus/icons-vue";

import EnvironmentChart from "../components/charts/EnvironmentChart.vue";
import AIPrediction from "../components/ai/AIPrediction.vue";
import DeviceTypeManager from "../components/devices/DeviceTypeManager.vue";
import LampManager from "../components/lamp/LampManager.vue";
import CloudPlatform from "../components/cloud/CloudPlatform.vue";
import MonitorRecord from "../components/records/MonitorRecord.vue";
import UserManagement from "../components/user/UserManagement.vue";
import {
  getDeviceStats,
  getDeviceList,
  deleteDevice as deleteDeviceApi,
  addDevice as addDeviceApi,
  updateDevice as updateDeviceApi,
} from "../api/auth.js";

const router = useRouter();

// 状态
const activeIndex = ref("1");
const searchKeyword = ref("");
const viewMode = ref("card");
const username = ref(localStorage.getItem("name") || "管理员");
const roleId = ref(parseInt(localStorage.getItem("roleId")) || 1);

// 计算属性
const roleText = computed(() => {
  return roleId.value === 1 ? "管理员" : "操作员";
});

const currentTitle = computed(() => {
  const titles = {
    1: "环境监测",
    2: "监测记录",
    3: "用户管理",
    4: "AI监测",
    5: "设备管理",
    6: "云平台",
    7: "路灯管理",
  };
  return titles[activeIndex.value] || "环境监测";
});

// 统计数据
const deviceCount = ref(0);
const avgTemperature = ref(0);
const avgHumidity = ref(0);
const avgLight = ref(0);

// 设备列表
const deviceList = ref([]);

// 添加设备对话框
const addDialogVisible = ref(false);
const addLoading = ref(false);
const addFormRef = ref(null);
const addForm = ref({
  id: "",
  name: "",
  deviceType: "环境监测",
  ip: "192.168.1.100",
  port: 8080,
  temperature: 25.0,
  humidity: 50,
  lightIntensity: 500,
  onLine: true,
});

const addFormRules = {
  id: [
    { required: true, message: "请输入设备ID", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 20 个字符", trigger: "blur" },
  ],
  name: [
    { required: true, message: "请输入设备名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
  ],
  deviceType: [
    { required: true, message: "请选择设备类型", trigger: "change" },
  ],
  ip: [
    { required: true, message: "请输入IP地址", trigger: "blur" },
    {
      pattern: /^(\d{1,3}\.){3}\d{1,3}$/,
      message: "IP地址格式不正确",
      trigger: "blur",
    },
  ],
  port: [{ required: true, message: "请输入端口", trigger: "blur" }],
};

// 查看设备详情
const detailDialogVisible = ref(false);
const detailDevice = ref({});

// 编辑设备
const editDialogVisible = ref(false);
const editLoading = ref(false);
const editFormRef = ref(null);
const editForm = ref({
  id: "",
  name: "",
  deviceType: "",
  ip: "",
  port: 8080,
  temperature: 25.0,
  humidity: 50,
  lightIntensity: 500,
  onLine: true,
});

// 方法
const handleSelect = (index) => {
  activeIndex.value = index;
};

const logout = () => {
  ElMessageBox.confirm("确定要退出登录吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    localStorage.clear();
    ElMessage.success("退出成功");
    router.push("/login");
  });
};

const handleAdd = () => {
  addForm.value = {
    id: "",
    name: "",
    deviceType: "环境监测",
    ip: "192.168.1.100",
    port: 8080,
    temperature: 25.0,
    humidity: 50,
    lightIntensity: 500,
    onLine: true,
  };
  addDialogVisible.value = true;
};

const submitAddDevice = async () => {
  if (!addFormRef.value) return;

  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      addLoading.value = true;
      try {
        // 转换数据格式以适配后端
        const deviceData = {
          ...addForm.value,
          onLine: addForm.value.onLine ? 1 : 0,
        };
        const response = await addDeviceApi(deviceData);
        if (response.data.code === 1) {
          ElMessage.success("设备添加成功");
          addDialogVisible.value = false;
          await refreshData();
        } else {
          ElMessage.error(response.data.msg || "添加失败");
        }
      } catch (error) {
        console.error("添加设备失败", error);
        ElMessage.error("添加设备失败");
      } finally {
        addLoading.value = false;
      }
    }
  });
};

const refreshData = async () => {
  try {
    const statsResponse = await getDeviceStats();
    if (statsResponse.data.code === 1) {
      const stats = statsResponse.data.data;
      deviceCount.value = stats.deviceCount;
      avgTemperature.value = stats.avgTemperature;
      avgHumidity.value = stats.avgHumidity;
      avgLight.value = stats.avgLight;

      const listResponse = await getDeviceList({
        match: searchKeyword.value,
        offset: 0,
        pageSize: 100,
        sort: "asc",
      });
      if (listResponse.data.code === 1) {
        deviceList.value = listResponse.data.data;
      }
    }
    ElMessage.success("数据已刷新");
  } catch (error) {
    ElMessage.error("数据刷新失败");
    console.error(error);
  }
};

const viewDetail = (device) => {
  detailDevice.value = { ...device };
  detailDialogVisible.value = true;
};

const editDevice = (device) => {
  editForm.value = {
    id: device.id,
    name: device.name,
    deviceType: device.deviceType || "环境监测",
    ip: device.ip || "192.168.1.100",
    port: device.port || 8080,
    temperature: device.temperature || 25.0,
    humidity: device.humidity || 50,
    lightIntensity: device.lightIntensity || 500,
    onLine: device.onLine === 1 || device.onLine === true,
  };
  editDialogVisible.value = true;
};

const submitEditDevice = async () => {
  if (!editFormRef.value) return;

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true;
      try {
        const deviceData = {
          ...editForm.value,
          onLine: editForm.value.onLine ? 1 : 0,
        };
        const response = await updateDeviceApi(deviceData);
        if (response.data.code === 1) {
          ElMessage.success("设备更新成功");
          editDialogVisible.value = false;
          await refreshData();
        } else {
          ElMessage.error(response.data.msg || "更新失败");
        }
      } catch (error) {
        console.error("更新设备失败", error);
        ElMessage.error("更新设备失败");
      } finally {
        editLoading.value = false;
      }
    }
  });
};

const deleteDevice = async (device) => {
  ElMessageBox.confirm(`确定要删除设备 ${device.name} 吗？`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      const response = await deleteDeviceApi([device.id]);
      if (response.data.code === 1) {
        ElMessage.success("删除成功");
        await refreshData();
      } else {
        ElMessage.error(response.data.msg || "删除失败");
      }
    } catch (error) {
      ElMessage.error("删除失败");
      console.error(error);
    }
  });
};

onMounted(async () => {
  await refreshData();
});
</script>

<style scoped>
.home-container {
  min-height: 100vh;
}

.sidebar {
  background: #1a5f1a;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.el-menu-vertical {
  flex: 1;
  border-right: none;
}

.user-info {
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
}

.user-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  color: #fff;
  font-size: 14px;
}

.main-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-title h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.device-list-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.device-card {
  transition: all 0.3s;
}

.device-card:hover {
  transform: translateY(-5px);
}

.device-card.offline {
  opacity: 0.7;
}

.device-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.device-name {
  flex: 1;
  font-weight: 600;
  color: #2c3e50;
}

.device-data {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.data-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.device-actions {
  display: flex;
  gap: 8px;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}
/* 移动端适配 */
@media (max-width: 768px) {
  .sidebar {
    width: 200px !important;
  }

  .logo {
    font-size: 16px;
  }

  .main-header {
    flex-direction: column;
    align-items: flex-start;
    padding: 10px;
    gap: 10px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .header-actions .el-input {
    flex: 1;
    margin-right: 10px !important;
  }

  .main-content {
    padding: 10px;
  }

  .stats-row {
    margin-bottom: 10px;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 15px;
  }

  .stat-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }

  .device-card {
    margin-bottom: 10px;
  }

  .device-actions {
    flex-wrap: wrap;
  }

  .device-actions .el-button {
    flex: 1;
    margin-bottom: 5px;
  }

  .chart-container {
    height: 300px !important;
  }

  .ai-monitor-panel {
    padding: 10px;
  }

  .prediction-params {
    padding: 10px;
  }

  .prediction-chart {
    height: 300px !important;
  }

  .el-form-item {
    margin-right: 0 !important;
    margin-bottom: 10px !important;
  }

  .el-select {
    width: 100% !important;
  }
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .sidebar {
    width: 180px !important;
  }

  .logo span {
    display: none;
  }

  .el-menu-item {
    font-size: 12px;
  }

  .el-menu-item .el-icon {
    margin-right: 5px;
  }

  .stat-value {
    font-size: 20px;
  }

  .device-name {
    font-size: 14px;
  }

  .data-item {
    font-size: 12px;
  }

  .prediction-stats .el-col {
    margin-bottom: 10px;
  }
}
</style>
