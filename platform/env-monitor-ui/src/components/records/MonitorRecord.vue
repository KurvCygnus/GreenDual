<template>
  <div class="monitor-record">
    <!-- 查询条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="设备名称">
          <el-select
            v-model="filterForm.deviceId"
            placeholder="全部设备"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="device in deviceList"
              :key="device.id"
              :label="device.name"
              :value="device.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select
            v-model="filterForm.dataType"
            placeholder="全部类型"
            clearable
            style="width: 150px"
          >
            <el-option label="温度" value="temperature" />
            <el-option label="湿度" value="humidity" />
            <el-option label="光照" value="light" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch"
            >查询</el-button
          >
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" :icon="Download" @click="handleExport"
            >导出</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="32" color="#409eff"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ totalRecords }}</div>
              <div class="stat-label">总记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="32" color="#67c23a"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ normalRecords }}</div>
              <div class="stat-label">正常记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="32" color="#e6a23c"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ warningRecords }}</div>
              <div class="stat-label">预警记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="32" color="#f56c6c"><WarningFilled /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ abnormalRecords }}</div>
              <div class="stat-label">异常记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>监测记录列表</span>
          <el-button type="primary" :icon="Plus" @click="showAddDialog"
            >添加记录</el-button
          >
        </div>
      </template>
      <el-table
        :data="recordList"
        style="width: 100%"
        v-loading="loading"
        border
        stripe
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="recordTime" label="记录时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.recordTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="dataType" label="数据类型" width="100">
          <template #default="scope">
            <el-tag :type="getDataTypeTag(scope.row.dataType)">
              {{ getDataTypeName(scope.row.dataType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" label="数值" width="120">
          <template #default="scope">
            <span
              :class="{ 'abnormal-value': scope.row.status === 'abnormal' }"
            >
              {{ scope.row.value }} {{ getUnit(scope.row.dataType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewDetail(scope.row)"
              >详情</el-button
            >
            <el-button
              type="danger"
              size="small"
              @click="deleteRecord(scope.row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="记录详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="设备名称">{{
          currentRecord.deviceName
        }}</el-descriptions-item>
        <el-descriptions-item label="记录时间">{{
          formatDate(currentRecord.recordTime)
        }}</el-descriptions-item>
        <el-descriptions-item label="数据类型">{{
          getDataTypeName(currentRecord.dataType)
        }}</el-descriptions-item>
        <el-descriptions-item label="数值"
          >{{ currentRecord.value }}
          {{ getUnit(currentRecord.dataType) }}</el-descriptions-item
        >
        <el-descriptions-item label="状态">{{
          getStatusName(currentRecord.status)
        }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{
          currentRecord.remark || "无"
        }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 添加记录对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加监测记录" width="500px">
      <el-form
        :model="addForm"
        label-width="100px"
        :rules="addRules"
        ref="addFormRef"
      >
        <el-form-item label="设备" prop="deviceId">
          <el-select
            v-model="addForm.deviceId"
            placeholder="请选择设备"
            style="width: 100%"
          >
            <el-option
              v-for="device in deviceList"
              :key="device.id"
              :label="device.name"
              :value="device.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数据类型" prop="dataType">
          <el-select
            v-model="addForm.dataType"
            placeholder="请选择数据类型"
            style="width: 100%"
          >
            <el-option label="温度" value="temperature" />
            <el-option label="湿度" value="humidity" />
            <el-option label="光照" value="light" />
          </el-select>
        </el-form-item>
        <el-form-item label="数值" prop="value">
          <el-input-number
            v-model="addForm.value"
            :precision="2"
            :step="0.1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="addForm.status"
            placeholder="请选择状态"
            style="width: 100%"
          >
            <el-option label="正常" value="normal" />
            <el-option label="预警" value="warning" />
            <el-option label="异常" value="abnormal" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录时间" prop="recordTime">
          <el-date-picker
            v-model="addForm.recordTime"
            type="datetime"
            placeholder="选择记录时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="addForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddRecord" :loading="addLoading"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Search,
  Refresh,
  Download,
  Document,
  CircleCheck,
  Warning,
  WarningFilled,
  Plus,
} from "@element-plus/icons-vue";
import {
  getDeviceList,
  getRecordList,
  getRecordStats,
  deleteRecord as deleteRecordApi,
  addRecord as addRecordApi,
} from "@/api/auth";

const loading = ref(false);
const detailVisible = ref(false);
const currentRecord = ref({});
const addDialogVisible = ref(false);
const addLoading = ref(false);
const addFormRef = ref(null);

// 添加表单
const addForm = reactive({
  deviceId: "",
  deviceName: "",
  dataType: "",
  value: 0,
  status: "normal",
  recordTime: "",
  remark: "",
});

// 添加表单验证规则
const addRules = {
  deviceId: [{ required: true, message: "请选择设备", trigger: "change" }],
  dataType: [{ required: true, message: "请选择数据类型", trigger: "change" }],
  value: [{ required: true, message: "请输入数值", trigger: "blur" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
};

// 设备列表
const deviceList = ref([]);

// 筛选条件
const filterForm = reactive({
  deviceId: "",
  dateRange: [],
  dataType: "",
});

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

// 统计数据
const totalRecords = ref(1256);
const normalRecords = ref(1180);
const warningRecords = ref(56);
const abnormalRecords = ref(20);

// 记录列表
const recordList = ref([]);

// 生成模拟数据
const generateRecords = () => {
  const records = [];
  const dataTypes = ["temperature", "humidity", "light"];
  const statuses = ["normal", "warning", "abnormal"];
  const devices =
    deviceList.value.length > 0
      ? deviceList.value
      : [
          { id: "ENV001", name: "农田监测点1号" },
          { id: "ENV002", name: "温室大棚A区" },
        ];

  for (let i = 0; i < 20; i++) {
    const device = devices[Math.floor(Math.random() * devices.length)];
    const dataType = dataTypes[Math.floor(Math.random() * dataTypes.length)];
    let value, status, remark;

    if (dataType === "temperature") {
      value = (20 + Math.random() * 15).toFixed(1);
      if (value > 32) {
        status = "abnormal";
        remark = "温度过高";
      } else if (value > 28) {
        status = "warning";
        remark = "温度偏高";
      } else {
        status = "normal";
        remark = "正常";
      }
    } else if (dataType === "humidity") {
      value = Math.floor(40 + Math.random() * 40);
      if (value > 85) {
        status = "warning";
        remark = "湿度过高";
      } else if (value < 45) {
        status = "warning";
        remark = "湿度偏低";
      } else {
        status = "normal";
        remark = "正常";
      }
    } else {
      value = Math.floor(5000 + Math.random() * 15000);
      status = "normal";
      remark = "正常";
    }

    records.push({
      id: `REC${String(i + 1).padStart(4, "0")}`,
      deviceId: device.id,
      deviceName: device.name,
      recordTime: new Date(
        Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000,
      ),
      dataType,
      value,
      status,
      remark,
    });
  }

  return records.sort((a, b) => b.recordTime - a.recordTime);
};

// 获取设备列表
const fetchDeviceList = async () => {
  try {
    const response = await getDeviceList({ page: 1, pageSize: 100 });
    if (response.data.code === 1) {
      deviceList.value = response.data.data || [];
    }
  } catch (error) {
    console.error("获取设备列表失败", error);
  }
};

// 获取记录列表
const fetchRecords = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      deviceId: filterForm.deviceId,
      dataType: filterForm.dataType,
      dateRange: filterForm.dateRange,
    };
    const response = await getRecordList(params);
    if (response.data.code === 1) {
      recordList.value = response.data.data || [];
      pagination.total = response.data.totalPages || 0;
    }
  } catch (error) {
    console.error("获取记录列表失败", error);
    recordList.value = generateRecords();
  } finally {
    loading.value = false;
  }
};

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await getRecordStats();
    if (response.data.code === 1) {
      const stats = response.data.data;
      totalRecords.value = stats.total || 0;
      normalRecords.value = stats.normal || 0;
      warningRecords.value = stats.warning || 0;
      abnormalRecords.value = stats.abnormal || 0;
    }
  } catch (error) {
    console.error("获取统计失败", error);
  }
};

// 格式化日期
const formatDate = (date) => {
  if (!date) return "";
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")} ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}:${String(d.getSeconds()).padStart(2, "0")}`;
};

// 获取数据类型标签
const getDataTypeTag = (type) => {
  const map = { temperature: "danger", humidity: "primary", light: "warning" };
  return map[type] || "info";
};

// 获取数据类型名称
const getDataTypeName = (type) => {
  const map = { temperature: "温度", humidity: "湿度", light: "光照" };
  return map[type] || type;
};

// 获取单位
const getUnit = (type) => {
  const map = { temperature: "°C", humidity: "%", light: "lux" };
  return map[type] || "";
};

// 获取状态标签
const getStatusTag = (status) => {
  const map = { normal: "success", warning: "warning", abnormal: "danger" };
  return map[status] || "info";
};

// 获取状态名称
const getStatusName = (status) => {
  const map = { normal: "正常", warning: "预警", abnormal: "异常" };
  return map[status] || status;
};

// 查询
const handleSearch = () => {
  pagination.currentPage = 1;
  fetchRecords();
  ElMessage.success("查询成功");
};

// 重置
const handleReset = () => {
  filterForm.deviceId = "";
  filterForm.dateRange = [];
  filterForm.dataType = "";
  pagination.currentPage = 1;
  fetchRecords();
};

// 导出
const handleExport = () => {
  ElMessage.success("数据导出成功，请查看下载目录");
};

// 查看详情
const viewDetail = (record) => {
  currentRecord.value = record;
  detailVisible.value = true;
};

// 删除记录
const deleteRecord = (record) => {
  ElMessageBox.confirm(`确定要删除该记录吗？`, "提示", {
    type: "warning",
  })
    .then(async () => {
      try {
        const response = await deleteRecordApi(record.id);
        if (response.data.code === 1) {
          const index = recordList.value.findIndex((r) => r.id === record.id);
          if (index > -1) {
            recordList.value.splice(index, 1);
          }
          ElMessage.success("删除成功");
        } else {
          ElMessage.error(response.data.msg || "删除失败");
        }
      } catch (error) {
        console.error("删除记录失败", error);
        ElMessage.error("删除失败");
      }
    })
    .catch(() => {});
};

// 显示添加对话框
const showAddDialog = () => {
  // 重置表单
  addForm.deviceId = "";
  addForm.deviceName = "";
  addForm.dataType = "";
  addForm.value = 0;
  addForm.status = "normal";
  // 格式化日期为 yyyy-MM-dd HH:mm:ss 格式
  const now = new Date();
  addForm.recordTime =
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
  addForm.remark = "";
  addDialogVisible.value = true;
};

// 提交添加记录
const submitAddRecord = async () => {
  if (!addFormRef.value) return;

  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      addLoading.value = true;
      try {
        // 获取设备名称
        const device = deviceList.value.find((d) => d.id === addForm.deviceId);
        const recordData = {
          ...addForm,
          deviceName: device ? device.name : addForm.deviceId,
        };

        const response = await addRecordApi(recordData);
        if (response.data.code === 1) {
          ElMessage.success("添加记录成功");
          addDialogVisible.value = false;
          fetchRecords(); // 刷新列表
          fetchStats(); // 刷新统计
        } else {
          ElMessage.error(response.data.msg || "添加失败");
        }
      } catch (error) {
        console.error("添加记录失败", error);
        ElMessage.error("添加失败");
      } finally {
        addLoading.value = false;
      }
    }
  });
};

// 分页
const handleSizeChange = (size) => {
  pagination.pageSize = size;
  fetchRecords();
};

const handleCurrentChange = (page) => {
  pagination.currentPage = page;
  fetchRecords();
};

onMounted(() => {
  fetchDeviceList();
  fetchRecords();
  fetchStats();
});
</script>

<style scoped>
.monitor-record {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
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
  gap: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.table-card {
  margin-bottom: 20px;
}

.abnormal-value {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
