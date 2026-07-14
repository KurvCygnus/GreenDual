<template>
  <div class="ai-prediction">
    <!-- AI 环境趋势预测 -->
    <el-card shadow="hover" class="ai-card prediction-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon size="22" color="#409EFF"><Cpu /></el-icon>
            <span class="header-title">AI 环境趋势预测</span>
          </div>
          <el-button
            type="primary"
            size="small"
            @click="predict"
            :loading="loading"
            class="predict-btn"
          >
            <el-icon><Refresh /></el-icon>
            开始预测
          </el-button>
        </div>
      </template>

      <div class="prediction-content">
        <div class="prediction-params">
          <el-form :inline="true" label-position="top" class="params-form">
            <el-form-item label="预测类型" class="param-item">
              <el-select
                v-model="predictType"
                size="small"
                class="param-select"
              >
                <el-option label="温度" value="temperature" />
                <el-option label="湿度" value="humidity" />
                <el-option label="光照" value="light" />
                <el-option label="全部" value="all" />
              </el-select>
            </el-form-item>

            <el-form-item label="预测时长" class="param-item">
              <el-select
                v-model="predictHours"
                size="small"
                class="param-select"
              >
                <el-option label="6小时" value="6" />
                <el-option label="12小时" value="12" />
                <el-option label="24小时" value="24" />
                <el-option label="48小时" value="48" />
              </el-select>
            </el-form-item>

            <el-form-item label="预测精度" class="param-item accuracy-item">
              <el-slider
                v-model="predictAccuracy"
                :min="1"
                :max="5"
                :step="1"
                class="accuracy-slider"
              />
              <span class="accuracy-text">{{ predictAccuracy }}星</span>
            </el-form-item>
          </el-form>
        </div>

        <div
          class="prediction-result"
          v-if="predictionData.length > 0 || aiPrediction"
        >
          <div class="result-header">
            <h3 class="result-title">预测结果</h3>
            <el-tag type="success" class="result-tag">AI 预测完成</el-tag>
          </div>

          <div class="prediction-chart" ref="predictionChartRef"></div>

          <div class="prediction-stats">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-title">预测最大值</div>
                  <div class="stat-value">{{ maxValue }} {{ unit }}</div>
                  <div class="stat-time">{{ maxTime }}</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-title">预测最小值</div>
                  <div class="stat-value">{{ minValue }} {{ unit }}</div>
                  <div class="stat-time">{{ minTime }}</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-title">预测平均值</div>
                  <div class="stat-value">{{ avgValue }} {{ unit }}</div>
                  <div class="stat-confidence">置信度: {{ confidence }}%</div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <div class="ai-analysis" v-if="aiPrediction">
            <el-divider content-position="left" class="analysis-divider">
              <el-icon size="18" color="#409EFF"><Cpu /></el-icon>
              <span class="divider-text">AI 深度分析</span>
            </el-divider>
            <el-card class="analysis-card">
              <div
                class="analysis-content"
                v-html="formatAnalysis(aiPrediction)"
              ></div>
            </el-card>
          </div>
        </div>

        <div v-else class="prediction-empty">
          <el-empty description="点击开始预测按钮生成预测结果" />
        </div>
      </div>
    </el-card>

    <!-- AI 智能助手 -->
    <el-card shadow="hover" class="ai-card chat-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon size="22" color="#409EFF"><ChatDotRound /></el-icon>
            <span class="header-title">AI 智能助手</span>
          </div>
          <el-button
            type="info"
            size="small"
            @click="clearChat"
            :loading="clearLoading"
            class="clear-btn"
          >
            <el-icon><Delete /></el-icon>
            清空对话
          </el-button>
        </div>
      </template>

      <div class="chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div
            v-for="(msg, index) in chatMessages"
            :key="index"
            :class="[
              'message',
              msg.role === 'user' ? 'user-message' : 'ai-message',
            ]"
          >
            <div class="message-avatar">
              <el-avatar
                v-if="msg.role === 'user'"
                :size="36"
                icon="User"
                class="user-avatar"
              />
              <el-avatar
                v-else
                :size="36"
                style="
                  background: linear-gradient(135deg, #409eff 0%, #38bdf8 100%);
                "
                icon="Cpu"
                class="ai-avatar"
              />
            </div>
            <div class="message-content">
              <div
                class="message-text"
                v-html="formatMessage(msg.content)"
              ></div>
            </div>
          </div>
          <div v-if="chatLoading" class="message ai-message">
            <div class="message-avatar">
              <el-avatar
                :size="36"
                style="
                  background: linear-gradient(135deg, #409eff 0%, #38bdf8 100%);
                "
                icon="Cpu"
                class="ai-avatar"
              />
            </div>
            <div class="message-content">
              <div class="message-text loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                AI 正在思考中...
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="chatInput"
            placeholder="输入您的问题，例如：分析当前环境状况..."
            @keyup.enter="sendMessage"
            :disabled="chatLoading"
            class="chat-input-field"
          >
            <template #append>
              <el-button
                type="primary"
                @click="sendMessage"
                :loading="chatLoading"
                class="send-btn"
              >
                <el-icon><Send /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="quick-actions">
          <el-button
            size="small"
            @click="quickAsk('分析当前环境数据')"
            class="quick-btn"
            >分析环境</el-button
          >
          <el-button
            size="small"
            @click="quickAsk('给出环境改善建议')"
            class="quick-btn"
            >改善建议</el-button
          >
          <el-button
            size="small"
            @click="quickAsk('预测未来24小时趋势')"
            class="quick-btn"
            >趋势预测</el-button
          >
          <el-button
            size="small"
            @click="quickAsk('设备故障诊断')"
            class="quick-btn"
            >故障诊断</el-button
          >
        </div>
      </div>
    </el-card>

    <!-- AI 员工监测 -->
    <el-card shadow="hover" class="ai-card ai-employee-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon size="22" color="#409EFF"><UserFilled /></el-icon>
            <span class="header-title">AI 员工监测</span>
          </div>
          <div class="header-buttons">
            <el-button
              type="primary"
              size="small"
              @click="startAIMonitoring"
              :loading="aiMonitoringLoading"
              :disabled="aiMonitoringStatus === 'running'"
              class="monitor-btn"
            >
              <el-icon><VideoPlay /></el-icon>
              {{ aiMonitoringStatus === "running" ? "监测中" : "开始监测" }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="stopAIMonitoring"
              :disabled="aiMonitoringStatus !== 'running'"
              class="stop-btn"
            >
              <el-icon><Close /></el-icon>
              停止监测
            </el-button>
          </div>
        </div>
      </template>

      <div class="ai-employee-content">
        <div class="monitoring-controls">
          <el-form :inline="true" label-position="top" class="controls-form">
            <el-form-item label="监测设备" class="control-item">
              <el-select
                v-model="selectedDeviceId"
                size="small"
                style="width: 200px"
                class="control-select"
              >
                <el-option label="全部设备" value="all" />
                <el-option
                  v-for="device in deviceList"
                  :key="device.id"
                  :label="device.name"
                  :value="device.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="监测频率" class="control-item">
              <el-select
                v-model="monitoringInterval"
                size="small"
                class="control-select"
              >
                <el-option label="5分钟" value="5" />
                <el-option label="15分钟" value="15" />
                <el-option label="30分钟" value="30" />
                <el-option label="1小时" value="60" />
              </el-select>
            </el-form-item>

            <el-form-item label="故障处理" class="control-item">
              <el-switch
                v-model="autoHandleFaults"
                active-text="自动处理"
                inactive-text="手动处理"
                class="fault-switch"
              />
            </el-form-item>
          </el-form>
        </div>

        <div class="monitoring-status" v-if="aiMonitoringStatus === 'running'">
          <el-alert
            :title="`AI员工正在监测设备 (${monitoringInterval}分钟间隔)`"
            type="success"
            :closable="false"
            show-icon
            class="status-alert"
          />
          <div class="status-info">
            <span class="status-item"
              >上次监测时间: {{ lastMonitoringTime }}</span
            >
            <span class="status-item"
              >监测设备数: {{ monitoringDeviceCount }}</span
            >
            <span class="status-item">故障设备数: {{ faultDeviceCount }}</span>
          </div>
        </div>

        <div class="monitoring-results" v-if="monitoringLogs.length > 0">
          <el-divider content-position="left" class="results-divider">
            <el-icon size="18" color="#409EFF"><Timer /></el-icon>
            <span class="divider-text">监测日志</span>
          </el-divider>
          <el-table
            :data="monitoringLogs"
            style="width: 100%"
            class="logs-table"
          >
            <el-table-column prop="time" label="监测时间" width="180" />
            <el-table-column prop="deviceName" label="设备名称" width="150" />
            <el-table-column prop="deviceType" label="设备类型" width="120" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag
                  :type="scope.row.status === '正常' ? 'success' : 'danger'"
                  class="status-tag"
                  >{{ scope.row.status }}</el-tag
                >
              </template>
            </el-table-column>
            <el-table-column prop="data" label="监测数据" />
            <el-table-column prop="action" label="AI操作" />
          </el-table>
        </div>

        <div
          v-else-if="aiMonitoringStatus !== 'running'"
          class="monitoring-empty"
        >
          <el-empty description="点击开始监测按钮启动AI员工监测" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue";
import * as echarts from "echarts";
import { aiChat, aiClearHistory, aiPredictTrend, addRecord, getDeviceStats, getDeviceList } from "@/api/auth";
import {
  Cpu,
  Refresh,
  ChatDotRound,
  Delete,
  Loading,
  UserFilled,
  Timer,
  Warning,
  Check,
  Close,
  VideoPlay,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import MarkdownIt from 'markdown-it';

const loading = ref(false);
const predictType = ref("temperature");
const predictHours = ref(24);
const predictAccuracy = ref(3);
const predictionData = ref([]);
const predictionChartRef = ref(null);
const predictionChart = ref(null);
const aiPrediction = ref("");

const chatMessages = ref([]);
const chatInput = ref("");
const chatLoading = ref(false);
const clearLoading = ref(false);
const chatMessagesRef = ref(null);
const sessionId = ref("session_" + Date.now());

// AI员工监测相关状态
const aiMonitoringLoading = ref(false);
const aiMonitoringStatus = ref("idle"); // idle, running, stopped
const selectedDeviceId = ref("all");
const monitoringInterval = ref(15);
const autoHandleFaults = ref(true);
const lastMonitoringTime = ref("");
const monitoringDeviceCount = ref(0);
const faultDeviceCount = ref(0);
const monitoringLogs = ref([]);
const deviceList = ref([]);
const monitoringTimer = ref(null);

// 基于Markdown-It的Markdown渲染实例, 原方案采用的正则替换没有效果.
const md = new MarkdownIt({
  html: true,        // 允许 HTML 标签
  linkify: true,     // 自动转换链接
  typographer: true, // 优化标点符号
});

const unit = computed(() => {
  if (predictType.value === "temperature") return "°C";
  if (predictType.value === "humidity") return "%";
  if (predictType.value === "light") return "lux";
  return "";
});

const maxValue = computed(() => {
  if (predictionData.value.length === 0) return 0;
  return Math.max(...predictionData.value.map((item) => item.value)).toFixed(1);
});

const minValue = computed(() => {
  if (predictionData.value.length === 0) return 0;
  return Math.min(...predictionData.value.map((item) => item.value)).toFixed(1);
});

const avgValue = computed(() => {
  if (predictionData.value.length === 0) return 0;
  const sum = predictionData.value.reduce((acc, item) => acc + item.value, 0);
  return (sum / predictionData.value.length).toFixed(1);
});

const maxTime = computed(() => {
  if (predictionData.value.length === 0) return "";
  const max = Math.max(...predictionData.value.map((item) => item.value));
  const maxItem = predictionData.value.find((item) => item.value === max);
  return maxItem ? maxItem.time : "";
});

const minTime = computed(() => {
  if (predictionData.value.length === 0) return "";
  const min = Math.min(...predictionData.value.map((item) => item.value));
  const minItem = predictionData.value.find((item) => item.value === min);
  return minItem ? minItem.time : "";
});

const confidence = computed(() => {
  return (predictAccuracy.value * 20).toFixed(0);
});

const predict = async () => {
  loading.value = true;

  try {
    const data = generatePredictionData();
    predictionData.value = data;

    // 等待DOM更新后再初始化图表
    await nextTick();
    initPredictionChart();

    try {
      const response = await aiPredictTrend(
        sessionId.value + "_predict",
        predictType.value,
        parseInt(predictHours.value),
        data.slice(0, 10),
      );

      if (response.data.code === 1) {
        aiPrediction.value = response.data.data.prediction;
      }
    } catch (aiError) {
      console.error("AI预测API调用失败:", aiError);
      ElMessage.warning("AI预测服务暂不可用，请稍后重试");
    }

    // AI预测完成后自动添加监测记录
    await addPredictionRecord();
  } catch (error) {
    console.error("预测失败:", error);
    ElMessage.error("预测失败");
  } finally {
    loading.value = false;
  }
};

// 添加AI预测记录
const addPredictionRecord = async () => {
  try {
    // 格式化日期为 yyyy-MM-dd HH:mm:ss 格式
    const now = new Date();
    const formattedTime =
      now.getFullYear() +
      "-" +
      String(now.getMonth() + 1).padStart(2, "0") +
      "-" +
      String(now.getDate()).padStart(2, "0") +
      " " +
      String(now.getHours()).padStart(2, "0") +
      ":" +
      String(now.getMinutes()).padStart(2, "0") +
      ":" +
      String(now.getSeconds()).padStart(2, "0");

    // 获取预测的平均值作为记录值
    const avgVal = parseFloat(avgValue.value) || 0;

    const record = {
      deviceId: "AI-PREDICT-001",
      deviceName: "AI预测系统",
      recordTime: formattedTime,
      dataType: predictType.value,
      value: avgVal,
      status: "normal",
      remark: `AI${predictHours.value}小时${getDataTypeName(predictType.value)}预测，平均值: ${avgVal.toFixed(1)}`,
    };

    const response = await addRecord(record);
    if (response.data.code === 1) {
      console.log("AI预测记录已自动添加");
      ElMessage.success("预测完成，记录已保存");
    } else {
      console.error("添加预测记录失败:", response.data.msg);
    }
  } catch (error) {
    console.error("自动添加预测记录失败:", error);
  }
};

// 获取数据类型名称
const getDataTypeName = (type) => {
  const names = {
    temperature: "温度",
    humidity: "湿度",
    light: "光照",
    all: "环境",
  };
  return names[type] || "环境";
};

const generatePredictionData = () => {
  const data = [];
  const now = new Date();

  let baseValue = 0;
  if (predictType.value === "temperature") baseValue = 25;
  if (predictType.value === "humidity") baseValue = 60;
  if (predictType.value === "light") baseValue = 10000;

  for (let i = 0; i < parseInt(predictHours.value); i++) {
    const time = new Date(now);
    time.setHours(now.getHours() + i);
    const timeStr = time.getHours().toString().padStart(2, "0") + ":00";

    let trend = 0;
    if (predictType.value === "temperature") {
      const hour = time.getHours();
      if (hour >= 6 && hour <= 14) trend = (hour - 6) * 0.5;
      else if (hour > 14 && hour <= 22) trend = (22 - hour) * 0.5;
      else trend = -2;
    } else if (predictType.value === "humidity") {
      const hour = time.getHours();
      if (hour >= 20 || hour <= 6) trend = 5;
      else trend = -3;
    } else if (predictType.value === "light") {
      const hour = time.getHours();
      if (hour >= 6 && hour <= 18) trend = 5000;
      else trend = -8000;
    }

    const variation = (i % 5 - 2) * baseValue * 0.03;
    const value = baseValue + trend + variation;

    data.push({
      time: timeStr,
      value: Math.max(0, value),
    });
  }

  return data;
};

// 获取真实设备数据
const initPredictionChart = (retryCount = 0) => {
  if (!predictionChartRef.value) {
    console.error("图表容器不存在");
    return;
  }

  // 确保容器有尺寸
  const container = predictionChartRef.value;
  console.log("图表容器尺寸:", container.clientWidth, container.clientHeight);

  if (container.clientWidth === 0 || container.clientHeight === 0) {
    if (retryCount >= 10) {
      console.warn("图表容器尺寸为0，已达最大重试次数，停止重试");
      return;
    }
    console.warn("图表容器尺寸为0，延迟初始化");
    // 延迟100ms后重试
    setTimeout(() => {
      initPredictionChart(retryCount + 1);
    }, 100);
    return;
  }

  if (predictionChart.value) {
    predictionChart.value.dispose();
  }

  try {
    predictionChart.value = echarts.init(predictionChartRef.value);
    console.log("ECharts初始化成功");
  } catch (error) {
    console.error("ECharts初始化失败:", error);
    return;
  }

  const option = {
    title: {
      text: getChartTitle(),
      left: "center",
    },
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "cross",
        label: {
          backgroundColor: "#6a7985",
        },
      },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true,
    },
    xAxis: [
      {
        type: "category",
        boundaryGap: false,
        data: predictionData.value.map((item) => item.time),
      },
    ],
    yAxis: [
      {
        type: "value",
        name: getYAxisName(),
        axisLabel: {
          formatter: `{value} ${unit.value}`,
        },
      },
    ],
    series: [
      {
        name: getSeriesName(),
        type: "line",
        data: predictionData.value.map((item) => item.value.toFixed(1)),
        smooth: true,
        lineStyle: {
          color: getChartColor(),
          width: 3,
        },
        itemStyle: {
          color: getChartColor(),
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: getChartColor() + "80",
            },
            {
              offset: 1,
              color: getChartColor() + "20",
            },
          ]),
        },
        markLine: {
          data: [
            {
              type: "average",
              name: "平均值",
              lineStyle: {
                color: "#999",
              },
              label: {
                formatter: "平均值: {c}",
              },
            },
          ],
        },
      },
    ],
  };

  predictionChart.value.setOption(option);
};

const getChartTitle = () => {
  const titles = {
    temperature: "温度趋势预测",
    humidity: "湿度趋势预测",
    light: "光照强度趋势预测",
    all: "环境数据趋势预测",
  };
  return titles[predictType.value] || "环境数据趋势预测";
};

const getYAxisName = () => {
  const names = {
    temperature: "温度",
    humidity: "湿度",
    light: "光照强度",
    all: "数值",
  };
  return names[predictType.value] || "数值";
};

const getSeriesName = () => {
  const names = {
    temperature: "温度",
    humidity: "湿度",
    light: "光照强度",
    all: "数值",
  };
  return names[predictType.value] || "数值";
};

const getChartColor = () => {
  const colors = {
    temperature: "#F56C6C",
    humidity: "#409EFF",
    light: "#E6A23C",
    all: "#67C23A",
  };
  return colors[predictType.value] || "#67C23A";
};

const sendMessage = async () => {
  if (!chatInput.value.trim()) return;

  const userMessage = chatInput.value.trim();
  chatMessages.value.push({
    role: "user",
    content: userMessage,
  });
  chatInput.value = "";

  await nextTick();
  scrollToBottom();

  chatLoading.value = true;
  try {
    // 获取真实设备数据
    let stats = { deviceCount: 0, avgTemperature: 0, avgHumidity: 0, avgLight: 0 };
    let onlineDevices = 0;
    let offlineDevices = 0;
    try {
      const statsRes = await getDeviceStats();
      if (statsRes.data.code === 1) stats = statsRes.data.data;
      const deviceRes = await getDeviceList({ pageNum: 1, pageSize: 100 });
      if (deviceRes.data.code === 1) {
        const devices = deviceRes.data.data;
        onlineDevices = devices.filter((d) => d.onLine === 1).length;
        offlineDevices = devices.filter((d) => d.onLine === 0).length;
      }
    } catch (e) {
      console.error("获取设备数据失败:", e);
    }

    // 构建包含真实数据的消息
    const enhancedMessage = `当前环境监测系统实时数据：
- 监测设备总数：${stats.deviceCount}个
- 在线设备：${onlineDevices}个
- 离线设备：${offlineDevices}个
- 平均温度：${stats.avgTemperature}°C
- 平均湿度：${stats.avgHumidity}%
- 平均光照：${stats.avgLight}lux

用户问题：${userMessage}

请基于以上真实数据进行分析和回答。`;

    const response = await aiChat(sessionId.value, enhancedMessage);

    if (response.data.code === 1) {
      chatMessages.value.push({
        role: "assistant",
        content: response.data.data.response,
      });

      // AI分析完成后自动添加监测记录
      if (userMessage.includes("分析") || userMessage.includes("监测")) {
        await autoAddAnalysisRecord(userMessage, response.data.data.response);
      }
    } else {
      chatMessages.value.push({
        role: "assistant",
        content: "抱歉，AI服务暂时不可用，请稍后再试。",
      });
    }
  } catch (error) {
    console.error("AI对话失败:", error);
    chatMessages.value.push({
      role: "assistant",
      content: "抱歉，AI服务暂时不可用，请稍后再试。",
    });
  } finally {
    chatLoading.value = false;
    await nextTick();
    scrollToBottom();
  }
};

// 自动添加AI分析记录
const autoAddAnalysisRecord = async (question, answer) => {
  try {
    // 获取当前环境数据
    const currentData = await getCurrentEnvData();

    // 格式化日期为 yyyy-MM-dd HH:mm:ss 格式
    const now = new Date();
    const formattedTime =
      now.getFullYear() +
      "-" +
      String(now.getMonth() + 1).padStart(2, "0") +
      "-" +
      String(now.getDate()).padStart(2, "0") +
      " " +
      String(now.getHours()).padStart(2, "0") +
      ":" +
      String(now.getMinutes()).padStart(2, "0") +
      ":" +
      String(now.getSeconds()).padStart(2, "0");

    const record = {
      deviceId: currentData.deviceId || "AI-001",
      deviceName: currentData.deviceName || "AI智能分析系统",
      recordTime: formattedTime,
      dataType: detectDataType(question),
      value: currentData.value || 25.5,
      status: detectStatus(answer),
      remark: `AI分析: ${question.substring(0, 50)}...`,
    };

    const response = await addRecord(record);
    if (response.data.code === 1) {
      console.log("AI分析记录已自动添加");
    } else {
      console.error("添加记录失败:", response.data.msg);
    }
  } catch (error) {
    console.error("自动添加记录失败:", error);
  }
};

// 获取当前环境数据
const getCurrentEnvData = async () => {
  try {
    const res = await getDeviceStats();
    if (res.data.code === 1) {
      const stats = res.data.data;
      return {
        deviceId: "AI-001",
        deviceName: "AI智能分析系统",
        value: stats.avgTemperature || 25.5,
      };
    }
  } catch (error) {
    console.error("获取环境数据失败:", error);
  }
  return {
    deviceId: "AI-001",
    deviceName: "AI智能分析系统",
    value: 25.5,
  };
};

// 检测数据类型
const detectDataType = (question) => {
  if (question.includes("温度")) return "temperature";
  if (question.includes("湿度")) return "humidity";
  if (question.includes("光照")) return "light";
  return "all";
};

// 检测状态
const detectStatus = (answer) => {
  if (
    answer.includes("异常") ||
    answer.includes("故障") ||
    answer.includes("警告")
  ) {
    return "abnormal";
  }
  if (answer.includes("预警") || answer.includes("注意")) {
    return "warning";
  }
  return "normal";
};

const quickAsk = (question) => {
  chatInput.value = question;
  sendMessage();
};

// 加载设备列表
const loadDeviceList = async () => {
  try {
    const res = await getDeviceList({ pageNum: 1, pageSize: 100 });
    if (res.data.code === 1) {
      const records = res.data.data;
      if (records && records.length > 0) {
        deviceList.value = records.map((d) => ({
          id: d.deviceId || d.id || Date.now(),
          name: d.deviceName || d.deviceType || "未知设备",
          deviceType: d.deviceType || "其他",
          onLine: d.onLine !== undefined ? d.onLine : 1,
          temperature: 0,
          humidity: 0,
          lightIntensity: 0,
        }));
        return;
      }
    }
    deviceList.value = [];
  } catch (error) {
    console.error("加载设备列表失败:", error);
    deviceList.value = [];
  }
};

// 开始AI员工监测
const startAIMonitoring = async () => {
  aiMonitoringLoading.value = true;

  try {
    // 加载设备列表
    await loadDeviceList();

    // 开始监测
    aiMonitoringStatus.value = "running";

    // 立即执行一次监测
    await performMonitoring();

    // 设置定时监测
    const intervalMs = parseInt(monitoringInterval.value) * 60 * 1000;
    monitoringTimer.value = setInterval(async () => {
      await performMonitoring();
    }, intervalMs);

    ElMessage.success("AI员工监测已启动");
  } catch (error) {
    console.error("启动AI监测失败:", error);
    ElMessage.error("启动监测失败");
    aiMonitoringStatus.value = "idle";
  } finally {
    aiMonitoringLoading.value = false;
  }
};

// 停止AI员工监测
const stopAIMonitoring = () => {
  ElMessageBox.confirm("确定要停止AI员工监测吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    if (monitoringTimer.value) {
      clearInterval(monitoringTimer.value);
      monitoringTimer.value = null;
    }
    aiMonitoringStatus.value = "stopped";
    ElMessage.success("AI员工监测已停止");
  });
};

// 执行监测
const performMonitoring = async () => {
  try {
    // 更新监测时间
    const now = new Date();
    lastMonitoringTime.value = now.toLocaleString();

    // 过滤要监测的设备
    let devicesToMonitor = deviceList.value;
    if (selectedDeviceId.value !== "all") {
      devicesToMonitor = deviceList.value.filter(
        (device) => device.id === selectedDeviceId.value,
      );
    }

    monitoringDeviceCount.value = devicesToMonitor.length;
    let faults = 0;

    // 获取真实环境平均数据
    let avgTemp = 25;
    let avgHum = 55;
    let avgLight = 3000;
    try {
      const statsRes = await getDeviceStats();
      if (statsRes.data.code === 1) {
        const s = statsRes.data.data;
        avgTemp = s.avgTemperature || 25;
        avgHum = s.avgHumidity || 55;
        avgLight = s.avgLight || 3000;
      }
    } catch (e) { /* 使用默认值 */ }

    // 执行监测
    for (const device of devicesToMonitor) {
      let status = "正常";
      let action = "无操作";

      // 基于真实设备在线状态检测故障
      if (device.onLine === 0) {
        status = "故障";
        faults++;
        action = autoHandleFaults.value ? "已自动离线处理" : "需要手动处理";
      } else {
        // 正常设备，处理路灯自动控制
        if (device.deviceType === "路灯") {
          const lightStatus = await handleStreetLight(
            device,
            avgLight,
            avgTemp,
            avgHum,
          );
          action = lightStatus;
        }
      }

      // 添加监测日志
      monitoringLogs.value.unshift({
        time: now.toLocaleString(),
        deviceName: device.name,
        deviceType: device.deviceType,
        status: status,
        data: `温度: ${avgTemp.toFixed(1)}°C, 湿度: ${avgHum.toFixed(1)}%, 光照: ${avgLight.toFixed(0)}lux`,
        action: action,
      });

      // 限制日志数量
      if (monitoringLogs.value.length > 50) {
        monitoringLogs.value = monitoringLogs.value.slice(0, 50);
      }
    }

    faultDeviceCount.value = faults;

    // 添加监测记录
    await addMonitoringRecord();
  } catch (error) {
    console.error("执行监测失败:", error);
  }
};

// 处理路灯自动控制
const handleStreetLight = async (
  device,
  lightIntensity,
  temperature,
  humidity,
) => {
  // 光照强度阈值
  const lightThreshold = 2000;

  // 白天/黑夜判断
  const isNight = lightIntensity < lightThreshold;

  // 天气状况判断
  const isBadWeather = humidity > 80 || temperature < 0;

  // 自动控制逻辑
  if (isNight) {
    // 黑夜亮灯
    if (isBadWeather) {
      return "黑夜+恶劣天气，已开启路灯";
    } else {
      return "黑夜，已开启路灯";
    }
  } else {
    // 白天关灯
    return "白天，已关闭路灯";
  }
};

// 添加监测记录
const addMonitoringRecord = async () => {
  try {
    const now = new Date();
    const formattedTime = now.toLocaleString();

    const record = {
      deviceId: "AI-MONITOR-001",
      deviceName: "AI员工监测系统",
      recordTime: formattedTime,
      dataType: "monitoring",
      value: monitoringDeviceCount.value,
      status: faultDeviceCount.value > 0 ? "warning" : "normal",
      remark: `AI员工监测，设备数: ${monitoringDeviceCount.value}, 故障数: ${faultDeviceCount.value}`,
    };

    // 添加监测记录
    try {
      await addRecord(record);
    } catch (e) {
      console.error("添加监测记录失败:", e);
    }
  } catch (error) {
    console.error("添加监测记录失败:", error);
  }
};

// 初始化
onMounted(() => {
  loadDeviceList();
});

const clearChat = async () => {
  clearLoading.value = true;
  try {
    await aiClearHistory(sessionId.value);
    chatMessages.value = [];
    ElMessage.success("对话已清空");
  } catch (error) {
    console.error("清空对话失败:", error);
    chatMessages.value = [];
  } finally {
    clearLoading.value = false;
  }
};

const scrollToBottom = () => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight;
  }
};

const formatMessage = (content) => {
  if (!content) return "";
  return md.render(content);
};

const formatAnalysis = (content) => {
  if (!content) return "";
  return md.render(content);
};

// 窗口 resize 时自适应图表
const resizeHandler = () => {
  predictionChart.value?.resize();
};

onMounted(() => {
  window.addEventListener("resize", resizeHandler);

  chatMessages.value.push({
    role: "assistant",
    content:
      "您好！我是环境监测AI助手。我可以帮您分析环境数据、预测趋势、诊断设备故障等。请问有什么可以帮助您的？",
  });
});

onUnmounted(() => {
  if (monitoringTimer.value) {
    clearInterval(monitoringTimer.value);
    monitoringTimer.value = null;
  }
  predictionChart.value?.dispose();
  window.removeEventListener("resize", resizeHandler);
});
</script>

<style scoped>
.ai-prediction {
  margin: 30px 0;
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

.ai-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
  margin-bottom: 30px;
}

.ai-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border-color: #38bdf8;
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e2e8f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.predict-btn,
.clear-btn,
.monitor-btn,
.stop-btn {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.predict-btn:hover,
.monitor-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.clear-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(148, 163, 184, 0.3);
}

.stop-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(248, 113, 113, 0.3);
}

.header-buttons {
  display: flex;
  gap: 12px;
}

.prediction-content {
  padding: 20px;
}

.prediction-params {
  margin-bottom: 30px;
}

.params-form {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.param-item {
  margin-right: 0 !important;
  margin-bottom: 0 !important;
  flex: 1;
  min-width: 200px;
}

.accuracy-item {
  flex: 2;
  min-width: 300px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.param-select {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
}

.param-select:hover {
  border-color: #38bdf8;
  box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.1);
}

.accuracy-slider {
  flex: 1;
}

.accuracy-text {
  font-weight: 600;
  color: #409eff;
  min-width: 40px;
  text-align: center;
}

.prediction-result {
  margin-top: 20px;
  animation: slideIn 0.5s ease-in-out;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e2e8f0;
}

.result-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.result-tag {
  border-radius: 20px;
  font-weight: 500;
}

.prediction-chart {
  width: 100%;
  min-height: 350px;
  height: 350px;
  margin-bottom: 15px;
  border-radius: 12px;
  background: #f8fafc;
  padding: 15px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
  position: relative;
}

.prediction-chart:hover {
  transform: scale(1.01);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.prediction-stats {
  margin-top: 0;
}

.stat-card {
  text-align: center;
  padding: 15px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  border-color: #38bdf8;
}

.stat-title {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 12px;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-value {
  transform: scale(1.05);
  color: #38bdf8;
}

.stat-time {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

.stat-confidence {
  font-size: 13px;
  color: #67c23a;
  font-weight: 500;
}

.ai-analysis {
  margin-top: 40px;
}

.analysis-divider,
.results-divider {
  margin: 20px 0;
}

.divider-text {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-left: 8px;
}

.analysis-card {
  padding: 25px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.analysis-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-color: #38bdf8;
}

.analysis-content :deep(h1), 
.analysis-content :deep(h2), 
.analysis-content :deep(h3) {
  margin-top: 16px;
  margin-bottom: 8px;
  color: #1e293b;
}

.analysis-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
}

.analysis-content :deep(th), 
.analysis-content :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 8px;
  text-align: left;
}

.analysis-content strong {
  color: #1e293b;
  font-weight: 600;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 600px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 25px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.message {
  display: flex;
  margin-bottom: 20px;
  animation: messageIn 0.3s ease-in-out;
  align-items: flex-start;
}

@keyframes messageIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}

.user-avatar,
.ai-avatar {
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-avatar:hover,
.ai-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-message {
  flex-direction: row-reverse;
}

.user-message .message-avatar {
  margin-right: 0;
  margin-left: 12px;
}

.message-content {
  max-width: 75%;
  padding: 15px 20px;
  border-radius: 16px;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.user-message .message-content {
  background: linear-gradient(135deg, #409eff 0%, #38bdf8 100%);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-message .message-content {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border: 1px solid #e2e8f0;
  border-bottom-left-radius: 4px;
}

.message-content:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.message-text :deep(p) {
  margin: 0 0 8px 0;
}

.message-text :deep(ul), .message-text :deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}

.message-text :deep(code) {
  background-color: rgba(0, 0, 0, 0.06);
  padding: 2px 4px;
  border-radius: 4px;
  font-family: monospace;
}

.message-text.loading {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #64748b;
}

.chat-input {
  padding: 20px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}

.chat-input-field {
  border-radius: 25px !important;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
}

.chat-input-field:hover {
  border-color: #38bdf8;
  box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.1);
}

.send-btn {
  border-radius: 50% !important;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.send-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.quick-actions {
  display: flex;
  gap: 12px;
  padding: 15px 20px;
  border-top: 1px solid #e2e8f0;
  background: #f1f5f9;
  flex-wrap: wrap;
}

.quick-btn {
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #e2e8f0;
}

.quick-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-color: #38bdf8;
  color: #38bdf8;
}

.ai-employee-content {
  padding: 20px;
}

.monitoring-controls {
  margin-bottom: 30px;
}

.controls-form {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.control-item {
  margin-right: 0 !important;
  margin-bottom: 0 !important;
  flex: 1;
  min-width: 200px;
}

.control-select {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
}

.control-select:hover {
  border-color: #38bdf8;
  box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.1);
}

.fault-switch {
  margin-top: 5px;
}

.monitoring-status {
  margin-bottom: 30px;
}

.status-alert {
  border-radius: 8px;
  margin-bottom: 15px;
}

.status-info {
  display: flex;
  gap: 20px;
  margin-top: 15px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  flex-wrap: wrap;
}

.status-item {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
  padding: 8px 12px;
  background: #f1f5f9;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.status-item:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
}

.monitoring-results {
  margin-top: 30px;
}

.logs-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.status-tag {
  border-radius: 20px;
  font-weight: 500;
  padding: 2px 12px;
}

.monitoring-empty {
  padding: 60px 0;
  text-align: center;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-prediction {
    margin: 20px 0;
  }

  .ai-card {
    margin-bottom: 20px;
  }

  .card-header {
    padding: 15px;
  }

  .header-title {
    font-size: 16px;
  }

  .prediction-content {
    padding: 15px;
  }

  .params-form,
  .controls-form {
    flex-direction: column;
    padding: 15px;
  }

  .param-item,
  .control-item {
    width: 100%;
  }

  .prediction-chart {
    height: 350px;
    padding: 15px;
  }

  .chat-container {
    height: 500px;
  }

  .chat-messages {
    padding: 15px;
  }

  .message-content {
    max-width: 85%;
    padding: 12px 16px;
  }

  .quick-actions {
    justify-content: center;
  }

  .status-info {
    flex-direction: column;
    gap: 10px;
  }

  .status-item {
    width: 100%;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .prediction-chart {
    height: 300px;
  }

  .chat-container {
    height: 400px;
  }

  .header-buttons {
    flex-direction: column;
    gap: 8px;
  }

  .header-buttons .el-button {
    width: 100%;
  }
}
</style>
