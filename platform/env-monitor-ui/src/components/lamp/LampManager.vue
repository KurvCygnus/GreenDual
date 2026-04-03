<template>
  <div class="lamp-manager">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #409eff">
            <el-icon size="28" color="#fff"><Sunny /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalCount }}</div>
            <div class="stat-label">路灯总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon size="28" color="#fff"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.onlineCount }}</div>
            <div class="stat-label">在线</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon size="28" color="#fff"><Sunny /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.onCount }}</div>
            <div class="stat-label">开启</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #909399">
            <el-icon size="28" color="#fff"><Moon /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.offCount }}</div>
            <div class="stat-label">关闭</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #f56c6c">
            <el-icon size="28" color="#fff"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.faultCount }}</div>
            <div class="stat-label">故障</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #8e44ad">
            <el-icon size="28" color="#fff"><Lightning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalPower }}W</div>
            <div class="stat-label">总功耗</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 路灯监控 -->
    <el-card v-if="currentView === '1-1'" class="operation-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>路灯监控</span>
          <div class="header-actions">
            <el-button type="success" :icon="SwitchButton" @click="batchTurnOn">
              一键开灯
            </el-button>
            <el-button type="warning" :icon="CircleClose" @click="batchTurnOff">
              一键关灯
            </el-button>
            <el-button
              type="primary"
              :icon="MagicStick"
              @click="showSmartAdjust"
            >
              智能调光
            </el-button>
            <el-button type="danger" :icon="Warning" @click="showFaultList">
              故障告警
            </el-button>
          </div>
        </div>
      </template>

      <!-- 路灯列表 -->
      <el-table :data="lampList" style="width: 100%" v-loading="loading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="设备ID" width="120" />
        <el-table-column prop="name" label="路灯名称" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column label="在线状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.onLine ? 'success' : 'info'">
              {{ scope.row.onLine ? "在线" : "离线" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开关状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.lampStatus"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleLampSwitch(scope.row, val)"
              :disabled="!scope.row.onLine"
            />
          </template>
        </el-table-column>
        <el-table-column label="亮度" width="200">
          <template #default="scope">
            <el-slider
              v-model="scope.row.brightness"
              :max="100"
              :disabled="!scope.row.onLine || scope.row.lampStatus === 0"
              @change="(val) => handleBrightnessChange(scope.row, val)"
              show-stops
            />
          </template>
        </el-table-column>
        <el-table-column prop="powerConsumption" label="功耗(W)" width="100" />
        <el-table-column label="故障状态" width="100">
          <template #default="scope">
            <el-tag
              :type="
                scope.row.faultStatus || scope.row.isBroken
                  ? 'danger'
                  : 'success'
              "
            >
              {{
                scope.row.faultStatus || scope.row.isBroken ? "故障" : "正常"
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="showDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="smartAdjust(scope.row)"
            >
              智能调光
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="showLocation(scope.row)"
            >
              位置
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 智能调光 -->
    <el-card
      v-else-if="currentView === '1-2'"
      class="operation-card"
      shadow="never"
    >
      <template #header>
        <div class="card-header">
          <span>智能调光</span>
          <div class="header-actions">
            <el-button
              type="primary"
              :icon="MagicStick"
              @click="showSmartAdjust"
            >
              批量智能调光
            </el-button>
            <el-button type="success" @click="optimizeAll">
              一键优化
            </el-button>
          </div>
        </div>
      </template>
      <div class="smart-dimming-content">
        <!-- 调光统计卡片 -->
        <el-row :gutter="20" class="dimming-stats-row">
          <el-col :span="6">
            <el-card shadow="hover" class="dimming-stat-card">
              <div
                class="dimming-stat-icon"
                style="
                  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                "
              >
                <el-icon size="28" color="#fff"><Sunny /></el-icon>
              </div>
              <div class="dimming-stat-info">
                <div class="dimming-stat-value">
                  {{ dimmingStats.avgBrightness }}%
                </div>
                <div class="dimming-stat-label">平均亮度</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="dimming-stat-card">
              <div
                class="dimming-stat-icon"
                style="
                  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                "
              >
                <el-icon size="28" color="#fff"><Lightning /></el-icon>
              </div>
              <div class="dimming-stat-info">
                <div class="dimming-stat-value">
                  {{ dimmingStats.energySaved }}kWh
                </div>
                <div class="dimming-stat-label">本月节电</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="dimming-stat-card">
              <div
                class="dimming-stat-icon"
                style="
                  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                "
              >
                <el-icon size="28" color="#fff"><Timer /></el-icon>
              </div>
              <div class="dimming-stat-info">
                <div class="dimming-stat-value">
                  {{ dimmingStats.autoAdjustCount }}
                </div>
                <div class="dimming-stat-label">自动调节次数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="dimming-stat-card">
              <div
                class="dimming-stat-icon"
                style="
                  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
                "
              >
                <el-icon size="28" color="#fff"><CircleCheck /></el-icon>
              </div>
              <div class="dimming-stat-info">
                <div class="dimming-stat-value">
                  {{ dimmingStats.optimizationRate }}%
                </div>
                <div class="dimming-stat-label">优化达成率</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 调光记录表格 -->
        <el-card shadow="never" class="dimming-record-card">
          <template #header>
            <div class="card-header">
              <span>近期调光记录</span>
              <el-radio-group v-model="dimmingTimeRange" size="small">
                <el-radio-button label="today">今日</el-radio-button>
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-table
            :data="dimmingRecords"
            style="width: 100%"
            v-loading="dimmingLoading"
          >
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="deviceName" label="设备名称" />
            <el-table-column prop="triggerType" label="触发方式">
              <template #default="scope">
                <el-tag
                  :type="
                    scope.row.triggerType === '自动' ? 'success' : 'primary'
                  "
                  size="small"
                >
                  {{ scope.row.triggerType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ambientLight" label="环境光照">
              <template #default="scope">
                {{ scope.row.ambientLight }} lux
              </template>
            </el-table-column>
            <el-table-column prop="oldBrightness" label="原亮度">
              <template #default="scope">
                <el-progress
                  :percentage="scope.row.oldBrightness"
                  :color="'#909399'"
                  :show-text="true"
                />
              </template>
            </el-table-column>
            <el-table-column prop="newBrightness" label="新亮度">
              <template #default="scope">
                <el-progress
                  :percentage="scope.row.newBrightness"
                  :color="getBrightnessColor(scope.row.newBrightness)"
                  :show-text="true"
                />
              </template>
            </el-table-column>
            <el-table-column prop="energySaved" label="预估节电">
              <template #default="scope">
                <span style="color: #67c23a; font-weight: 500"
                  >{{ scope.row.energySaved }} kWh</span
                >
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 调光效果图表 -->
        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <div class="chart-header">
                  <span>亮度分布统计</span>
                </div>
              </template>
              <div
                ref="brightnessChartRef"
                class="chart-container"
                style="height: 300px"
              ></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <div class="chart-header">
                  <span>调光趋势分析</span>
                </div>
              </template>
              <div
                ref="trendChartRef"
                class="chart-container"
                style="height: 300px"
              ></div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 控制策略 -->
    <el-card
      v-else-if="currentView === '1-3'"
      class="operation-card"
      shadow="never"
    >
      <template #header>
        <div class="card-header">
          <span>控制策略</span>
          <el-button
            type="primary"
            :icon="Plus"
            @click="showAddStrategy = true"
          >
            添加策略
          </el-button>
        </div>
      </template>
      <div class="strategy-content">
        <el-row :gutter="20">
          <el-col
            :span="12"
            v-for="strategy in strategyList"
            :key="strategy.id"
          >
            <el-card shadow="hover" class="strategy-item">
              <template #header>
                <div class="strategy-header">
                  <span class="strategy-name">{{ strategy.name }}</span>
                  <el-switch
                    v-model="strategy.enabled"
                    @change="(val) => toggleStrategy(strategy, val)"
                  />
                </div>
              </template>
              <div class="strategy-body">
                <div class="strategy-desc">{{ strategy.description }}</div>
                <el-divider />
                <div class="strategy-rules">
                  <div
                    class="rule-item"
                    v-for="(rule, index) in strategy.rules"
                    :key="index"
                  >
                    <el-tag size="small" :type="rule.type">{{
                      rule.condition
                    }}</el-tag>
                    <el-icon><ArrowRight /></el-icon>
                    <el-tag size="small" effect="dark">{{
                      rule.action
                    }}</el-tag>
                  </div>
                </div>
              </div>
              <div class="strategy-footer">
                <span class="strategy-time"
                  >更新时间: {{ strategy.updateTime }}</span
                >
                <el-button
                  type="primary"
                  size="small"
                  text
                  @click="editStrategy(strategy)"
                  >编辑</el-button
                >
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 智能调光对话框 -->
    <el-dialog
      v-model="smartAdjustVisible"
      :title="currentLamp ? '智能调光 - ' + currentLamp.name : '批量智能调光'"
      width="500px"
    >
      <el-alert
        v-if="!currentLamp"
        title="将对所有在线路灯进行智能调光"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      />
      <el-form :model="smartForm" label-width="120px">
        <el-form-item label="环境光照强度">
          <el-slider v-model="smartForm.ambientLight" :max="30000" show-input />
          <span class="hint">lux (流明)</span>
        </el-form-item>
        <el-form-item label="预计亮度">
          <el-progress
            :percentage="predictedBrightness"
            :color="brightnessColor"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="smartAdjustVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSmartAdjust"
          >确认调节</el-button
        >
      </template>
    </el-dialog>

    <!-- 故障列表对话框 -->
    <el-dialog v-model="faultVisible" title="故障告警" width="800px">
      <el-table :data="faultList" style="width: 100%">
        <el-table-column prop="id" label="设备ID" />
        <el-table-column prop="name" label="路灯名称" />
        <el-table-column prop="ip" label="IP地址" />
        <el-table-column label="故障类型">
          <template #default="scope">
            <el-tag type="danger">
              {{ scope.row.isBroken ? "设备损坏" : "故障告警" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastMaintenance" label="上次维护" />
      </el-table>
    </el-dialog>

    <!-- 位置地图对话框 -->
    <el-dialog v-model="locationVisible" title="路灯位置" width="800px">
      <div class="location-info">
        <p><strong>路灯名称:</strong> {{ currentLamp?.name }}</p>
        <p><strong>位置:</strong> 湖北省武汉市文华学院光谷创业街附近</p>
        <p><strong>经度:</strong> {{ currentLamp?.longitude || "未设置" }}</p>
        <p><strong>纬度:</strong> {{ currentLamp?.latitude || "未设置" }}</p>
      </div>
      <LampMap :lamps="lampList" :current-lamp="currentLamp" />
    </el-dialog>

    <!-- 添加策略对话框 -->
    <el-dialog
      v-model="showAddStrategy"
      title="添加控制策略"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="newStrategyForm"
        label-width="100px"
        :rules="strategyRules"
        ref="strategyFormRef"
      >
        <el-form-item label="策略名称" prop="name">
          <el-input
            v-model="newStrategyForm.name"
            placeholder="请输入策略名称"
          />
        </el-form-item>
        <el-form-item label="策略描述" prop="description">
          <el-input
            v-model="newStrategyForm.description"
            type="textarea"
            :rows="2"
            placeholder="请输入策略描述"
          />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch
            v-model="newStrategyForm.enabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-divider>规则配置</el-divider>
        <div
          v-for="(rule, index) in newStrategyForm.rules"
          :key="index"
          class="rule-config-item"
        >
          <el-row :gutter="10">
            <el-col :span="10">
              <el-form-item
                :label="`规则${index + 1}条件`"
                :prop="`rules.${index}.condition`"
                :rules="{
                  required: true,
                  message: '请输入条件',
                  trigger: 'blur',
                }"
              >
                <el-input
                  v-model="rule.condition"
                  placeholder="如：时间: 18:00-22:00"
                />
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item
                :label="`规则${index + 1}动作`"
                :prop="`rules.${index}.action`"
                :rules="{
                  required: true,
                  message: '请输入动作',
                  trigger: 'blur',
                }"
              >
                <el-input
                  v-model="rule.action"
                  placeholder="如：亮度提升至80%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="类型">
                <el-select v-model="rule.type" size="small" style="width: 80px">
                  <el-option label="信息" value="info" />
                  <el-option label="警告" value="warning" />
                  <el-option label="成功" value="success" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-button
            v-if="newStrategyForm.rules.length > 1"
            type="danger"
            size="small"
            text
            @click="removeRule(index)"
            >删除规则</el-button
          >
        </div>
        <el-button type="primary" size="small" :icon="Plus" @click="addRule"
          >添加规则</el-button
        >
      </el-form>
      <template #footer>
        <el-button @click="showAddStrategy = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitAddStrategy"
          :loading="addStrategyLoading"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import * as echarts from "echarts";
import {
  Sunny,
  CircleCheck,
  Moon,
  Warning,
  Lightning,
  SwitchButton,
  CircleClose,
  MagicStick,
  Plus,
  Timer,
  ArrowRight,
} from "@element-plus/icons-vue";
import {
  getLampList,
  controlLamp,
  adjustBrightness,
  batchControlLamp,
  getLampStats,
  getFaultLamps,
  smartAdjust as smartAdjustApi,
} from "@/api/auth";
import LampMap from "@/components/map/LampMap.vue";

const props = defineProps({
  activeIndex: {
    type: String,
    default: "1-1",
  },
});

const currentView = computed(() => {
  return props.activeIndex;
});

const loading = ref(false);
const lampList = ref([]);
const faultList = ref([]);
const stats = ref({
  totalCount: 0,
  onlineCount: 0,
  offlineCount: 0,
  onCount: 0,
  offCount: 0,
  faultCount: 0,
  totalPower: 0,
});

// 智能调光
const smartAdjustVisible = ref(false);
const smartForm = ref({
  deviceId: "",
  ambientLight: 5000,
});
const currentLamp = ref(null);

// 故障列表
const faultVisible = ref(false);

// 位置
const locationVisible = ref(false);

// 智能调光统计数据
const dimmingStats = ref({
  avgBrightness: 65,
  energySaved: 1280,
  autoAdjustCount: 156,
  optimizationRate: 92.5,
});
const dimmingTimeRange = ref("today");
const dimmingLoading = ref(false);
const dimmingRecords = ref([
  {
    time: "2025-04-03 14:30:25",
    deviceName: "教学楼A区路灯01",
    triggerType: "自动",
    ambientLight: 8500,
    oldBrightness: 80,
    newBrightness: 60,
    energySaved: 0.15,
  },
  {
    time: "2025-04-03 14:28:10",
    deviceName: "图书馆路灯01",
    triggerType: "手动",
    ambientLight: 12000,
    oldBrightness: 100,
    newBrightness: 70,
    energySaved: 0.22,
  },
  {
    time: "2025-04-03 14:25:45",
    deviceName: "学生宿舍1号楼路灯01",
    triggerType: "自动",
    ambientLight: 9200,
    oldBrightness: 90,
    newBrightness: 65,
    energySaved: 0.18,
  },
  {
    time: "2025-04-03 14:22:30",
    deviceName: "校园主干道路灯01",
    triggerType: "自动",
    ambientLight: 7800,
    oldBrightness: 85,
    newBrightness: 55,
    energySaved: 0.2,
  },
  {
    time: "2025-04-03 14:20:15",
    deviceName: "教学楼B区路灯01",
    triggerType: "自动",
    ambientLight: 10500,
    oldBrightness: 95,
    newBrightness: 70,
    energySaved: 0.19,
  },
]);

// 图表相关
const brightnessChartRef = ref(null);
const trendChartRef = ref(null);
let brightnessChart = null;
let trendChart = null;

// 控制策略
const showAddStrategy = ref(false);
const addStrategyLoading = ref(false);
const strategyFormRef = ref(null);
const newStrategyForm = ref({
  name: "",
  description: "",
  enabled: true,
  rules: [{ condition: "", action: "", type: "info" }],
});
const strategyRules = {
  name: [{ required: true, message: "请输入策略名称", trigger: "blur" }],
  description: [{ required: true, message: "请输入策略描述", trigger: "blur" }],
};
const strategyList = ref([
  {
    id: 1,
    name: "深夜节能模式",
    description: "深夜时段自动降低路灯亮度，节省能源",
    enabled: true,
    updateTime: "2025-04-01 10:30",
    rules: [
      { condition: "时间: 00:00-05:00", action: "亮度降至50%", type: "info" },
      { condition: "人流量<10人/小时", action: "亮度降至30%", type: "warning" },
    ],
  },
  {
    id: 2,
    name: "雨天自动补光",
    description: "雨天或阴天自动提高路灯亮度",
    enabled: true,
    updateTime: "2025-03-28 14:20",
    rules: [
      { condition: "光照强度<5000lux", action: "亮度提升至80%", type: "info" },
      { condition: "降雨概率>70%", action: "提前开启路灯", type: "success" },
    ],
  },
  {
    id: 3,
    name: "人流感应控制",
    description: "根据人流量自动调节路灯亮度",
    enabled: false,
    updateTime: "2025-03-25 09:15",
    rules: [
      { condition: "人流量>50人/小时", action: "亮度100%", type: "info" },
      { condition: "人流量<5人/小时", action: "亮度40%", type: "warning" },
    ],
  },
  {
    id: 4,
    name: "考试周照明保障",
    description: "考试期间延长路灯照明时间",
    enabled: false,
    updateTime: "2025-03-20 16:45",
    rules: [
      { condition: "考试周期间", action: "延长至24:00关闭", type: "info" },
      { condition: "图书馆周边", action: "亮度保持80%", type: "success" },
    ],
  },
]);

const toggleStrategy = (strategy, enabled) => {
  ElMessage.success(`${strategy.name}已${enabled ? "启用" : "禁用"}`);
  strategy.updateTime = new Date().toLocaleString();
};

const editStrategy = (strategy) => {
  ElMessage.info(`编辑策略: ${strategy.name}`);
};

// 添加规则
const addRule = () => {
  newStrategyForm.value.rules.push({ condition: "", action: "", type: "info" });
};

// 删除规则
const removeRule = (index) => {
  newStrategyForm.value.rules.splice(index, 1);
};

// 提交添加策略
const submitAddStrategy = async () => {
  if (!strategyFormRef.value) return;

  await strategyFormRef.value.validate((valid) => {
    if (valid) {
      addStrategyLoading.value = true;
      setTimeout(() => {
        const newStrategy = {
          id: Date.now(),
          name: newStrategyForm.value.name,
          description: newStrategyForm.value.description,
          enabled: newStrategyForm.value.enabled,
          updateTime: new Date().toLocaleString(),
          rules: [...newStrategyForm.value.rules],
        };
        strategyList.value.push(newStrategy);
        ElMessage.success("策略添加成功");
        showAddStrategy.value = false;
        addStrategyLoading.value = false;
        // 重置表单
        newStrategyForm.value = {
          name: "",
          description: "",
          enabled: true,
          rules: [{ condition: "", action: "", type: "info" }],
        };
      }, 500);
    }
  });
};

// 获取亮度颜色
const getBrightnessColor = (brightness) => {
  if (brightness >= 80) return "#f56c6c";
  if (brightness >= 60) return "#e6a23c";
  if (brightness >= 40) return "#409eff";
  return "#67c23a";
};

// 初始化调光图表
const initDimmingCharts = () => {
  if (currentView.value !== "1-2") return;

  nextTick(() => {
    // 亮度分布饼图
    if (brightnessChartRef.value) {
      brightnessChart = echarts.init(brightnessChartRef.value);
      brightnessChart.setOption({
        tooltip: { trigger: "item", formatter: "{b}: {c}盏 ({d}%)" },
        legend: { orient: "vertical", right: "5%", top: "center" },
        series: [
          {
            type: "pie",
            radius: ["40%", "70%"],
            center: ["40%", "50%"],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 8, borderColor: "#fff", borderWidth: 2 },
            label: { show: false },
            emphasis: {
              label: { show: true, fontSize: 14, fontWeight: "bold" },
            },
            data: [
              {
                value: 2,
                name: "高亮度(80-100%)",
                itemStyle: { color: "#f56c6c" },
              },
              {
                value: 3,
                name: "中亮度(50-80%)",
                itemStyle: { color: "#e6a23c" },
              },
              {
                value: 2,
                name: "低亮度(30-50%)",
                itemStyle: { color: "#409eff" },
              },
              {
                value: 1,
                name: "微亮(0-30%)",
                itemStyle: { color: "#67c23a" },
              },
            ],
          },
        ],
      });
    }

    // 调光趋势折线图
    if (trendChartRef.value) {
      trendChart = echarts.init(trendChartRef.value);
      trendChart.setOption({
        tooltip: { trigger: "axis" },
        legend: { data: ["自动调光", "手动调光"], bottom: 0 },
        grid: {
          left: "3%",
          right: "4%",
          bottom: "15%",
          top: "10%",
          containLabel: true,
        },
        xAxis: {
          type: "category",
          data: ["00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "24:00"],
          axisLine: { lineStyle: { color: "#ccc" } },
          axisLabel: { color: "#666" },
        },
        yAxis: {
          type: "value",
          name: "次数",
          axisLine: { lineStyle: { color: "#ccc" } },
          axisLabel: { color: "#666" },
          splitLine: { lineStyle: { color: "#eee" } },
        },
        series: [
          {
            name: "自动调光",
            type: "line",
            data: [12, 8, 25, 35, 28, 42, 18],
            smooth: true,
            itemStyle: { color: "#67c23a" },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "rgba(103, 194, 58, 0.3)" },
                { offset: 1, color: "rgba(103, 194, 58, 0.05)" },
              ]),
            },
          },
          {
            name: "手动调光",
            type: "line",
            data: [5, 3, 8, 12, 10, 15, 8],
            smooth: true,
            itemStyle: { color: "#409eff" },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "rgba(64, 158, 255, 0.3)" },
                { offset: 1, color: "rgba(64, 158, 255, 0.05)" },
              ]),
            },
          },
        ],
      });
    }
  });
};

// 监听视图变化
watch(
  () => currentView.value,
  (newVal) => {
    if (newVal === "1-2") {
      // 延迟初始化，确保DOM已渲染
      setTimeout(() => {
        initDimmingCharts();
      }, 100);
    }
  }
);

// 窗口大小变化时重新调整图表
const handleResize = () => {
  brightnessChart?.resize();
  trendChart?.resize();
};

// 计算预测亮度
const predictedBrightness = computed(() => {
  const ambient = smartForm.value.ambientLight;
  if (ambient < 1000) return 100;
  if (ambient < 5000) return 80;
  if (ambient < 10000) return 60;
  if (ambient < 20000) return 40;
  return 20;
});

const brightnessColor = computed(() => {
  const brightness = predictedBrightness.value;
  if (brightness >= 80) return "#f56c6c";
  if (brightness >= 60) return "#e6a23c";
  if (brightness >= 40) return "#409eff";
  return "#67c23a";
});

// 获取路灯列表
const fetchLampList = async () => {
  loading.value = true;
  try {
    const response = await getLampList();
    if (response.data.code === 1) {
      lampList.value = response.data.data;
    }
  } catch (error) {
    ElMessage.error("获取路灯列表失败");
  } finally {
    loading.value = false;
  }
};

// 获取统计信息
const fetchStats = async () => {
  try {
    const response = await getLampStats();
    if (response.data.code === 1) {
      stats.value = response.data.data;
    }
  } catch (error) {
    console.error("获取统计信息失败", error);
  }
};

// 获取故障列表
const fetchFaultList = async () => {
  try {
    const response = await getFaultLamps();
    if (response.data.code === 1) {
      faultList.value = response.data.data;
    }
  } catch (error) {
    ElMessage.error("获取故障列表失败");
  }
};

// 开关灯
const handleLampSwitch = async (lamp, status) => {
  try {
    const response = await controlLamp({
      deviceId: lamp.id,
      status: status,
    });
    if (response.data.code === 1) {
      ElMessage.success(status === 1 ? "路灯已开启" : "路灯已关闭");
      fetchStats();
    } else {
      ElMessage.error("操作失败");
      lamp.lampStatus = status === 1 ? 0 : 1;
    }
  } catch (error) {
    ElMessage.error("操作失败");
    lamp.lampStatus = status === 1 ? 0 : 1;
  }
};

// 调节亮度
const handleBrightnessChange = async (lamp, brightness) => {
  try {
    const response = await adjustBrightness({
      deviceId: lamp.id,
      brightness: brightness,
    });
    if (response.data.code === 1) {
      ElMessage.success("亮度调节成功");
    } else {
      ElMessage.error("亮度调节失败");
    }
  } catch (error) {
    ElMessage.error("亮度调节失败");
  }
};

// 批量开灯
const batchTurnOn = async () => {
  try {
    await ElMessageBox.confirm("确定要开启所有路灯吗？", "提示", {
      type: "warning",
    });
    const lampIds = lampList.value.filter((l) => l.onLine).map((l) => l.id);
    const response = await batchControlLamp({
      deviceIds: lampIds,
      status: 1,
    });
    if (response.data.code === 1) {
      ElMessage.success("批量开灯成功");
      fetchLampList();
      fetchStats();
    }
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("批量开灯失败");
    }
  }
};

// 批量关灯
const batchTurnOff = async () => {
  try {
    await ElMessageBox.confirm("确定要关闭所有路灯吗？", "提示", {
      type: "warning",
    });
    const lampIds = lampList.value.filter((l) => l.onLine).map((l) => l.id);
    const response = await batchControlLamp({
      deviceIds: lampIds,
      status: 0,
    });
    if (response.data.code === 1) {
      ElMessage.success("批量关灯成功");
      fetchLampList();
      fetchStats();
    }
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("批量关灯失败");
    }
  }
};

// 显示智能调光对话框（批量）
const showSmartAdjust = () => {
  currentLamp.value = null;
  smartForm.value = {
    deviceId: "",
    deviceIds: [],
    ambientLight: 5000,
  };
  smartAdjustVisible.value = true;
};

// 单个路灯智能调光
const smartAdjust = (lamp) => {
  currentLamp.value = lamp;
  smartForm.value = {
    deviceId: lamp.id,
    deviceIds: [],
    ambientLight: 5000,
  };
  smartAdjustVisible.value = true;
};

// 确认智能调光
const confirmSmartAdjust = async () => {
  try {
    let response;

    // 批量调光
    if (!smartForm.value.deviceId && !currentLamp.value) {
      const onlineLampIds = lampList.value
        .filter((l) => l.onLine)
        .map((l) => l.id);

      if (onlineLampIds.length === 0) {
        ElMessage.warning("没有在线的路灯可以调光");
        return;
      }

      response = await smartAdjustApi({
        deviceIds: onlineLampIds,
        ambientLight: smartForm.value.ambientLight,
      });

      if (response.data.code === 1) {
        const data = response.data.data;
        ElMessage.success(
          `批量智能调光成功，共调节 ${data.successCount}/${data.totalCount} 盏路灯，亮度 ${data.brightness}%`,
        );
        fetchLampList();
        fetchStats();
        smartAdjustVisible.value = false;
      } else {
        ElMessage.error(response.data.msg || "批量智能调光失败");
      }
    } else {
      // 单个调光
      response = await smartAdjustApi({
        deviceId: smartForm.value.deviceId,
        ambientLight: smartForm.value.ambientLight,
      });

      if (response.data.code === 1) {
        ElMessage.success(
          `智能调光成功，设置亮度为 ${response.data.data.brightness}%`,
        );
        fetchLampList();
        smartAdjustVisible.value = false;
      } else {
        ElMessage.error(response.data.msg || "智能调光失败");
      }
    }
  } catch (error) {
    ElMessage.error("智能调光失败");
  }
};

// 显示故障列表
const showFaultList = () => {
  fetchFaultList();
  faultVisible.value = true;
};

// 显示详情
const showDetail = (lamp) => {
  ElMessageBox.alert(
    `
    设备ID: ${lamp.id}
    名称: ${lamp.name}
    位置: 湖北省武汉市文华学院光谷创业街附近
    IP地址: ${lamp.ip}
    端口: ${lamp.port}
    在线状态: ${lamp.onLine ? "在线" : "离线"}
    开关状态: ${lamp.lampStatus ? "开启" : "关闭"}
    亮度: ${lamp.brightness}%
    功耗: ${lamp.powerConsumption}W
    上次维护: ${lamp.lastMaintenance || "未记录"}
  `,
    "路灯详情",
    {
      confirmButtonText: "确定",
    },
  );
};

// 显示位置
const showLocation = (lamp) => {
  currentLamp.value = lamp;
  locationVisible.value = true;
};

onMounted(() => {
  fetchLampList();
  fetchStats();
  window.addEventListener("resize", handleResize);
  // 如果当前是智能调光页面，初始化图表
  if (currentView.value === "1-2") {
    setTimeout(() => {
      initDimmingCharts();
    }, 100);
  }
});
</script>

<style scoped>
.lamp-manager {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.operation-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.hint {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.location-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.location-info p {
  margin: 8px 0;
  font-size: 14px;
}

.location-info strong {
  color: #303133;
  display: inline-block;
  width: 80px;
}

.smart-dimming-content,
.strategy-content {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dimming-options {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.dimming-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.strategy-item {
  margin-bottom: 20px;
}

.strategy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.strategy-name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.strategy-body {
  padding: 10px 0;
}

.strategy-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 15px;
}

.strategy-rules {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.strategy-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.strategy-time {
  font-size: 12px;
  color: #909399;
}

/* 智能调光样式 */
.dimming-stats-row {
  margin-bottom: 20px;
}

.dimming-stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.dimming-stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.dimming-stat-info {
  flex: 1;
}

.dimming-stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.dimming-stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.dimming-record-card {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

/* 规则配置样式 */
.rule-config-item {
  margin-bottom: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

@media (max-width: 1200px) {
  .dimming-stats-row .el-col {
    width: 50%;
    margin-bottom: 15px;
  }
}

@media (max-width: 768px) {
  .dimming-stats-row .el-col {
    width: 100%;
  }
}
</style>
