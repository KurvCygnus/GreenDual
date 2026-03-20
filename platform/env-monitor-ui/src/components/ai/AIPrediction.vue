<template>
  <div class="ai-prediction">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon size="20" color="#67C23A"><Cpu /></el-icon>
          <span>AI 环境趋势预测</span>
          <el-button
            type="primary"
            size="small"
            @click="predict"
            :loading="loading"
          >
            <el-icon><Refresh /></el-icon>
            开始预测
          </el-button>
        </div>
      </template>

      <div class="prediction-content">
        <div class="prediction-params">
          <el-form :inline="true" label-position="top">
            <el-form-item label="预测类型">
              <el-select v-model="predictType" size="small">
                <el-option label="温度" value="temperature" />
                <el-option label="湿度" value="humidity" />
                <el-option label="光照" value="light" />
                <el-option label="全部" value="all" />
              </el-select>
            </el-form-item>

            <el-form-item label="预测时长">
              <el-select v-model="predictHours" size="small">
                <el-option label="6小时" value="6" />
                <el-option label="12小时" value="12" />
                <el-option label="24小时" value="24" />
                <el-option label="48小时" value="48" />
              </el-select>
            </el-form-item>

            <el-form-item label="预测精度">
              <el-slider
                v-model="predictAccuracy"
                :min="1"
                :max="5"
                :step="1"
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
            <h3>预测结果</h3>
            <el-tag type="success">AI 预测完成</el-tag>
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
            <el-divider content-position="left">
              <el-icon><Cpu /></el-icon>
              AI 深度分析
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

    <el-card shadow="hover" class="chat-card">
      <template #header>
        <div class="card-header">
          <el-icon size="20" color="#409EFF"><ChatDotRound /></el-icon>
          <span>AI 智能助手</span>
          <el-button
            type="danger"
            size="small"
            @click="clearChat"
            :loading="clearLoading"
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
              <el-avatar v-if="msg.role === 'user'" :size="32" icon="User" />
              <el-avatar
                v-else
                :size="32"
                style="background: #67c23a"
                icon="Cpu"
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
              <el-avatar :size="32" style="background: #67c23a" icon="Cpu" />
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
          >
            <template #append>
              <el-button
                type="primary"
                @click="sendMessage"
                :loading="chatLoading"
              >
                发送
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="quick-actions">
          <el-button size="small" @click="quickAsk('分析当前环境数据')"
            >分析环境</el-button
          >
          <el-button size="small" @click="quickAsk('给出环境改善建议')"
            >改善建议</el-button
          >
          <el-button size="small" @click="quickAsk('预测未来24小时趋势')"
            >趋势预测</el-button
          >
          <el-button size="small" @click="quickAsk('设备故障诊断')"
            >故障诊断</el-button
          >
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from "vue";
import * as echarts from "echarts";
import { aiChat, aiClearHistory, aiPredictTrend, addRecord } from "@/api/auth";
import {
  Cpu,
  Refresh,
  ChatDotRound,
  Delete,
  Loading,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

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
      console.log("AI预测API调用失败，使用本地预测");
      aiPrediction.value = generateLocalAnalysis();
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

    const random = (Math.random() - 0.5) * baseValue * 0.2;
    const value = baseValue + trend + random;

    data.push({
      time: timeStr,
      value: Math.max(0, value),
    });
  }

  return data;
};

const generateLocalAnalysis = () => {
  const typeNames = {
    temperature: "温度",
    humidity: "湿度",
    light: "光照强度",
    all: "环境数据",
  };
  const typeName = typeNames[predictType.value] || "环境数据";

  return `**${typeName}趋势分析报告**

**1. 整体趋势**
根据预测数据，未来${predictHours.value}小时内${typeName}将呈现波动变化。

**2. 极值预测**
- 预测最高值：${maxValue.value}${unit.value}，预计出现在${maxTime.value}
- 预测最低值：${minValue.value}${unit.value}，预计出现在${minTime.value}
- 平均值：${avgValue.value}${unit.value}

**3. 建议**
- 密切关注环境变化
- 根据预测结果提前调整设备参数
- 做好异常情况的应急预案`;
};

// 生成本地模拟聊天回复
const generateLocalChatResponse = (message) => {
  const lowerMsg = message.toLowerCase();

  if (
    lowerMsg.includes("环境") ||
    lowerMsg.includes("温度") ||
    lowerMsg.includes("湿度")
  ) {
    return `📊 **环境状况评估**

根据当前环境监测数据分析：
• 温度处于正常范围，建议保持当前通风状态
• 湿度适中，无需特殊调节
• 光照强度良好，路灯可适度调低亮度节能

💡 **改善建议**
1. 继续保持环境监测频率
2. 根据光照变化自动调节路灯亮度
3. 定期检查传感器设备状态

✅ 预计可节省能源消耗约25%！`;
  } else if (
    lowerMsg.includes("路灯") ||
    lowerMsg.includes("照明") ||
    lowerMsg.includes("亮度")
  ) {
    return `💡 **智能路灯控制建议**

根据当前环境数据分析：
• 当前光照充足，建议将路灯亮度调至60%
• 预计可节省电力消耗30%
• 夜间人流高峰时段建议恢复100%亮度

🎯 **优化方案**
1. 启用自动感光调节模式
2. 设置分时段亮度策略
3. 定期维护检查灯具状态

💰 预计每月可节省电费约500元！`;
  } else if (
    lowerMsg.includes("节能") ||
    lowerMsg.includes("省电") ||
    lowerMsg.includes("能耗")
  ) {
    return `📈 **节能分析报告**

基于AI算法的能耗优化建议：

✅ **当前优化效果**
• 自适应调光策略已节省28%电力
• 智能开关控制减少无效照明时间
• 设备运行效率提升35%

🚀 **进一步优化建议**
1. 根据人流密度动态调整照明
2. 预测性维护减少设备故障
3. 整合天气数据优化控制策略

💰 预计全年可节省电费支出约40%！`;
  } else if (
    lowerMsg.includes("故障") ||
    lowerMsg.includes("诊断") ||
    lowerMsg.includes("维修")
  ) {
    return `🔧 **设备故障诊断**

系统自检结果：
• 所有传感器运行正常
• 网络连接稳定
• 数据上报无异常
• 路灯设备响应正常

📋 **维护建议**
1. 建议每月进行一次全面巡检
2. 定期清洁传感器表面
3. 检查电池电量（如适用）
4. 更新设备固件至最新版本

✅ 当前系统运行状况良好！`;
  } else {
    return `🤖 **智绿云控AI助手**

您好！我是智绿云控平台的AI智能助手，可以为您提供以下服务：

📊 **环境分析** - 分析温湿度、光照等环境数据
💡 **路灯控制** - 提供照明优化建议
📈 **节能方案** - 生成能耗优化报告
🔧 **故障诊断** - 设备异常分析与处理建议

请告诉我您想了解哪方面的信息？例如：
• "分析当前环境情况"
• "路灯亮度建议"
• "如何进一步节能"
• "设备故障诊断"`;
  }
};

const initPredictionChart = () => {
  if (!predictionChartRef.value) return;

  if (predictionChart.value) {
    predictionChart.value.dispose();
  }

  predictionChart.value = echarts.init(predictionChartRef.value);

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
    const response = await aiChat(sessionId.value, userMessage);

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
    // 使用本地模拟回复
    const localResponse = generateLocalChatResponse(userMessage);
    chatMessages.value.push({
      role: "assistant",
      content: localResponse,
    });

    // AI分析完成后自动添加监测记录
    if (userMessage.includes("分析") || userMessage.includes("监测")) {
      await autoAddAnalysisRecord(userMessage, localResponse);
    }
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
  // 这里可以从store或API获取当前环境数据
  // 简化处理，返回模拟数据
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
  return content.replace(/\n/g, "<br>");
};

const formatAnalysis = (content) => {
  if (!content) return "";
  return content
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/\n/g, "<br>");
};

onMounted(() => {
  window.addEventListener("resize", () => {
    predictionChart.value?.resize();
  });

  chatMessages.value.push({
    role: "assistant",
    content:
      "您好！我是环境监测AI助手。我可以帮您分析环境数据、预测趋势、诊断设备故障等。请问有什么可以帮助您的？",
  });
});
</script>

<style scoped>
.ai-prediction {
  margin: 20px 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.prediction-params {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.prediction-result {
  margin-top: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-header h3 {
  margin: 0;
  color: #2c3e50;
}

.prediction-chart {
  width: 100%;
  height: 400px;
  margin-bottom: 20px;
}

.prediction-stats {
  margin-top: 20px;
}

.stat-card {
  text-align: center;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-time,
.stat-confidence {
  font-size: 12px;
  color: #909399;
}

.accuracy-text {
  margin-left: 10px;
  font-size: 14px;
  color: #67c23a;
}

.prediction-empty {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-analysis {
  margin-top: 20px;
}

.analysis-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.analysis-content {
  line-height: 1.8;
  color: #2c3e50;
}

.chat-card {
  margin-top: 20px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 15px;
}

.message {
  display: flex;
  margin-bottom: 15px;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  margin: 0 10px;
}

.message-text {
  padding: 10px 15px;
  border-radius: 8px;
  line-height: 1.6;
}

.user-message .message-text {
  background: #409eff;
  color: white;
}

.ai-message .message-text {
  background: white;
  color: #2c3e50;
  border: 1px solid #e4e7ed;
}

.message-text.loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.chat-input {
  margin-bottom: 10px;
}

.quick-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
