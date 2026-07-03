<template>
  <div class="ecology-engine">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon green">
            <el-icon size="28" color="#fff"><FirstAidKit /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.greenCount }}</div>
            <div class="stat-label">绿植区域</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon blue">
            <el-icon size="28" color="#fff"><Odometer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.onlineCount }}</div>
            <div class="stat-label">监测设备</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon orange">
            <el-icon size="28" color="#fff"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.warningCount }}</div>
            <div class="stat-label">健康预警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon cyan">
            <el-icon size="28" color="#fff"><Pouring /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.irrigationCount }}</div>
            <div class="stat-label">今日灌溉</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 根据子功能显示不同内容 -->
    <template v-if="activeSubMenu === '2-1'">
      <!-- 环境监测图表 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="card-header">
                <span>温度湿度趋势</span>
                <el-select
                  v-model="selectedArea"
                  size="small"
                  style="width: 150px"
                  @change="updateCharts"
                >
                  <el-option
                    v-for="area in greenAreas"
                    :key="area.id"
                    :label="area.name"
                    :value="area.id"
                  />
                </el-select>
              </div>
            </template>
            <div ref="tempHumidityChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="card-header">
                <span>光照强度变化</span>
              </div>
            </template>
            <div ref="lightChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :span="8">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="card-header">
                <span>土壤温度分布</span>
              </div>
            </template>
            <div ref="soilTempChartRef" class="chart-container-small"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="card-header">
                <span>土壤湿度分布</span>
              </div>
            </template>
            <div ref="soilHumidityChartRef" class="chart-container-small"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="card-header">
                <span>区域健康状态</span>
              </div>
            </template>
            <div ref="healthPieChartRef" class="chart-container-small"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 环境监测数据卡片 -->
      <el-card shadow="hover" class="content-card">
        <template #header>
          <div class="card-header">
            <span>实时环境监测</span>
            <el-radio-group v-model="envTimeRange" size="small">
              <el-radio-button label="realtime">实时</el-radio-button>
              <el-radio-button label="hour">1小时</el-radio-button>
              <el-radio-button label="day">24小时</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :span="8" v-for="area in greenAreas" :key="area.id">
            <div
              class="env-area-card"
              :class="{ 'warning-card': area.status === 'warning' }"
            >
              <div class="area-header">
                <span class="area-name">{{ area.name }}</span>
                <el-tag
                  :type="area.status === 'normal' ? 'success' : 'warning'"
                  size="small"
                >
                  {{ area.status === "normal" ? "正常" : "异常" }}
                </el-tag>
              </div>
              <el-row :gutter="10" class="env-metrics">
                <el-col :span="8">
                  <div class="metric-item">
                    <div class="metric-icon temp">
                      <el-icon><Sunny /></el-icon>
                    </div>
                    <div class="metric-info">
                      <div class="metric-value">{{ area.temperature }}°C</div>
                      <div class="metric-label">温度</div>
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="metric-item">
                    <div class="metric-icon humidity">
                      <el-icon><Pouring /></el-icon>
                    </div>
                    <div class="metric-info">
                      <div class="metric-value">{{ area.humidity }}%</div>
                      <div class="metric-label">湿度</div>
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="metric-item">
                    <div class="metric-icon light">
                      <el-icon><Sunny /></el-icon>
                    </div>
                    <div class="metric-info">
                      <div class="metric-value">{{ area.light }}lux</div>
                      <div class="metric-label">光照</div>
                    </div>
                  </div>
                </el-col>
              </el-row>
              <div class="env-soil">
                <div class="soil-item">
                  <span class="soil-label">土壤温度</span>
                  <el-progress
                    :percentage="area.soilTemp"
                    :color="soilTempColor"
                  />
                </div>
                <div class="soil-item">
                  <span class="soil-label">土壤湿度</span>
                  <el-progress
                    :percentage="area.soilHumidity"
                    :color="soilHumidityColor"
                  />
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </template>

    <template v-if="activeSubMenu === '2-2'">
      <!-- 绿植管理 -->
      <el-card shadow="hover" class="content-card">
        <template #header>
          <div class="card-header">
            <span>绿植档案管理</span>
            <el-button type="primary" size="small" @click="showAddPlant = true">
              <el-icon><Plus /></el-icon>添加绿植
            </el-button>
          </div>
        </template>
        <el-table :data="plantList" style="width: 100%">
          <el-table-column prop="name" label="植物名称" width="150">
            <template #default="scope">
              <div class="plant-name">
                <el-avatar
                  :size="32"
                  :src="scope.row.image"
                  class="plant-avatar"
                >
                  <el-icon><FirstAidKit /></el-icon>
                </el-avatar>
                <span>{{ scope.row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="area" label="所在区域" width="120" />
          <el-table-column prop="type" label="植物类型" width="100">
            <template #default="scope">
              <el-tag size="small">{{ scope.row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="health" label="健康状态" width="100">
            <template #default="scope">
              <el-tag
                :type="
                  scope.row.health === '良好'
                    ? 'success'
                    : scope.row.health === '一般'
                      ? 'warning'
                      : 'danger'
                "
                size="small"
              >
                {{ scope.row.health }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastIrrigation" label="上次灌溉" width="120" />
          <el-table-column
            prop="nextIrrigation"
            label="预计下次灌溉"
            width="120"
          />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                text
                @click="viewPlant(scope.row)"
                >详情</el-button
              >
              <el-button
                type="primary"
                size="small"
                text
                @click="irrigatePlant(scope.row)"
                >浇水</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <template v-if="activeSubMenu === '2-3'">
      <!-- 灌溉控制 -->
      <el-card shadow="hover" class="content-card">
        <template #header>
          <div class="card-header">
            <span>智能灌溉控制</span>
            <el-button type="success" size="small" @click="batchIrrigate">
              <el-icon><Pouring /></el-icon>一键灌溉
            </el-button>
          </div>
        </template>
        <div class="irrigation-list">
          <div
            v-for="item in irrigationList"
            :key="item.id"
            class="irrigation-item"
          >
            <div class="irrigation-info">
              <div class="irrigation-name">{{ item.name }}</div>
              <div class="irrigation-status">
                <el-tag
                  :type="
                    item.status === '待灌溉'
                      ? 'warning'
                      : item.status === '灌溉中'
                        ? 'primary'
                        : 'success'
                  "
                  size="small"
                >
                  {{ item.status }}
                </el-tag>
                <span class="irrigation-soil"
                  >土壤湿度: {{ item.soilHumidity }}%</span
                >
                <span class="irrigation-time">{{ item.suggestedTime }}</span>
              </div>
            </div>
            <div class="irrigation-actions">
              <el-switch
                v-model="item.autoMode"
                active-text="自动"
                inactive-text="手动"
                size="small"
              />
              <el-button
                :type="item.status === '灌溉中' ? 'danger' : 'primary'"
                size="small"
                @click="toggleIrrigation(item)"
              >
                {{ item.status === "灌溉中" ? "停止" : "开始" }}
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </template>

    <template v-if="activeSubMenu === '2-4'">
      <!-- 健康预警 -->
      <el-card shadow="hover" class="content-card">
        <template #header>
          <div class="card-header">
            <span>植物健康预警</span>
            <el-radio-group v-model="warningFilter" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="high">高危</el-radio-button>
              <el-radio-button label="medium">中危</el-radio-button>
              <el-radio-button label="low">低危</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div class="warning-list">
          <div
            v-for="warning in filteredWarnings"
            :key="warning.id"
            class="warning-item"
            :class="warning.level"
          >
            <div class="warning-icon">
              <el-icon size="24" color="#fff">
                <Warning v-if="warning.level === 'high'" />
                <InfoFilled v-else-if="warning.level === 'medium'" />
                <CircleCheck v-else />
              </el-icon>
            </div>
            <div class="warning-content">
              <div class="warning-title">{{ warning.title }}</div>
              <div class="warning-desc">{{ warning.description }}</div>
              <div class="warning-meta">
                <span class="warning-area">{{ warning.area }}</span>
                <span class="warning-time">{{ warning.time }}</span>
              </div>
            </div>
            <div class="warning-actions">
              <el-button
                type="primary"
                size="small"
                text
                @click="handleWarning(warning)"
                >处理</el-button
              >
              <el-button size="small" text @click="ignoreWarning(warning)"
                >忽略</el-button
              >
            </div>
          </div>
        </div>
      </el-card>
    </template>

    <!-- 添加绿植对话框 -->
    <el-dialog v-model="showAddPlant" title="添加绿植" width="500px">
      <el-form :model="newPlant" label-width="100px">
        <el-form-item label="植物名称">
          <el-input v-model="newPlant.name" placeholder="请输入植物名称" />
        </el-form-item>
        <el-form-item label="所在区域">
          <el-select
            v-model="newPlant.area"
            placeholder="请选择区域"
            style="width: 100%"
          >
            <el-option
              v-for="area in greenAreas"
              :key="area.id"
              :label="area.name"
              :value="area.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="植物类型">
          <el-select
            v-model="newPlant.type"
            placeholder="请选择类型"
            style="width: 100%"
          >
            <el-option label="乔木" value="乔木" />
            <el-option label="灌木" value="灌木" />
            <el-option label="花卉" value="花卉" />
            <el-option label="草坪" value="草坪" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="newPlant.remark"
            type="textarea"
            rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddPlant = false">取消</el-button>
        <el-button type="primary" @click="addPlant">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import {
  Refresh,
  FirstAidKit,
  Odometer,
  Warning,
  Pouring,
  Sunny,
  Plus,
  InfoFilled,
  CircleCheck,
} from "@element-plus/icons-vue";

const props = defineProps({
  activeIndex: {
    type: String,
    default: "2-1",
  },
});

const activeSubMenu = computed(() => props.activeIndex);

// 页面标题
const currentPageTitle = computed(() => {
  const titles = {
    "2-1": "环境监测",
    "2-2": "绿植管理",
    "2-3": "灌溉控制",
    "2-4": "健康预警",
  };
  return titles[props.activeIndex] || "生态引擎";
});

const currentPageSubtitle = computed(() => {
  const subtitles = {
    "2-1": "实时监测校园绿植区域的温湿度、光照、土壤等环境参数",
    "2-2": "管理校园绿植档案，跟踪植物生长状态",
    "2-3": "智能控制灌溉系统，支持自动/手动模式",
    "2-4": "监测植物健康状况，及时发现病虫害和环境异常",
  };
  return subtitles[props.activeIndex] || "校园绿植养护管理系统";
});

// 统计数据
const stats = ref({
  greenCount: 6,
  onlineCount: 18,
  warningCount: 2,
  irrigationCount: 12,
});

// 环境监测
const envTimeRange = ref("realtime");
const greenAreas = ref([
  {
    id: 1,
    name: "图书馆绿植区",
    status: "normal",
    temperature: 24,
    humidity: 65,
    light: 850,
    soilTemp: 22,
    soilHumidity: 58,
  },
  {
    id: 2,
    name: "教学楼A区花园",
    status: "normal",
    temperature: 26,
    humidity: 70,
    light: 1200,
    soilTemp: 23,
    soilHumidity: 45,
  },
  {
    id: 3,
    name: "行政楼景观带",
    status: "warning",
    temperature: 25,
    humidity: 62,
    light: 680,
    soilTemp: 21,
    soilHumidity: 72,
  },
  {
    id: 4,
    name: "学生宿舍绿化带",
    status: "normal",
    temperature: 23,
    humidity: 68,
    light: 920,
    soilTemp: 20,
    soilHumidity: 55,
  },
  {
    id: 5,
    name: "体育馆周边绿植",
    status: "warning",
    temperature: 28,
    humidity: 55,
    light: 1500,
    soilTemp: 25,
    soilHumidity: 38,
  },
  {
    id: 6,
    name: "食堂景观植物",
    status: "normal",
    temperature: 27,
    humidity: 75,
    light: 450,
    soilTemp: 24,
    soilHumidity: 80,
  },
]);

const soilTempColor = [{ color: "#67C23A", percentage: 100 }];
const soilHumidityColor = [{ color: "#409EFF", percentage: 100 }];

// 绿植管理
const showAddPlant = ref(false);
const plantList = ref([
  {
    id: 1,
    name: "银杏树",
    area: "图书馆绿植区",
    type: "乔木",
    health: "良好",
    lastIrrigation: "2天前",
    nextIrrigation: "3天后",
    image: "",
  },
  {
    id: 2,
    name: "月季花",
    area: "教学楼A区花园",
    type: "花卉",
    health: "良好",
    lastIrrigation: "1天前",
    nextIrrigation: "2天后",
    image: "",
  },
  {
    id: 3,
    name: "冬青",
    area: "行政楼景观带",
    type: "灌木",
    health: "一般",
    lastIrrigation: "3天前",
    nextIrrigation: "1天后",
    image: "",
  },
  {
    id: 4,
    name: "草坪",
    area: "学生宿舍绿化带",
    type: "草坪",
    health: "良好",
    lastIrrigation: "1天前",
    nextIrrigation: "2天后",
    image: "",
  },
  {
    id: 5,
    name: "桂花树",
    area: "体育馆周边绿植",
    type: "乔木",
    health: "需关注",
    lastIrrigation: "5天前",
    nextIrrigation: "今天",
    image: "",
  },
  {
    id: 6,
    name: "绿萝",
    area: "食堂景观植物",
    type: "花卉",
    health: "良好",
    lastIrrigation: "1天前",
    nextIrrigation: "3天后",
    image: "",
  },
]);

const newPlant = ref({ name: "", area: "", type: "", remark: "" });

const addPlant = () => {
  if (!newPlant.value.name) {
    ElMessage.warning("请输入植物名称");
    return;
  }
  plantList.value.push({
    id: Date.now(),
    name: newPlant.value.name,
    area: newPlant.value.area,
    type: newPlant.value.type,
    health: "良好",
    lastIrrigation: "今天",
    nextIrrigation: "3天后",
    image: "",
  });
  showAddPlant.value = false;
  ElMessage.success("添加成功");
  newPlant.value = { name: "", area: "", type: "", remark: "" };
};

const viewPlant = (plant) => {
  ElMessage.info(`查看植物：${plant.name}`);
};

const irrigatePlant = (plant) => {
  ElMessage.success(`已为 ${plant.name} 启动灌溉`);
};

// 灌溉控制
const irrigationList = ref([
  {
    id: 1,
    name: "图书馆绿植区",
    status: "待灌溉",
    soilHumidity: 45,
    suggestedTime: "建议14:00",
    autoMode: true,
  },
  {
    id: 2,
    name: "教学楼A区花园",
    status: "灌溉中",
    soilHumidity: 62,
    suggestedTime: "预计15分钟",
    autoMode: true,
  },
  {
    id: 3,
    name: "行政楼景观带",
    status: "已完成",
    soilHumidity: 78,
    suggestedTime: "已完成",
    autoMode: false,
  },
  {
    id: 4,
    name: "学生宿舍绿化带",
    status: "待灌溉",
    soilHumidity: 38,
    suggestedTime: "建议16:00",
    autoMode: true,
  },
  {
    id: 5,
    name: "体育馆周边绿植",
    status: "灌溉中",
    soilHumidity: 35,
    suggestedTime: "预计30分钟",
    autoMode: false,
  },
  {
    id: 6,
    name: "食堂景观植物",
    status: "已完成",
    soilHumidity: 82,
    suggestedTime: "已完成",
    autoMode: true,
  },
]);

const batchIrrigate = () => {
  ElMessage.success("已启动一键灌溉，系统将按顺序执行");
};

const toggleIrrigation = (item) => {
  if (item.status === "灌溉中") {
    item.status = "待灌溉";
    ElMessage.info(`已停止 ${item.name} 的灌溉`);
  } else {
    item.status = "灌溉中";
    ElMessage.success(`已开始 ${item.name} 的灌溉`);
  }
};

// 健康预警
const warningFilter = ref("all");
const warnings = ref([
  {
    id: 1,
    title: "体育馆周边绿植土壤湿度过低",
    description: "土壤湿度仅38%，建议立即灌溉",
    area: "体育馆周边绿植",
    time: "10分钟前",
    level: "high",
  },
  {
    id: 2,
    title: "桂花树叶片出现黄化",
    description: "可能缺氮或浇水过多，建议检查根系",
    area: "体育馆周边绿植",
    time: "2小时前",
    level: "medium",
  },
  {
    id: 3,
    title: "行政楼景观带光照不足",
    description: "连续3天光照低于500lux，建议补光",
    area: "行政楼景观带",
    time: "5小时前",
    level: "medium",
  },
  {
    id: 4,
    title: "食堂景观植物土壤湿度过高",
    description: "土壤湿度82%，建议暂停灌溉",
    area: "食堂景观植物",
    time: "1天前",
    level: "low",
  },
]);

const filteredWarnings = computed(() => {
  if (warningFilter.value === "all") return warnings.value;
  return warnings.value.filter((w) => w.level === warningFilter.value);
});

const handleWarning = (warning) => {
  ElMessage.success(`已处理预警：${warning.title}`);
};

const ignoreWarning = (warning) => {
  ElMessage.info(`已忽略预警：${warning.title}`);
};

const refreshData = () => {
  ElMessage.success("数据已刷新");
  updateCharts();
};

// 图表相关
const tempHumidityChartRef = ref(null);
const lightChartRef = ref(null);
const soilTempChartRef = ref(null);
const soilHumidityChartRef = ref(null);
const healthPieChartRef = ref(null);
const selectedArea = ref(1);

let tempHumidityChart = null;
let lightChart = null;
let soilTempChart = null;
let soilHumidityChart = null;
let healthPieChart = null;

const generateChartData = () => {
  const hours = [];
  const temps = [];
  const humiditys = [];
  const lights = [];
  const now = new Date();
  for (let i = 11; i >= 0; i--) {
    const time = new Date(now - i * 3600000);
    hours.push(time.getHours().toString().padStart(2, "0") + ":00");
    temps.push(Math.round(20 + Math.random() * 10));
    humiditys.push(Math.round(50 + Math.random() * 30));
    lights.push(Math.round(500 + Math.random() * 1000));
  }
  return { hours, temps, humiditys, lights };
};

const initCharts = () => {
  if (!tempHumidityChartRef.value) return;

  const data = generateChartData();

  tempHumidityChart = echarts.init(tempHumidityChartRef.value);
  tempHumidityChart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["温度", "湿度"], bottom: 0 },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "15%",
      top: "10%",
      containLabel: true,
    },
    xAxis: { type: "category", data: data.hours, boundaryGap: false },
    yAxis: [
      { type: "value", name: "温度(°C)", position: "left" },
      { type: "value", name: "湿度(%)", position: "right", max: 100 },
    ],
    series: [
      {
        name: "温度",
        type: "line",
        smooth: true,
        data: data.temps,
        itemStyle: { color: "#F56C6C" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(245, 108, 108, 0.3)" },
            { offset: 1, color: "rgba(245, 108, 108, 0.05)" },
          ]),
        },
      },
      {
        name: "湿度",
        type: "line",
        smooth: true,
        yAxisIndex: 1,
        data: data.humiditys,
        itemStyle: { color: "#409EFF" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(64, 158, 255, 0.3)" },
            { offset: 1, color: "rgba(64, 158, 255, 0.05)" },
          ]),
        },
      },
    ],
  });

  lightChart = echarts.init(lightChartRef.value);
  lightChart.setOption({
    tooltip: { trigger: "axis" },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      top: "10%",
      containLabel: true,
    },
    xAxis: { type: "category", data: data.hours, boundaryGap: false },
    yAxis: { type: "value", name: "光照(lux)" },
    series: [
      {
        type: "line",
        smooth: true,
        data: data.lights,
        itemStyle: { color: "#E6A23C" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(230, 162, 60, 0.3)" },
            { offset: 1, color: "rgba(230, 162, 60, 0.05)" },
          ]),
        },
      },
    ],
  });

  soilTempChart = echarts.init(soilTempChartRef.value);
  soilTempChart.setOption({
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "15%",
      top: "10%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: greenAreas.value.map((a) => a.name.slice(0, 4)),
      axisLabel: { interval: 0, rotate: 30 },
    },
    yAxis: { type: "value", name: "温度(°C)" },
    series: [
      {
        type: "bar",
        data: greenAreas.value.map((a) => a.soilTemp),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#67C23A" },
            { offset: 1, color: "#95D475" },
          ]),
        },
      },
    ],
  });

  soilHumidityChart = echarts.init(soilHumidityChartRef.value);
  soilHumidityChart.setOption({
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "15%",
      top: "10%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: greenAreas.value.map((a) => a.name.slice(0, 4)),
      axisLabel: { interval: 0, rotate: 30 },
    },
    yAxis: { type: "value", name: "湿度(%)", max: 100 },
    series: [
      {
        type: "bar",
        data: greenAreas.value.map((a) => a.soilHumidity),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#409EFF" },
            { offset: 1, color: "#79BBFF" },
          ]),
        },
      },
    ],
  });

  healthPieChart = echarts.init(healthPieChartRef.value);
  const normalCount = greenAreas.value.filter(
    (a) => a.status === "normal",
  ).length;
  const warningCount = greenAreas.value.filter(
    (a) => a.status === "warning",
  ).length;
  healthPieChart.setOption({
    tooltip: { trigger: "item" },
    legend: { bottom: 0, left: "center" },
    series: [
      {
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: "#fff", borderWidth: 2 },
        label: { show: false, position: "center" },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: "bold" } },
        labelLine: { show: false },
        data: [
          { value: normalCount, name: "正常", itemStyle: { color: "#67C23A" } },
          {
            value: warningCount,
            name: "异常",
            itemStyle: { color: "#E6A23C" },
          },
        ],
      },
    ],
  });
};

const updateCharts = () => {
  const data = generateChartData();
  if (tempHumidityChart) {
    tempHumidityChart.setOption({
      xAxis: { data: data.hours },
      series: [{ data: data.temps }, { data: data.humiditys }],
    });
  }
  if (lightChart) {
    lightChart.setOption({
      xAxis: { data: data.hours },
      series: [{ data: data.lights }],
    });
  }
};

watch(
  () => props.activeIndex,
  (newVal) => {
    if (newVal.startsWith("2-1")) {
      nextTick(() => {
        initCharts();
      });
    }
  },
  { immediate: true },
);

onMounted(() => {
  if (props.activeIndex.startsWith("2-1")) {
    nextTick(() => {
      initCharts();
    });
  }
  window.addEventListener("resize", () => {
    tempHumidityChart?.resize();
    lightChart?.resize();
    soilTempChart?.resize();
    soilHumidityChart?.resize();
    healthPieChart?.resize();
  });
});

onUnmounted(() => {
  tempHumidityChart?.dispose();
  lightChart?.dispose();
  soilTempChart?.dispose();
  soilHumidityChart?.dispose();
  healthPieChart?.dispose();
  window.removeEventListener("resize", () => {});
});
</script>

<style scoped>
.ecology-engine {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon.green {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}
.stat-icon.blue {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}
.stat-icon.orange {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}
.stat-icon.cyan {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.content-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

/* 环境监测样式 */
.env-area-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.area-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.env-metrics {
  margin-bottom: 12px;
}

.metric-item {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 10px;
  border-radius: 6px;
}

.metric-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}

.metric-icon.temp {
  background: #fef0f0;
  color: #f56c6c;
}
.metric-icon.humidity {
  background: #f0f9ff;
  color: #409eff;
}
.metric-icon.light {
  background: #fff7e6;
  color: #e6a23c;
}

.metric-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.metric-label {
  font-size: 12px;
  color: #909399;
}

.env-soil {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.soil-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.soil-label {
  font-size: 13px;
  color: #606266;
  width: 70px;
  flex-shrink: 0;
}

/* 绿植管理样式 */
.plant-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.plant-avatar {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

/* 灌溉控制样式 */
.irrigation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.irrigation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.irrigation-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.irrigation-status {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #606266;
}

.irrigation-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 健康预警样式 */
.warning-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.warning-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  gap: 12px;
}

.warning-item.high {
  background: #fef0f0;
}
.warning-item.medium {
  background: #fdf6ec;
}
.warning-item.low {
  background: #f4f4f5;
}

.warning-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.warning-item.high .warning-icon {
  background: #f56c6c;
}
.warning-item.medium .warning-icon {
  background: #e6a23c;
}
.warning-item.low .warning-icon {
  background: #909399;
}

.warning-content {
  flex: 1;
}

.warning-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.warning-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}

.warning-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.warning-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 100%;
}

.chart-container {
  width: 100%;
  height: 280px;
}

.chart-container-small {
  width: 100%;
  height: 220px;
}

.warning-card {
  border: 1px solid #e6a23c;
  background: #fdf6ec;
}
</style>
