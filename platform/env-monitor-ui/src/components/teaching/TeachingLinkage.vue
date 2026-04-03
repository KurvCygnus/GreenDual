<template>
  <div class="teaching-linkage">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #409eff">
            <el-icon size="28" color="#fff"><School /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">12</div>
            <div class="stat-label">合作班级</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon size="28" color="#fff"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">486</div>
            <div class="stat-label">参与学生</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon size="28" color="#fff"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">36</div>
            <div class="stat-label">实践课程</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #8e44ad">
            <el-icon size="28" color="#fff"><Trophy /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">8</div>
            <div class="stat-label">获奖项目</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 教学场景 -->
    <el-row :gutter="20" class="content-row">
      <el-col :span="16">
        <el-card shadow="hover" class="scenario-card">
          <template #header>
            <div class="card-header">
              <span>教学实践场景</span>
              <el-button
                type="primary"
                size="small"
                @click="showAddScenario = true"
              >
                <el-icon><Plus /></el-icon>添加场景
              </el-button>
            </div>
          </template>
          <div class="scenario-list">
            <div
              v-for="scenario in scenarios"
              :key="scenario.id"
              class="scenario-item"
            >
              <div
                class="scenario-icon"
                :style="{ background: scenario.color }"
              >
                <el-icon size="24" color="#fff"
                  ><component :is="scenario.icon"
                /></el-icon>
              </div>
              <div class="scenario-content">
                <div class="scenario-title">{{ scenario.title }}</div>
                <div class="scenario-desc">{{ scenario.description }}</div>
                <div class="scenario-meta">
                  <el-tag size="small" :type="scenario.status">{{
                    scenario.statusText
                  }}</el-tag>
                  <span class="scenario-class">{{ scenario.className }}</span>
                  <span class="scenario-time">{{ scenario.time }}</span>
                </div>
              </div>
              <div class="scenario-actions">
                <el-button
                  type="primary"
                  size="small"
                  text
                  @click="viewScenario(scenario)"
                  >查看</el-button
                >
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="resource-card">
          <template #header>
            <div class="card-header">
              <span>教学资源</span>
            </div>
          </template>
          <div class="resource-list">
            <div
              v-for="resource in resources"
              :key="resource.id"
              class="resource-item"
              @click="openResource(resource)"
            >
              <el-icon size="20" :color="resource.color"
                ><component :is="resource.icon"
              /></el-icon>
              <div class="resource-info">
                <div class="resource-title">{{ resource.title }}</div>
                <div class="resource-count">{{ resource.count }}个资源</div>
              </div>
              <el-icon class="resource-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="notice-card" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>最新动态</span>
            </div>
          </template>
          <div class="notice-list">
            <div v-for="notice in notices" :key="notice.id" class="notice-item">
              <div class="notice-dot" :class="notice.type"></div>
              <div class="notice-content">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-time">{{ notice.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 课程案例 -->
    <el-card shadow="hover" class="case-card">
      <template #header>
        <div class="card-header">
          <span>优秀课程案例</span>
          <el-button type="primary" size="small" text>查看更多</el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8" v-for="caseItem in caseList" :key="caseItem.id">
          <div class="case-item">
            <div class="case-image" :style="{ background: caseItem.bgColor }">
              <el-icon size="48" color="#fff"
                ><component :is="caseItem.icon"
              /></el-icon>
            </div>
            <div class="case-content">
              <div class="case-title">{{ caseItem.title }}</div>
              <div class="case-desc">{{ caseItem.description }}</div>
              <div class="case-footer">
                <span class="case-teacher">{{ caseItem.teacher }}</span>
                <el-tag size="small" type="success">{{ caseItem.tag }}</el-tag>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 添加场景对话框 -->
    <el-dialog v-model="showAddScenario" title="添加教学场景" width="600px">
      <el-form :model="newScenario" label-width="100px">
        <el-form-item label="场景名称">
          <el-input v-model="newScenario.title" placeholder="请输入场景名称" />
        </el-form-item>
        <el-form-item label="所属课程">
          <el-select
            v-model="newScenario.course"
            placeholder="请选择课程"
            style="width: 100%"
          >
            <el-option label="环境科学导论" value="env-intro" />
            <el-option label="物联网技术" value="iot" />
            <el-option label="智慧校园实践" value="smart-campus" />
            <el-option label="绿色能源管理" value="green-energy" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select
            v-model="newScenario.class"
            placeholder="请选择班级"
            style="width: 100%"
          >
            <el-option label="计算机2301班" value="cs2301" />
            <el-option label="物联网2202班" value="iot2202" />
            <el-option label="环境科学2101班" value="env2101" />
          </el-select>
        </el-form-item>
        <el-form-item label="场景描述">
          <el-input
            v-model="newScenario.description"
            type="textarea"
            rows="3"
            placeholder="请输入场景描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddScenario = false">取消</el-button>
        <el-button type="primary" @click="addScenario">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import {
  School,
  User,
  Document,
  Trophy,
  Plus,
  ArrowRight,
  Monitor,
  FirstAidKit,
  Sunny,
  TrendCharts,
  Reading,
  Opportunity,
  DataLine,
  Microphone,
} from "@element-plus/icons-vue";

// 教学场景数据
const scenarios = ref([
  {
    id: 1,
    title: "路灯能耗数据分析",
    description: "利用节能引擎采集的路灯用电数据，进行能耗分析和节能方案设计",
    icon: "Sunny",
    color: "#E6A23C",
    status: "success",
    statusText: "进行中",
    className: "物联网2202班",
    time: "2024-03-15",
  },
  {
    id: 2,
    title: "绿植生长环境研究",
    description: "通过生态引擎监测数据，分析校园绿植生长环境，提出改善建议",
    icon: "FirstAidKit",
    color: "#67C23A",
    status: "primary",
    statusText: "已发布",
    className: "环境科学2101班",
    time: "2024-03-12",
  },
  {
    id: 3,
    title: "智慧校园系统搭建",
    description: "学生动手搭建小型环境监测系统，体验双引擎协同工作原理",
    icon: "Monitor",
    color: "#409EFF",
    status: "warning",
    statusText: "筹备中",
    className: "计算机2301班",
    time: "2024-03-20",
  },
  {
    id: 4,
    title: "AI节能算法实践",
    description: "学习AI调光算法原理，编写简单的节能控制程序",
    icon: "Opportunity",
    color: "#8E44AD",
    status: "success",
    statusText: "进行中",
    className: "人工智能2201班",
    time: "2024-03-18",
  },
]);

// 教学资源
const resources = ref([
  {
    id: 1,
    title: "实验指导手册",
    count: 24,
    icon: "Document",
    color: "#409EFF",
  },
  { id: 2, title: "教学视频", count: 18, icon: "Monitor", color: "#67C23A" },
  { id: 3, title: "数据集", count: 36, icon: "DataLine", color: "#E6A23C" },
  { id: 4, title: "课件PPT", count: 42, icon: "Reading", color: "#8E44AD" },
]);

// 最新动态
const notices = ref([
  {
    id: 1,
    title: "物联网2202班完成路灯数据分析报告",
    time: "10分钟前",
    type: "success",
  },
  {
    id: 2,
    title: "新增《智慧校园实践》课程场景",
    time: "2小时前",
    type: "primary",
  },
  {
    id: 3,
    title: "环境科学2101班绿植研究获奖",
    time: "1天前",
    type: "warning",
  },
  { id: 4, title: "教学资源库更新12个新案例", time: "2天前", type: "info" },
]);

// 课程案例
const caseList = ref([
  {
    id: 1,
    title: "基于双引擎的校园节能方案",
    description:
      "学生通过分析真实监测数据，提出校园照明和绿植养护的综合节能方案",
    teacher: "张教授",
    tag: "优秀案例",
    icon: "TrendCharts",
    bgColor: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
  },
  {
    id: 2,
    title: "微环境监测与植物生长研究",
    description: "利用生态引擎数据，研究不同环境条件下校园绿植的生长状况",
    teacher: "李老师",
    tag: "创新实践",
    icon: "FirstAidKit",
    bgColor: "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)",
  },
  {
    id: 3,
    title: "AIoT智慧校园系统开发",
    description: "学生团队开发基于物联网的智慧校园管理系统原型",
    teacher: "王工程师",
    tag: "工程实践",
    icon: "Microphone",
    bgColor: "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)",
  },
]);

// 添加场景
const showAddScenario = ref(false);
const newScenario = ref({
  title: "",
  course: "",
  class: "",
  description: "",
});

const addScenario = () => {
  if (!newScenario.value.title) {
    ElMessage.warning("请输入场景名称");
    return;
  }
  scenarios.value.unshift({
    id: Date.now(),
    title: newScenario.value.title,
    description: newScenario.value.description,
    icon: "Monitor",
    color: "#409EFF",
    status: "primary",
    statusText: "已发布",
    className: newScenario.value.class,
    time: new Date().toISOString().split("T")[0],
  });
  showAddScenario.value = false;
  ElMessage.success("场景添加成功");
  newScenario.value = { title: "", course: "", class: "", description: "" };
};

const viewScenario = (scenario) => {
  ElMessage.info(`查看场景：${scenario.title}`);
};

const openResource = (resource) => {
  ElMessage.info(`打开资源：${resource.title}`);
};
</script>

<style scoped>
.teaching-linkage {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
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

.content-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.scenario-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.scenario-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.scenario-item:hover {
  background: #e4e7ed;
}

.scenario-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.scenario-content {
  flex: 1;
  min-width: 0;
}

.scenario-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.scenario-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.4;
}

.scenario-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.scenario-class {
  color: #606266;
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.resource-item:hover {
  background: #e4e7ed;
}

.resource-info {
  flex: 1;
  margin-left: 12px;
}

.resource-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.resource-count {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.resource-arrow {
  color: #c0c4cc;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.notice-dot.success {
  background: #67c23a;
}
.notice-dot.primary {
  background: #409eff;
}
.notice-dot.warning {
  background: #e6a23c;
}
.notice-dot.info {
  background: #909399;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
  margin-bottom: 2px;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.case-card {
  margin-top: 20px;
}

.case-item {
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.case-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.case-image {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.case-content {
  padding: 16px;
}

.case-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.case-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 12px;
  height: 40px;
  overflow: hidden;
}

.case-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.case-teacher {
  font-size: 12px;
  color: #909399;
}
</style>
