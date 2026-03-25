<template>
  <div class="home-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="240px" class="sidebar">
        <div class="logo">
          <el-icon size="32" color="#409EFF"><Monitor /></el-icon>
          <span>智绿云控平台</span>
        </div>

        <el-menu
          :default-active="activeIndex"
          class="el-menu-vertical"
          background-color="#0f172a"
          text-color="#94a3b8"
          active-text-color="#38bdf8"
          @select="handleSelect"
          :collapse-transition="false"
        >
          <el-menu-item index="1">
            <el-icon class="menu-icon"><Odometer /></el-icon>
            <span>环境监测</span>
          </el-menu-item>

          <el-menu-item index="2">
            <el-icon class="menu-icon"><Document /></el-icon>
            <span>监测记录</span>
          </el-menu-item>

          <el-menu-item index="3">
            <el-icon class="menu-icon"><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>

          <el-menu-item index="4">
            <el-icon class="menu-icon"><Cpu /></el-icon>
            <span>AI监测</span>
          </el-menu-item>

          <el-menu-item index="5">
            <el-icon class="menu-icon"><Cpu /></el-icon>
            <span>设备管理</span>
          </el-menu-item>

          <el-menu-item index="7">
            <el-icon class="menu-icon"><Sunny /></el-icon>
            <span>路灯管理</span>
          </el-menu-item>

          <el-menu-item index="6">
            <el-icon class="menu-icon"><Cloudy /></el-icon>
            <span>云平台</span>
          </el-menu-item>
        </el-menu>

        <div class="user-info">
          <el-avatar :size="48" :icon="UserFilled" class="user-avatar" />
          <div class="user-detail">
            <span class="username">{{ username }}</span>
            <el-tag size="small" type="info" effect="light" class="role-tag">{{
              roleText
            }}</el-tag>
          </div>
          <el-button
            type="primary"
            size="small"
            @click="logout"
            circle
            class="logout-btn"
          >
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-header class="main-header">
          <div class="header-title">
            <el-icon :size="24" color="#409EFF" class="title-icon"
              ><Monitor
            /></el-icon>
            <h2>{{ currentTitle }}</h2>
          </div>

          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索设备..."
              :prefix-icon="Search"
              clearable
              style="width: 280px; margin-right: 16px"
              class="search-input"
            />
            <el-button
              type="primary"
              :icon="Plus"
              @click="handleAdd"
              class="add-btn"
              >添加设备</el-button
            >
            <el-button
              type="info"
              :icon="Refresh"
              @click="refreshData"
              class="refresh-btn"
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
              <EnvironmentChart :device-type="selectedDeviceType" />

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
            @change="handleDeviceTypeChange"
          >
            <el-option
              v-for="type in deviceTypeList"
              :key="type.id"
              :label="type.name"
              :value="type.name"
            />
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
            <el-option
              v-for="type in deviceTypeList"
              :key="type.id"
              :label="type.name"
              :value="type.name"
            />
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
  getDeviceTypeList,
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

// 设备类型列表
const deviceTypeList = ref([]);

// 添加设备对话框
const addDialogVisible = ref(false);
const addLoading = ref(false);
const addFormRef = ref(null);
const addForm = ref({
  id: "",
  name: "",
  deviceType: "环境监测站",
  ip: "192.168.1.100",
  port: 8080,
  temperature: 25.0,
  humidity: 50,
  lightIntensity: 500,
  onLine: true,
});

// 当前选中的设备类型，用于图表交互
const selectedDeviceType = ref("all");

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
    deviceType: "环境监测站",
    ip: "192.168.1.100",
    port: 8080,
    temperature: 25.0,
    humidity: 50,
    lightIntensity: 500,
    onLine: true,
  };
  addDialogVisible.value = true;
};

// 设备类型变化时的处理，实现与图表的动态交互
const handleDeviceTypeChange = (type) => {
  selectedDeviceType.value = type;
  // 这里可以根据设备类型更新图表数据
  // 例如：根据设备类型过滤数据，更新图表显示
  console.log("设备类型变更为:", type);
  // 实际项目中，这里可以调用API获取对应类型设备的数据
  refreshData();
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

// 获取设备类型列表
const fetchDeviceTypes = async () => {
  try {
    const response = await getDeviceTypeList();
    if (response.data.code === 1) {
      deviceTypeList.value = response.data.data || [];
    }
  } catch (error) {
    console.error("获取设备类型列表失败", error);
  }
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
  await fetchDeviceTypes();
  await refreshData();
});
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
}

.sidebar {
  background: #0f172a;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.sidebar:hover {
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.15);
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #f8fafc;
  font-size: 20px;
  font-weight: 700;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
}

.el-menu-vertical {
  flex: 1;
  border-right: none;
  overflow-y: auto;
}

.el-menu-item {
  height: 60px;
  line-height: 60px;
  margin: 8px 12px;
  border-radius: 12px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 12px;
}

.el-menu-item:hover {
  background-color: rgba(56, 189, 248, 0.1) !important;
  color: #38bdf8 !important;
}

.el-menu-item.is-active {
  background: linear-gradient(
    135deg,
    rgba(56, 189, 248, 0.2) 0%,
    rgba(64, 158, 255, 0.1) 100%
  );
  box-shadow: 0 4px 12px rgba(56, 189, 248, 0.2);
}

.menu-icon {
  font-size: 20px;
  transition: all 0.3s ease;
}

.el-menu-item.is-active .menu-icon {
  transform: scale(1.1);
}

.user-info {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  margin-top: auto;
}

.user-avatar {
  border: 2px solid #38bdf8;
  box-shadow: 0 0 15px rgba(56, 189, 248, 0.5);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 0 20px rgba(56, 189, 248, 0.7);
}

.user-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  color: #f8fafc;
  font-size: 15px;
  font-weight: 600;
}

.role-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 10px;
}

.logout-btn {
  transition: all 0.3s ease;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 0 15px rgba(56, 189, 248, 0.5);
}

.main-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  height: 70px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  transition: all 0.3s ease;
}

.header-title h2 {
  margin: 0;
  color: #1e293b;
  font-size: 22px;
  font-weight: 700;
  transition: all 0.3s ease;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  border-radius: 20px !important;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.search-input:hover {
  border-color: #38bdf8;
  box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.1);
}

.add-btn,
.refresh-btn {
  border-radius: 20px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(148, 163, 184, 0.3);
}

.main-content {
  background: #f8fafc;
  padding: 30px;
  min-height: calc(100vh - 70px);
}

.monitor-panel {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stats-row {
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border-color: #38bdf8;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-value {
  color: #38bdf8;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.device-list-card {
  margin-top: 30px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid #e2e8f0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.viewMode {
  display: flex;
  gap: 8px;
}

.device-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.device-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
  border-color: #38bdf8;
}

.device-card.offline {
  opacity: 0.7;
  border-color: #e2e8f0;
}

.device-card.offline:hover {
  border-color: #f87171;
  box-shadow: 0 12px 30px rgba(248, 113, 113, 0.1);
}

.device-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.device-name {
  flex: 1;
  font-weight: 600;
  color: #1e293b;
  font-size: 16px;
}

.device-data {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
  padding: 0 20px;
}

.data-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #64748b;
  font-weight: 500;
  font-size: 14px;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.data-item:last-child {
  border-bottom: none;
}

.data-item el-icon {
  color: #38bdf8;
  font-size: 18px;
}

.device-actions {
  display: flex;
  gap: 10px;
  padding: 15px 20px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}

.device-actions .el-button {
  flex: 1;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.device-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .sidebar {
    width: 200px !important;
  }

  .logo {
    font-size: 18px;
    height: 60px;
  }

  .main-header {
    flex-direction: column;
    align-items: flex-start;
    padding: 15px;
    gap: 12px;
    height: auto;
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
    padding: 20px;
  }

  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }

  .stat-icon {
    margin-right: 0;
    margin-bottom: 15px;
  }

  .device-card {
    margin-bottom: 15px;
  }

  .device-actions {
    flex-wrap: wrap;
  }

  .device-actions .el-button {
    flex: 1;
    margin-bottom: 8px;
  }

  .chart-container {
    height: 300px !important;
  }

  .ai-monitor-panel {
    padding: 15px;
  }

  .prediction-params {
    padding: 15px;
  }

  .prediction-chart {
    height: 300px !important;
  }

  .el-form-item {
    margin-right: 0 !important;
    margin-bottom: 15px !important;
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
    height: 50px;
    line-height: 50px;
  }

  .el-menu-item .menu-icon {
    margin-right: 8px;
    font-size: 16px;
  }

  .stat-value {
    font-size: 24px;
  }

  .device-name {
    font-size: 14px;
  }

  .data-item {
    font-size: 12px;
  }

  .prediction-stats .el-col {
    margin-bottom: 15px;
  }
}
</style>
