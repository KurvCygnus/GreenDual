<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card stat-device" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-box">
              <el-icon size="32" color="#409eff"><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.deviceTotal }}</div>
              <div class="stat-label">设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card stat-online" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-box">
              <el-icon size="32" color="#67c23a"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.deviceOnline }}</div>
              <div class="stat-label">在线设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card stat-warning" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-box">
              <el-icon size="32" color="#e6a23c"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.alertCount }}</div>
              <div class="stat-label">告警数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card stat-energy" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-box">
              <el-icon size="32" color="#f56c6c"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalRecords }}</div>
              <div class="stat-label">监测记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时环境数据曲线 -->
    <el-card shadow="hover" class="chart-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon size="20" color="#409EFF"><Odometer /></el-icon>
            <span class="header-title">实时环境数据</span>
          </div>
          <div class="header-right">
            <el-select
              v-model="chartDataType"
              size="small"
              @change="refreshChart"
              style="width: 130px"
            >
              <el-option label="全部" value="all" />
              <el-option label="温度" value="temperature" />
              <el-option label="湿度" value="humidity" />
              <el-option label="光照" value="light" />
            </el-select>
            <el-button size="small" :icon="Refresh" @click="refreshAll"
              >刷新</el-button
            >
          </div>
        </div>
      </template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>

    <!-- 底部：设备在线状态列表 + 最近告警 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="12">
        <el-card shadow="hover" class="device-card">
          <template #header>
            <div class="card-header">
              <el-icon size="18" color="#409EFF"><Monitor /></el-icon>
              <span>设备在线状态</span>
            </div>
          </template>
          <div class="device-list" v-loading="deviceLoading">
            <div v-if="deviceList.length === 0" class="empty-tip">暂无设备数据</div>
            <div
              v-for="device in deviceList"
              :key="device.id"
              class="device-item"
            >
              <div class="device-left">
                <el-tag
                  :type="device.onLine === 1 ? 'success' : 'info'"
                  size="small"
                  effect="dark"
                >
                  {{ device.onLine === 1 ? "在线" : "离线" }}
                </el-tag>
                <span class="device-name">{{ device.name }}</span>
              </div>
              <div class="device-data">
                <span>{{ device.temperature != null ? device.temperature + "°C" : "--" }}</span>
                <span>{{ device.humidity != null ? device.humidity + "%" : "--" }}</span>
                <span>{{ device.lightIntensity != null ? device.lightIntensity + "lux" : "--" }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="alert-card">
          <template #header>
            <div class="card-header">
              <el-icon size="18" color="#e6a23c"><Warning /></el-icon>
              <span>最近告警</span>
            </div>
          </template>
          <div class="alert-list" v-loading="alertLoading">
            <div v-if="alertList.length === 0" class="empty-tip">暂无告警记录</div>
            <div
              v-for="alert in alertList"
              :key="alert.recordId || alert.id"
              class="alert-item"
            >
              <el-tag
                :type="alert.currentStatus === 'abnormal' ? 'danger' : 'warning'"
                size="small"
              >
                {{ alert.currentStatus === "abnormal" ? "异常" : "预警" }}
              </el-tag>
              <span class="alert-device">{{ alert.deviceName }}</span>
              <span class="alert-type">{{ getDataTypeName(alert.dataType) }}</span>
              <span class="alert-value">{{ alert.value }} {{ getUnit(alert.dataType) }}</span>
              <span class="alert-time">{{ formatTime(alert.recordTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from "vue";
import * as echarts from "echarts";
import { ElMessage } from "element-plus";
import {
  Monitor,
  Connection,
  Warning,
  TrendCharts,
  Odometer,
  Refresh,
} from "@element-plus/icons-vue";
import {
  getDeviceStats,
  getDeviceList,
  getRecordStats,
  getRecordList,
  getWarningList,
} from "@/api/auth";

const chartRef = ref(null);
const chartDataType = ref("all");
const deviceLoading = ref(false);
const alertLoading = ref(false);

// 统计数据
const stats = reactive({
  deviceTotal: 0,
  deviceOnline: 0,
  alertCount: 0,
  totalRecords: 0,
});

// 设备列表
const deviceList = ref([]);

// 告警列表
const alertList = ref([]);

// ECharts 实例
let chartInstance = null;
let refreshTimer = null;

// 数据工具
const getDataTypeName = (type) => {
  const map = { temperature: "温度", humidity: "湿度", light: "光照", lightIntensity: "光照" };
  return map[type] || type;
};

const getUnit = (type) => {
  const map = { temperature: "°C", humidity: "%", light: "lux", lightIntensity: "lux" };
  return map[type] || "";
};

const formatTime = (time) => {
  if (!time) return "";
  const d = new Date(time);
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
};

// 获取统计数据
const fetchStats = async () => {
  try {
    const [deviceResp, recordResp] = await Promise.all([
      getDeviceStats(),
      getRecordStats(),
    ]);

    if (deviceResp.data.code === 1) {
      const ds = deviceResp.data.data;
      stats.deviceTotal = ds.deviceCount || 0;
      stats.deviceOnline = ds.onlineCount || 0;
    }

    if (recordResp.data.code === 1) {
      const rs = recordResp.data.data;
      stats.totalRecords = rs.total || 0;
      stats.alertCount = (rs.warning || 0) + (rs.abnormal || 0);
    }
  } catch (e) {
    console.error("获取统计数据失败", e);
  }
};

// 获取设备列表
const fetchDevices = async () => {
  deviceLoading.value = true;
  try {
    const resp = await getDeviceList({ offset: 0, pageSize: 50 });
    if (resp.data.code === 1) {
      deviceList.value = resp.data.data || [];
    }
  } catch (e) {
    console.error("获取设备列表失败", e);
  } finally {
    deviceLoading.value = false;
  }
};

// 获取最近告警
const fetchAlerts = async () => {
  alertLoading.value = true;
  try {
    const resp = await getWarningList({
      page: 1,
      pageSize: 10,
    });
    if (resp.data.code === 1) {
      alertList.value = resp.data.data || [];
    }
  } catch (e) {
    console.error("获取告警列表失败", e);
  } finally {
    alertLoading.value = false;
  }
};

// 获取图表数据
const fetchChartData = async () => {
  try {
    const params = { page: 1, pageSize: 50 };
    if (chartDataType.value !== "all") {
      params.dataType = chartDataType.value;
    }
    const resp = await getRecordList(params);
    if (resp.data.code === 1) {
      return resp.data.data || [];
    }
    return [];
  } catch (e) {
    console.error("获取图表数据失败", e);
    return [];
  }
};

// 初始化 ECharts
const initChart = async () => {
  if (!chartRef.value) return;
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }

  const records = await fetchChartData();

  // 按时间排序
  records.sort((a, b) => new Date(a.recordTime) - new Date(b.recordTime));

  const times = records.map((r) => {
    const d = new Date(r.recordTime);
    return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
  });

  const temperatureData = [];
  const humidityData = [];
  const lightData = [];
  const timeLabels = [];

  records.forEach((r) => {
    const timeKey = (() => {
      const d = new Date(r.recordTime);
      return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
    })();

    if (r.dataType === "temperature" || chartDataType.value === "temperature") {
      temperatureData.push({ name: timeKey, value: [timeKey, r.value] });
    }
    if (r.dataType === "humidity" || chartDataType.value === "humidity") {
      humidityData.push({ name: timeKey, value: [timeKey, r.value] });
    }
    if (r.dataType === "light" || r.dataType === "lightIntensity" || chartDataType.value === "light") {
      lightData.push({ name: timeKey, value: [timeKey, r.value] });
    }
    if (!timeLabels.includes(timeKey)) timeLabels.push(timeKey);
  });

  // 按数据类型分组
  const series = [];
  if (temperatureData.length > 0) {
    series.push({
      name: "温度(°C)",
      type: "line",
      smooth: true,
      data: temperatureData.map((d) => d.value),
    });
  }
  if (humidityData.length > 0) {
    series.push({
      name: "湿度(%)",
      type: "line",
      smooth: true,
      data: humidityData.map((d) => d.value),
    });
  }
  if (lightData.length > 0) {
    series.push({
      name: "光照(lux)",
      type: "line",
      smooth: true,
      yAxisIndex: 1,
      data: lightData.map((d) => d.value),
    });
  }

  const option = {
    tooltip: { trigger: "axis" },
    legend: { data: ["温度(°C)", "湿度(%)", "光照(lux)"], bottom: 0 },
    grid: { left: "3%", right: "4%", bottom: "15%", top: "5%", containLabel: true },
    xAxis: {
      type: "category",
      data: timeLabels,
      boundaryGap: false,
      axisLabel: { fontSize: 11 },
    },
    yAxis: [
      { type: "value", name: "温度/湿度" },
      { type: "value", name: "光照(lux)", splitLine: { show: false } },
    ],
    series,
    color: ["#f56c6c", "#409eff", "#e6a23c"],
  };

  chartInstance.setOption(option, true);
};

const refreshChart = () => {
  initChart();
};

const refreshAll = () => {
  ElMessage.success("正在刷新数据...");
  fetchStats();
  fetchDevices();
  fetchAlerts();
  initChart();
};

// 窗口大小变化自适应
const handleResize = () => {
  if (chartInstance) chartInstance.resize();
};

onMounted(async () => {
  await Promise.all([fetchStats(), fetchDevices(), fetchAlerts()]);
  await nextTick();
  await initChart();
  window.addEventListener("resize", handleResize);

  // 每30秒自动刷新
  refreshTimer = setInterval(() => {
    fetchStats();
    fetchAlerts();
    initChart();
  }, 30000);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  if (refreshTimer) clearInterval(refreshTimer);
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
});
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-box {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(64, 158, 255, 0.1);
}

.stat-device .stat-icon-box { background: rgba(64, 158, 255, 0.1); }
.stat-online .stat-icon-box { background: rgba(103, 194, 58, 0.1); }
.stat-warning .stat-icon-box { background: rgba(230, 162, 60, 0.1); }
.stat-energy .stat-icon-box { background: rgba(245, 108, 108, 0.1); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  font-weight: 600;
  font-size: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-container {
  height: 350px;
}

.bottom-row {
  margin-bottom: 20px;
}

.device-list,
.alert-list {
  max-height: 350px;
  overflow-y: auto;
}

.device-item,
.alert-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.device-item:last-child,
.alert-item:last-child {
  border-bottom: none;
}

.device-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 140px;
}

.device-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.device-data {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #606266;
}

.alert-device {
  font-weight: 500;
  color: #303133;
  min-width: 100px;
}

.alert-type {
  color: #909399;
  min-width: 40px;
}

.alert-value {
  color: #e6a23c;
  min-width: 60px;
}

.alert-time {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
}

@media (max-width: 768px) {
  .dashboard { padding: 10px; }
  .stat-value { font-size: 22px; }
  .chart-container { height: 250px; }
  .device-item, .alert-item { flex-wrap: wrap; }
}
</style>
