<template>
  <div class="cloud-platform">
    <!-- 连接状态卡片 -->
    <el-row :gutter="20" class="status-row">
      <el-col :span="6">
        <el-card
          class="stat-card"
          shadow="hover"
          :class="{ connected: isConnected }"
        >
          <div class="stat-content">
            <div
              class="stat-icon"
              :style="{ background: isConnected ? '#67c23a' : '#909399' }"
            >
              <el-icon size="24" color="#fff"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ isConnected ? "已连接" : "未连接" }}
              </div>
              <div class="stat-label">
                {{ getPlatformName(cloudForm.platform) }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon size="24" color="#fff"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ syncCount }}</div>
              <div class="stat-label">同步次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon size="24" color="#fff"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ syncSuccessCount }}</div>
              <div class="stat-label">成功次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon size="24" color="#fff"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ syncFailedCount }}</div>
              <div class="stat-label">失败次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" class="cloud-tabs">
      <!-- 配置管理 -->
      <el-tab-pane label="配置管理" name="config">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>云平台配置</span>
              <div class="header-actions">
                <el-button
                  type="primary"
                  @click="testConnection"
                  :loading="testing"
                >
                  <el-icon><Connection /></el-icon>
                  测试连接
                </el-button>
                <el-button
                  :type="isConnected ? 'danger' : 'success'"
                  @click="connectCloud"
                  :loading="connecting"
                >
                  <el-icon><Link /></el-icon>
                  {{ isConnected ? "断开连接" : "连接云平台" }}
                </el-button>
              </div>
            </div>
          </template>

          <el-form
            :model="cloudForm"
            :rules="rules"
            ref="cloudFormRef"
            label-width="120px"
            class="config-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="云平台类型" prop="platform">
                  <el-select v-model="cloudForm.platform" style="width: 100%">
                    <el-option label="阿里云IoT" value="aliyun">
                      <div class="platform-option">
                        <span>阿里云IoT</span>
                        <el-tag size="small" type="success">推荐</el-tag>
                      </div>
                    </el-option>
                    <el-option label="华为云IoT" value="huaweicloud" />
                    <el-option label="腾讯云IoT" value="tencentcloud" />
                    <el-option label="百度云IoT" value="baiduyun" />
                    <el-option label="OneNET" value="onenet" />
                    <el-option label="自定义MQTT" value="custom" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="区域" prop="region">
                  <el-select v-model="cloudForm.region" style="width: 100%">
                    <el-option label="华东2(上海)" value="cn-shanghai" />
                    <el-option label="华北2(北京)" value="cn-beijing" />
                    <el-option label="华南1(深圳)" value="cn-shenzhen" />
                    <el-option label="华东1(杭州)" value="cn-hangzhou" />
                    <el-option label="香港" value="cn-hongkong" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="产品Key" prop="productKey">
                  <el-input
                    v-model="cloudForm.productKey"
                    placeholder="请输入产品Key"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="设备名称" prop="deviceName">
                  <el-input
                    v-model="cloudForm.deviceName"
                    placeholder="请输入设备名称"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="Access Key" prop="accessKey">
                  <el-input
                    v-model="cloudForm.accessKey"
                    placeholder="请输入Access Key ID"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Secret Key" prop="secretKey">
                  <el-input
                    v-model="cloudForm.secretKey"
                    type="password"
                    placeholder="请输入Access Key Secret"
                    show-password
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="数据同步">
              <div class="sync-settings">
                <el-switch
                  v-model="dataSync"
                  @change="toggleSync"
                  :disabled="!isConnected"
                />
                <span class="sync-text">{{
                  dataSync ? "已开启" : "已关闭"
                }}</span>
                <el-select
                  v-model="syncInterval"
                  size="small"
                  @change="updateSyncInterval"
                  :disabled="!isConnected"
                  style="width: 120px; margin-left: 20px"
                >
                  <el-option label="1分钟" :value="60000" />
                  <el-option label="5分钟" :value="300000" />
                  <el-option label="15分钟" :value="900000" />
                  <el-option label="30分钟" :value="1800000" />
                  <el-option label="1小时" :value="3600000" />
                </el-select>
                <span class="sync-text">同步频率</span>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveConfig">保存配置</el-button>
              <el-button @click="resetConfig">重置</el-button>
              <el-button type="warning" @click="exportConfig"
                >导出配置</el-button
              >
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 设备映射 -->
      <el-tab-pane label="设备映射" name="mapping">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>本地设备与云端设备映射</span>
              <el-button type="primary" size="small" @click="addMapping">
                <el-icon><Plus /></el-icon>
                添加映射
              </el-button>
            </div>
          </template>

          <el-table :data="deviceMappings" style="width: 100%">
            <el-table-column prop="localDevice" label="本地设备" width="180" />
            <el-table-column prop="localType" label="设备类型" width="120">
              <template #default="scope">
                <el-tag size="small">{{ scope.row.localType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="映射关系" width="80" align="center">
              <template #default>
                <el-icon color="#409eff"><Right /></el-icon>
              </template>
            </el-table-column>
            <el-table-column prop="cloudDevice" label="云端设备" width="180" />
            <el-table-column prop="cloudProduct" label="云端产品" width="150" />
            <el-table-column label="同步状态" width="100">
              <template #default="scope">
                <el-tag
                  :type="scope.row.syncEnabled ? 'success' : 'info'"
                  size="small"
                >
                  {{ scope.row.syncEnabled ? "已启用" : "已禁用" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最后同步" width="160">
              <template #default="scope">
                {{ scope.row.lastSync || "--" }}
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="200">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="syncSingleDevice(scope.row)"
                >
                  同步
                </el-button>
                <el-button
                  type="warning"
                  size="small"
                  @click="editMapping(scope.row)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="deleteMapping(scope.row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 同步日志 -->
      <el-tab-pane label="同步日志" name="logs">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>数据同步日志</span>
              <div class="header-actions">
                <el-date-picker
                  v-model="logDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  size="small"
                  style="width: 240px"
                />
                <el-button type="primary" size="small" @click="exportLogs">
                  <el-icon><Download /></el-icon>
                  导出日志
                </el-button>
                <el-button type="danger" size="small" @click="clearLogs">
                  <el-icon><Delete /></el-icon>
                  清空日志
                </el-button>
              </div>
            </div>
          </template>

          <el-table :data="filteredLogs" style="width: 100%" max-height="400">
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column label="状态" width="80">
              <template #default="scope">
                <el-tag
                  :type="scope.row.success ? 'success' : 'danger'"
                  size="small"
                >
                  {{ scope.row.success ? "成功" : "失败" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag size="small" :type="getLogTypeTag(scope.row.type)">
                  {{ scope.row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="device" label="设备" width="150" />
            <el-table-column prop="dataCount" label="数据量" width="80" />
            <el-table-column prop="duration" label="耗时(ms)" width="100" />
            <el-table-column
              prop="message"
              label="消息"
              show-overflow-tooltip
            />
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="logPagination.currentPage"
              v-model:page-size="logPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="syncLogs.length"
              layout="total, sizes, prev, pager, next, jumper"
              background
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 告警规则 -->
      <el-tab-pane label="告警规则" name="alerts">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>云端告警规则配置</span>
              <el-button type="primary" size="small" @click="addAlertRule">
                <el-icon><Plus /></el-icon>
                添加规则
              </el-button>
            </div>
          </template>

          <el-table :data="alertRules" style="width: 100%">
            <el-table-column prop="name" label="规则名称" width="180" />
            <el-table-column prop="device" label="关联设备" width="150" />
            <el-table-column prop="metric" label="监控指标" width="120" />
            <el-table-column label="条件" width="150">
              <template #default="scope">
                {{ scope.row.operator }} {{ scope.row.threshold }}
                {{ scope.row.unit }}
              </template>
            </el-table-column>
            <el-table-column label="通知方式" width="180">
              <template #default="scope">
                <el-tag
                  v-for="method in scope.row.notifyMethods"
                  :key="method"
                  size="small"
                  style="margin-right: 4px"
                >
                  {{ method }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="scope">
                <el-switch
                  v-model="scope.row.enabled"
                  @change="toggleAlertRule(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="150">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="editAlertRule(scope.row)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="deleteAlertRule(scope.row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 实时数据 -->
      <el-tab-pane label="实时数据" name="realtime">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>云端实时数据预览</span>
              <div class="header-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="refreshRealtimeData"
                  :loading="refreshing"
                >
                  <el-icon><Refresh /></el-icon>
                  刷新数据
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  @click="exportRealtimeData"
                >
                  <el-icon><Download /></el-icon>
                  导出数据
                </el-button>
              </div>
            </div>
          </template>

          <el-row :gutter="20">
            <el-col :span="16">
              <div class="realtime-chart" ref="chartContainer"></div>
            </el-col>
            <el-col :span="8">
              <div class="realtime-stats">
                <div class="stat-card-small">
                  <div class="stat-title">温度</div>
                  <div class="stat-value">{{ realtimeData.temperature }}°C</div>
                  <div class="stat-trend up">
                    <el-icon><Top /></el-icon>
                    +2.3%
                  </div>
                </div>
                <div class="stat-card-small">
                  <div class="stat-title">湿度</div>
                  <div class="stat-value">{{ realtimeData.humidity }}%</div>
                  <div class="stat-trend down">
                    <el-icon><Bottom /></el-icon>
                    -1.2%
                  </div>
                </div>
                <div class="stat-card-small">
                  <div class="stat-title">光照</div>
                  <div class="stat-value">{{ realtimeData.light }} lux</div>
                  <div class="stat-trend up">
                    <el-icon><Top /></el-icon>
                    +5.8%
                  </div>
                </div>
                <div class="stat-card-small">
                  <div class="stat-title">PM2.5</div>
                  <div class="stat-value">{{ realtimeData.pm25 }} μg/m³</div>
                  <div class="stat-trend down">
                    <el-icon><Bottom /></el-icon>
                    -3.1%
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 设备映射对话框 -->
    <el-dialog
      v-model="mappingDialogVisible"
      :title="isEditMapping ? '编辑映射' : '添加映射'"
      width="500px"
    >
      <el-form :model="mappingForm" label-width="100px">
        <el-form-item label="本地设备">
          <el-select
            v-model="mappingForm.localDevice"
            placeholder="选择本地设备"
            style="width: 100%"
          >
            <el-option label="温湿度传感器-01" value="temp_hum_01" />
            <el-option label="光照传感器-01" value="light_01" />
            <el-option label="路灯控制器-01" value="lamp_01" />
          </el-select>
        </el-form-item>
        <el-form-item label="云端产品">
          <el-input
            v-model="mappingForm.cloudProduct"
            placeholder="输入云端产品名称"
          />
        </el-form-item>
        <el-form-item label="云端设备">
          <el-input
            v-model="mappingForm.cloudDevice"
            placeholder="输入云端设备名称"
          />
        </el-form-item>
        <el-form-item label="启用同步">
          <el-switch v-model="mappingForm.syncEnabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mappingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMapping">确定</el-button>
      </template>
    </el-dialog>

    <!-- 告警规则对话框 -->
    <el-dialog
      v-model="alertDialogVisible"
      :title="isEditAlert ? '编辑告警规则' : '添加告警规则'"
      width="550px"
    >
      <el-form :model="alertForm" label-width="100px">
        <el-form-item label="规则名称">
          <el-input v-model="alertForm.name" placeholder="输入规则名称" />
        </el-form-item>
        <el-form-item label="关联设备">
          <el-select
            v-model="alertForm.device"
            placeholder="选择设备"
            style="width: 100%"
          >
            <el-option label="全部设备" value="all" />
            <el-option label="温湿度传感器-01" value="temp_hum_01" />
            <el-option label="光照传感器-01" value="light_01" />
          </el-select>
        </el-form-item>
        <el-form-item label="监控指标">
          <el-select
            v-model="alertForm.metric"
            placeholder="选择指标"
            style="width: 100%"
          >
            <el-option label="温度" value="temperature" />
            <el-option label="湿度" value="humidity" />
            <el-option label="光照" value="light" />
            <el-option label="PM2.5" value="pm25" />
            <el-option label="设备离线" value="offline" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发条件">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="alertForm.operator">
                <el-option label="大于" value=">" />
                <el-option label="小于" value="<" />
                <el-option label="等于" value="=" />
                <el-option label="大于等于" value=">=" />
                <el-option label="小于等于" value="<=" />
              </el-select>
            </el-col>
            <el-col :span="10">
              <el-input-number
                v-model="alertForm.threshold"
                :min="0"
                style="width: 100%"
              />
            </el-col>
            <el-col :span="6">
              <el-input v-model="alertForm.unit" placeholder="单位" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="通知方式">
          <el-checkbox-group v-model="alertForm.notifyMethods">
            <el-checkbox label="短信" />
            <el-checkbox label="邮件" />
            <el-checkbox label="钉钉" />
            <el-checkbox label="微信" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="启用规则">
          <el-switch v-model="alertForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="alertDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAlertRule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Connection,
  DataLine,
  CircleCheck,
  CircleClose,
  Link,
  Plus,
  Right,
  Download,
  Delete,
  Refresh,
  Top,
  Bottom,
} from "@element-plus/icons-vue";
import * as echarts from "echarts";

const activeTab = ref("config");
const cloudFormRef = ref(null);
const chartContainer = ref(null);
let chart = null;

const connecting = ref(false);
const testing = ref(false);
const refreshing = ref(false);
const isConnected = ref(false);
const dataSync = ref(false);
const syncInterval = ref(300000);
const syncCount = ref(0);
const syncSuccessCount = ref(0);
const syncFailedCount = ref(0);

const cloudForm = reactive({
  platform: "aliyun",
  productKey: "",
  deviceName: "",
  accessKey: "",
  secretKey: "",
  region: "cn-shanghai",
});

const rules = {
  platform: [
    { required: true, message: "请选择云平台类型", trigger: "change" },
  ],
  productKey: [{ required: true, message: "请输入产品Key", trigger: "blur" }],
  deviceName: [{ required: true, message: "请输入设备名称", trigger: "blur" }],
  accessKey: [{ required: true, message: "请输入Access Key", trigger: "blur" }],
  secretKey: [{ required: true, message: "请输入Secret Key", trigger: "blur" }],
};

const deviceMappings = ref([
  {
    id: 1,
    localDevice: "temp_hum_01",
    localType: "传感器",
    cloudDevice: "device_001",
    cloudProduct: "环境监测",
    syncEnabled: true,
    lastSync: "2024-01-15 10:30:00",
  },
  {
    id: 2,
    localDevice: "light_01",
    localType: "传感器",
    cloudDevice: "device_002",
    cloudProduct: "环境监测",
    syncEnabled: true,
    lastSync: "2024-01-15 10:30:00",
  },
  {
    id: 3,
    localDevice: "lamp_01",
    localType: "控制器",
    cloudDevice: "device_003",
    cloudProduct: "智能路灯",
    syncEnabled: false,
    lastSync: "--",
  },
]);

const syncLogs = ref([
  {
    id: 1,
    time: "2024-01-15 10:30:00",
    success: true,
    type: "自动同步",
    device: "temp_hum_01",
    dataCount: 120,
    duration: 156,
    message: "同步成功",
  },
  {
    id: 2,
    time: "2024-01-15 10:25:00",
    success: true,
    type: "自动同步",
    device: "light_01",
    dataCount: 85,
    duration: 98,
    message: "同步成功",
  },
  {
    id: 3,
    time: "2024-01-15 10:20:00",
    success: false,
    type: "手动同步",
    device: "lamp_01",
    dataCount: 0,
    duration: 0,
    message: "连接超时",
  },
]);

const logDateRange = ref([]);
const logPagination = reactive({ currentPage: 1, pageSize: 10 });

const alertRules = ref([
  {
    id: 1,
    name: "高温告警",
    device: "all",
    metric: "温度",
    operator: ">",
    threshold: 35,
    unit: "°C",
    notifyMethods: ["短信", "邮件"],
    enabled: true,
  },
  {
    id: 2,
    name: "设备离线告警",
    device: "all",
    metric: "设备离线",
    operator: ">",
    threshold: 5,
    unit: "分钟",
    notifyMethods: ["短信", "钉钉"],
    enabled: true,
  },
]);

const realtimeData = reactive({
  temperature: 25.6,
  humidity: 68,
  light: 1250,
  pm25: 35,
});

const mappingDialogVisible = ref(false);
const isEditMapping = ref(false);
const mappingForm = reactive({
  id: null,
  localDevice: "",
  cloudProduct: "",
  cloudDevice: "",
  syncEnabled: true,
});

const alertDialogVisible = ref(false);
const isEditAlert = ref(false);
const alertForm = reactive({
  id: null,
  name: "",
  device: "",
  metric: "",
  operator: ">",
  threshold: 0,
  unit: "",
  notifyMethods: [],
  enabled: true,
});

const getPlatformName = (platform) => {
  const names = {
    aliyun: "阿里云IoT",
    huaweicloud: "华为云IoT",
    tencentcloud: "腾讯云IoT",
    baiduyun: "百度云IoT",
    onenet: "OneNET",
    custom: "自定义MQTT",
  };
  return names[platform] || platform;
};

const getLogTypeTag = (type) => {
  const tags = {
    自动同步: "primary",
    手动同步: "warning",
    定时同步: "success",
  };
  return tags[type] || "info";
};

const filteredLogs = computed(() => {
  const start = (logPagination.currentPage - 1) * logPagination.pageSize;
  return syncLogs.value.slice(start, start + logPagination.pageSize);
});

const testConnection = async () => {
  testing.value = true;
  try {
    await new Promise((resolve) => setTimeout(resolve, 1500));
    ElMessage.success("连接测试成功");
  } catch (error) {
    ElMessage.error("连接测试失败");
  } finally {
    testing.value = false;
  }
};

const connectCloud = async () => {
  if (isConnected.value) {
    try {
      await ElMessageBox.confirm("确定要断开云平台连接吗？", "提示", {
        type: "warning",
      });
      isConnected.value = false;
      dataSync.value = false;
      stopSync();
      ElMessage.success("已断开云平台连接");
    } catch {}
    return;
  }

  if (!cloudFormRef.value) return;
  await cloudFormRef.value.validate(async (valid) => {
    if (valid) {
      connecting.value = true;
      try {
        await new Promise((resolve) => setTimeout(resolve, 2000));
        isConnected.value = true;
        dataSync.value = true;
        ElMessage.success("云平台连接成功");
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
      localStorage.setItem("cloudConfig", JSON.stringify(cloudForm));
      ElMessage.success("配置保存成功");
    }
  });
};

const resetConfig = () => {
  Object.assign(cloudForm, {
    platform: "aliyun",
    productKey: "",
    deviceName: "",
    accessKey: "",
    secretKey: "",
    region: "cn-shanghai",
  });
  ElMessage.info("配置已重置");
};

const exportConfig = () => {
  const config = JSON.stringify(cloudForm, null, 2);
  const blob = new Blob([config], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "cloud-config.json";
  a.click();
  ElMessage.success("配置已导出");
};

const toggleSync = (value) => {
  value ? startSync() : stopSync();
};

const updateSyncInterval = () => {
  if (dataSync.value) {
    stopSync();
    startSync();
  }
};

let syncTimer = null;
const startSync = () => {
  if (syncTimer) clearInterval(syncTimer);
  syncTimer = setInterval(() => syncData(), syncInterval.value);
};

const stopSync = () => {
  if (syncTimer) {
    clearInterval(syncTimer);
    syncTimer = null;
  }
};

const syncData = async () => {
  try {
    await new Promise((resolve) => setTimeout(resolve, 500));
    syncCount.value++;
    Math.random() > 0.1 ? syncSuccessCount.value++ : syncFailedCount.value++;
    const now = new Date().toLocaleString();
    syncLogs.value.unshift({
      id: Date.now(),
      time: now,
      success: Math.random() > 0.1,
      type: "自动同步",
      device: "temp_hum_01",
      dataCount: Math.floor(Math.random() * 100) + 50,
      duration: Math.floor(Math.random() * 200) + 50,
      message: Math.random() > 0.1 ? "同步成功" : "连接超时",
    });
  } catch {
    syncFailedCount.value++;
  }
};

const addMapping = () => {
  isEditMapping.value = false;
  Object.assign(mappingForm, {
    id: null,
    localDevice: "",
    cloudProduct: "",
    cloudDevice: "",
    syncEnabled: true,
  });
  mappingDialogVisible.value = true;
};

const editMapping = (row) => {
  isEditMapping.value = true;
  Object.assign(mappingForm, row);
  mappingDialogVisible.value = true;
};

const saveMapping = () => {
  if (isEditMapping.value) {
    const index = deviceMappings.value.findIndex(
      (m) => m.id === mappingForm.id,
    );
    if (index > -1) Object.assign(deviceMappings.value[index], mappingForm);
  } else {
    deviceMappings.value.push({
      ...mappingForm,
      id: Date.now(),
      localType: "传感器",
      lastSync: "--",
    });
  }
  mappingDialogVisible.value = false;
  ElMessage.success(isEditMapping.value ? "映射已更新" : "映射已添加");
};

const deleteMapping = (row) => {
  ElMessageBox.confirm(`确定删除设备 ${row.localDevice} 的映射吗？`, "提示", {
    type: "warning",
  })
    .then(() => {
      deviceMappings.value = deviceMappings.value.filter(
        (m) => m.id !== row.id,
      );
      ElMessage.success("删除成功");
    })
    .catch(() => {});
};

const syncSingleDevice = async (row) => {
  ElMessage.info(`正在同步设备 ${row.localDevice}...`);
  await new Promise((resolve) => setTimeout(resolve, 1000));
  row.lastSync = new Date().toLocaleString();
  ElMessage.success("同步成功");
};

const exportLogs = () => {
  const logs = JSON.stringify(syncLogs.value, null, 2);
  const blob = new Blob([logs], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `sync-logs-${new Date().toISOString().slice(0, 10)}.json`;
  a.click();
  ElMessage.success("日志已导出");
};

const clearLogs = () => {
  ElMessageBox.confirm("确定要清空所有同步日志吗？", "警告", {
    type: "warning",
  })
    .then(() => {
      syncLogs.value = [];
      ElMessage.success("日志已清空");
    })
    .catch(() => {});
};

const addAlertRule = () => {
  isEditAlert.value = false;
  Object.assign(alertForm, {
    id: null,
    name: "",
    device: "",
    metric: "",
    operator: ">",
    threshold: 0,
    unit: "",
    notifyMethods: [],
    enabled: true,
  });
  alertDialogVisible.value = true;
};

const editAlertRule = (row) => {
  isEditAlert.value = true;
  Object.assign(alertForm, row);
  alertDialogVisible.value = true;
};

const saveAlertRule = () => {
  if (isEditAlert.value) {
    const index = alertRules.value.findIndex((r) => r.id === alertForm.id);
    if (index > -1) Object.assign(alertRules.value[index], alertForm);
  } else {
    alertRules.value.push({ ...alertForm, id: Date.now() });
  }
  alertDialogVisible.value = false;
  ElMessage.success(isEditAlert.value ? "规则已更新" : "规则已添加");
};

const deleteAlertRule = (row) => {
  ElMessageBox.confirm(`确定删除告警规则 ${row.name} 吗？`, "提示", {
    type: "warning",
  })
    .then(() => {
      alertRules.value = alertRules.value.filter((r) => r.id !== row.id);
      ElMessage.success("删除成功");
    })
    .catch(() => {});
};

const toggleAlertRule = (row) => {
  ElMessage.success(`规则 ${row.name} 已${row.enabled ? "启用" : "禁用"}`);
};

const refreshRealtimeData = async () => {
  refreshing.value = true;
  await new Promise((resolve) => setTimeout(resolve, 800));
  realtimeData.temperature = (25 + Math.random() * 5).toFixed(1);
  realtimeData.humidity = Math.floor(60 + Math.random() * 20);
  realtimeData.light = Math.floor(1000 + Math.random() * 500);
  realtimeData.pm25 = Math.floor(30 + Math.random() * 30);
  updateChart();
  refreshing.value = false;
  ElMessage.success("数据已刷新");
};

const exportRealtimeData = () => {
  const data = JSON.stringify(realtimeData, null, 2);
  const blob = new Blob([data], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `realtime-data-${new Date().toISOString().slice(0, 10)}.json`;
  a.click();
  ElMessage.success("数据已导出");
};

const initChart = () => {
  if (!chartContainer.value) return;
  chart = echarts.init(chartContainer.value);
  updateChart();
};

const updateChart = () => {
  if (!chart) return;
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`);
  const tempData = hours.map(() => (20 + Math.random() * 15).toFixed(1));
  const humData = hours.map(() => Math.floor(50 + Math.random() * 40));
  chart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["温度", "湿度"], bottom: 0 },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "15%",
      top: "10%",
      containLabel: true,
    },
    xAxis: { type: "category", boundaryGap: false, data: hours },
    yAxis: [
      { type: "value", name: "温度(°C)", position: "left" },
      { type: "value", name: "湿度(%)", position: "right", max: 100 },
    ],
    series: [
      {
        name: "温度",
        type: "line",
        smooth: true,
        data: tempData,
        itemStyle: { color: "#f56c6c" },
        areaStyle: { color: "rgba(245, 108, 108, 0.1)" },
      },
      {
        name: "湿度",
        type: "line",
        smooth: true,
        yAxisIndex: 1,
        data: humData,
        itemStyle: { color: "#409eff" },
        areaStyle: { color: "rgba(64, 158, 255, 0.1)" },
      },
    ],
  });
};

const initConfig = () => {
  const savedConfig = localStorage.getItem("cloudConfig");
  if (savedConfig) {
    try {
      Object.assign(cloudForm, JSON.parse(savedConfig));
    } catch {}
  }
};

onMounted(() => {
  initConfig();
  nextTick(() => {
    if (activeTab.value === "realtime") initChart();
  });
});

onUnmounted(() => {
  if (chart) {
    chart.dispose();
    chart = null;
  }
  stopSync();
});
</script>

<style scoped>
.cloud-platform {
  margin: 20px 0;
}

.status-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
}

.stat-card.connected {
  border: 2px solid #67c23a;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.cloud-tabs {
  margin-top: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.config-form {
  max-width: 800px;
}

.platform-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.sync-settings {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sync-text {
  color: #606266;
  font-size: 14px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.realtime-chart {
  width: 100%;
  height: 350px;
}

.realtime-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.stat-card-small {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
}

.stat-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-card-small .stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stat-trend {
  font-size: 12px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.stat-trend.up {
  color: #67c23a;
}

.stat-trend.down {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .header-actions {
    flex-wrap: wrap;
  }

  .realtime-stats {
    grid-template-columns: 1fr;
  }
}
</style>
