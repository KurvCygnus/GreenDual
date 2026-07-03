<template>
  <div class="environment-chart">
    <el-card shadow="hover" class="chart-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon size="20" color="#409EFF"><Odometer /></el-icon>
            <span class="header-title">环境数据趋势</span>
          </div>
          <div class="header-right">
            <el-select
              v-model="chartType"
              size="small"
              @change="handleChartTypeChange"
              class="chart-select"
            >
              <el-option label="温度" value="temperature" />
              <el-option label="湿度" value="humidity" />
              <el-option label="光照" value="light" />
              <el-option label="全部" value="all" />
            </el-select>
            <el-select
              v-model="deviceType"
              size="small"
              @change="handleDeviceTypeChange"
              class="chart-select"
            >
              <el-option
                v-for="item in deviceTypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>
        </div>
      </template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated, watch, computed } from "vue";
import * as echarts from "echarts";
import { getDeviceTypeList } from "@/api/auth";

const props = defineProps({
  deviceType: {
    type: String,
    default: "all",
  },
});

const chartRef = ref(null);
const chart = ref(null);
const chartType = ref("all");
const deviceType = ref(props.deviceType);
const deviceTypeList = ref([{ label: "全部设备", value: "all" }]);

// 模拟历史数据，根据设备类型生成不同的数据
const historicalData = computed(() => {
  const data = [];
  const now = new Date();

  // 根据设备类型设置不同的数据范围
  const getDataRange = () => {
    switch (deviceType.value) {
      case "temperature_humidity":
        return {
          temperature: { min: 20, max: 30 },
          humidity: { min: 45, max: 65 },
          light: { min: 1000, max: 10000 },
        };
      case "light":
        return {
          temperature: { min: 22, max: 26 },
          humidity: { min: 50, max: 60 },
          light: { min: 500, max: 30000 },
        };
      case "soil":
        return {
          temperature: { min: 15, max: 30 },
          humidity: { min: 20, max: 90 },
          light: { min: 1000, max: 15000 },
        };

      case "wind":
        return {
          temperature: { min: 15, max: 35 },
          humidity: { min: 30, max: 80 },
          light: { min: 1000, max: 25000 },
        };
      case "comprehensive":
        return {
          temperature: { min: 18, max: 28 },
          humidity: { min: 40, max: 70 },
          light: { min: 3000, max: 20000 },
        };
      default:
        return {
          temperature: { min: 20, max: 25 },
          humidity: { min: 45, max: 55 },
          light: { min: 3000, max: 18000 },
        };
    }
  };

  const range = getDataRange();

  for (let i = 23; i >= 0; i--) {
    const time = new Date(now);
    time.setHours(now.getHours() - i);
    const timeStr = time.getHours().toString().padStart(2, "0") + ":00";

    data.push({
      time: timeStr,
      temperature:
        range.temperature.min +
        Math.random() * (range.temperature.max - range.temperature.min),
      humidity:
        range.humidity.min +
        Math.random() * (range.humidity.max - range.humidity.min),
      light:
        range.light.min + Math.random() * (range.light.max - range.light.min),
    });
  }

  return data;
});

const handleChartTypeChange = () => {
  initChart();
};

const handleDeviceTypeChange = () => {
  initChart();
};

// 获取设备类型列表
const fetchDeviceTypes = async () => {
  try {
    const response = await getDeviceTypeList();
    if (response.data.code === 1) {
      const types = response.data.data || [];
      deviceTypeList.value = [
        { label: "全部设备", value: "all" },
        ...types.map((type) => ({
          label: type.name,
          value: type.type,
        })),
      ];
    }
  } catch (error) {
    console.error("获取设备类型列表失败", error);
  }
};

// 监听设备类型变化
watch(
  () => props.deviceType,
  (newType) => {
    deviceType.value = newType;
    initChart();
  },
);

const initChart = () => {
  if (!chartRef.value) return;

  if (chart.value) {
    chart.value.dispose();
  }

  chart.value = echarts.init(chartRef.value);

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
    legend: {
      data: getLegendData(),
      top: 50,
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      top: 90,
      containLabel: true,
    },
    xAxis: [
      {
        type: "category",
        boundaryGap: false,
        data: historicalData.value.map((item) => item.time),
      },
    ],
    yAxis: getYAxis(),
    series: getSeriesData(),
  };

  chart.value.setOption(option);
};

const getChartTitle = () => {
  const titles = {
    temperature: "温度变化趋势",
    humidity: "湿度变化趋势",
    light: "光照强度变化趋势",
    all: "环境数据综合趋势",
  };
  return titles[chartType.value] || "环境数据趋势";
};

const getLegendData = () => {
  if (chartType.value === "all") {
    return ["温度", "湿度", "光照"];
  } else if (chartType.value === "temperature") {
    return ["温度"];
  } else if (chartType.value === "humidity") {
    return ["湿度"];
  } else if (chartType.value === "light") {
    return ["光照"];
  }
  return [];
};

const getYAxis = () => {
  if (chartType.value === "all") {
    return [
      {
        type: "value",
        name: "温度 (°C)",
        position: "left",
        axisLabel: {
          formatter: "{value} °C",
        },
      },
      {
        type: "value",
        name: "湿度 (%)",
        position: "right",
        axisLabel: {
          formatter: "{value} %",
        },
      },
      {
        type: "value",
        name: "光照 (lux)",
        position: "right",
        offset: 80,
        axisLabel: {
          formatter: "{value} lux",
        },
      },
    ];
  } else if (chartType.value === "temperature") {
    return [
      {
        type: "value",
        name: "温度 (°C)",
        axisLabel: {
          formatter: "{value} °C",
        },
      },
    ];
  } else if (chartType.value === "humidity") {
    return [
      {
        type: "value",
        name: "湿度 (%)",
        axisLabel: {
          formatter: "{value} %",
        },
      },
    ];
  } else if (chartType.value === "light") {
    return [
      {
        type: "value",
        name: "光照 (lux)",
        axisLabel: {
          formatter: "{value} lux",
        },
      },
    ];
  }
  return [];
};

const getSeriesData = () => {
  const series = [];

  if (chartType.value === "all" || chartType.value === "temperature") {
    series.push({
      name: "温度",
      type: "line",
      data: historicalData.value.map((item) => item.temperature.toFixed(1)),
      smooth: true,
      lineStyle: {
        color: "#F56C6C",
      },
      itemStyle: {
        color: "#F56C6C",
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: "rgba(245, 108, 108, 0.3)",
          },
          {
            offset: 1,
            color: "rgba(245, 108, 108, 0.1)",
          },
        ]),
      },
    });
  }

  if (chartType.value === "all" || chartType.value === "humidity") {
    series.push({
      name: "湿度",
      type: "line",
      yAxisIndex: chartType.value === "all" ? 1 : 0,
      data: historicalData.value.map((item) => item.humidity),
      smooth: true,
      lineStyle: {
        color: "#409EFF",
      },
      itemStyle: {
        color: "#409EFF",
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: "rgba(64, 158, 255, 0.3)",
          },
          {
            offset: 1,
            color: "rgba(64, 158, 255, 0.1)",
          },
        ]),
      },
    });
  }

  if (chartType.value === "all" || chartType.value === "light") {
    series.push({
      name: "光照",
      type: "line",
      yAxisIndex: chartType.value === "all" ? 2 : 0,
      data: historicalData.value.map((item) => item.light),
      smooth: true,
      lineStyle: {
        color: "#E6A23C",
      },
      itemStyle: {
        color: "#E6A23C",
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: "rgba(230, 162, 60, 0.3)",
          },
          {
            offset: 1,
            color: "rgba(230, 162, 60, 0.1)",
          },
        ]),
      },
    });
  }

  return series;
};

onMounted(async () => {
  await fetchDeviceTypes();
  initChart();

  window.addEventListener("resize", () => {
    chart.value?.resize();
  });
});

onActivated(() => {
  if (chart.value) {
    chart.value.resize();
  }
});
</script>

<style scoped>
.environment-chart {
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

.chart-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.chart-card:hover {
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

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-select {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
}

.chart-select:hover {
  border-color: #38bdf8;
  box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.1);
}

.chart-container {
  width: 100%;
  height: 450px;
  padding: 20px;
  transition: all 0.3s ease;
}

.chart-container:hover {
  transform: scale(1.01);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .environment-chart {
    margin: 20px 0;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 15px;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .chart-select {
    flex: 1;
  }

  .chart-container {
    height: 350px;
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .chart-container {
    height: 300px;
  }

  .header-title {
    font-size: 16px;
  }
}
</style>
