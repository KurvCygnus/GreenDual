<template>
  <div class="environment-chart">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>环境数据趋势</span>
          <el-select
            v-model="chartType"
            size="small"
            @change="handleChartTypeChange"
          >
            <el-option label="温度" value="temperature" />
            <el-option label="湿度" value="humidity" />
            <el-option label="光照" value="light" />
            <el-option label="全部" value="all" />
          </el-select>
        </div>
      </template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated, watch, computed } from "vue";
import * as echarts from "echarts";

const chartRef = ref(null);
const chart = ref(null);
const chartType = ref("all");

// 模拟历史数据
const historicalData = computed(() => {
  const data = [];
  const now = new Date();

  for (let i = 23; i >= 0; i--) {
    const time = new Date(now);
    time.setHours(now.getHours() - i);
    const timeStr = time.getHours().toString().padStart(2, "0") + ":00";

    data.push({
      time: timeStr,
      temperature: 20 + Math.random() * 10,
      humidity: 50 + Math.random() * 30,
      light: 5000 + Math.random() * 15000,
    });
  }

  return data;
});

const handleChartTypeChange = () => {
  initChart();
};

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

onMounted(() => {
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
  margin: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 400px;
}
</style>
