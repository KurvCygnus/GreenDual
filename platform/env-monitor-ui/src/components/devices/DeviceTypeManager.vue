<template>
  <div class="device-type-manager">
    <!-- 搜索栏 -->
    <el-card class="search-card mb-4" shadow="never">
      <el-form :inline="true" :model="searchForm" class="flex items-center">
        <el-form-item label="类型名称" class="mr-4">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入类型名称"
            clearable
            style="width: 220px"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item label="状态" class="mr-4">
          <el-select
            v-model="searchForm.status"
            placeholder="全部"
            clearable
            style="width: 120px"
            class="rounded-lg"
          >
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item class="flex gap-2">
          <el-button
            type="primary"
            :icon="Search"
            @click="handleSearch"
            class="rounded-full"
          >
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset" class="rounded-full">
            重置
          </el-button>
          <el-button
            type="primary"
            size="small"
            @click="addDeviceType"
            class="rounded-full"
          >
            <el-icon><Plus /></el-icon>
            添加设备类型
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row mb-4">
      <el-col :span="8">
        <el-card
          class="stat-card h-full rounded-xl overflow-hidden transition-all duration-300 hover:shadow-lg"
          shadow="hover"
        >
          <div class="stat-content flex items-center p-4">
            <div class="stat-icon bg-blue-500 rounded-full p-3 mr-4">
              <el-icon size="32" color="#fff"><Cpu /></el-icon>
            </div>
            <div class="stat-info flex-1">
              <div class="stat-value text-3xl font-bold text-gray-800">
                {{ totalCount }}
              </div>
              <div class="stat-label text-gray-500 mt-1">类型总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card
          class="stat-card h-full rounded-xl overflow-hidden transition-all duration-300 hover:shadow-lg"
          shadow="hover"
        >
          <div class="stat-content flex items-center p-4">
            <div class="stat-icon bg-green-500 rounded-full p-3 mr-4">
              <el-icon size="32" color="#fff"><Check /></el-icon>
            </div>
            <div class="stat-info flex-1">
              <div class="stat-value text-3xl font-bold text-gray-800">
                {{ activeCount }}
              </div>
              <div class="stat-label text-gray-500 mt-1">启用类型</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card
          class="stat-card h-full rounded-xl overflow-hidden transition-all duration-300 hover:shadow-lg"
          shadow="hover"
        >
          <div class="stat-content flex items-center p-4">
            <div class="stat-icon bg-yellow-500 rounded-full p-3 mr-4">
              <el-icon size="32" color="#fff"><Close /></el-icon>
            </div>
            <div class="stat-info flex-1">
              <div class="stat-value text-3xl font-bold text-gray-800">
                {{ inactiveCount }}
              </div>
              <div class="stat-label text-gray-500 mt-1">禁用类型</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="rounded-xl overflow-hidden">
      <div class="device-type-content p-4">
        <!-- 空状态 -->
        <el-empty
          v-if="filteredTypes.length === 0"
          description="暂无设备类型数据"
          class="py-10"
        >
          <el-button type="primary" @click="addDeviceType" class="rounded-full">
            添加设备类型
          </el-button>
        </el-empty>

        <el-row v-else :gutter="24" class="device-type-grid">
          <el-col
            v-for="type in filteredTypes"
            :key="type.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            style="margin-bottom: 24px"
          >
            <el-card
              class="device-type-card rounded-xl overflow-hidden transition-all duration-300 hover:shadow-xl hover:-translate-y-1"
              :class="{ active: selectedType === type.id }"
            >
              <div
                class="device-type-header text-center pb-4 border-b border-gray-100"
              >
                <div
                  class="type-icon inline-block rounded-full p-4 mb-3"
                  :style="{ backgroundColor: type.color + '20' }"
                >
                  <component
                    :is="getTypeIcon(type.type)"
                    :size="36"
                    :color="type.color"
                  />
                </div>
                <h3 class="text-lg font-semibold text-gray-800 mb-2">
                  {{ type.name }}
                </h3>
                <el-tag
                  :type="type.status === 'active' ? 'success' : 'info'"
                  size="small"
                  effect="light"
                  class="rounded-full px-3"
                >
                  {{ type.status === "active" ? "启用" : "禁用" }}
                </el-tag>
              </div>

              <div class="device-type-info p-4">
                <div class="info-item flex mb-3">
                  <span class="label text-gray-500 w-24">设备类型:</span>
                  <span class="value text-gray-700 flex-1 font-medium">{{
                    type.type
                  }}</span>
                </div>
                <div class="info-item flex mb-3">
                  <span class="label text-gray-500 w-24">支持传感器:</span>
                  <span class="value text-gray-700 flex-1">{{
                    type.sensors.join(", ")
                  }}</span>
                </div>
                <div class="info-item flex mb-3">
                  <span class="label text-gray-500 w-24">设备数量:</span>
                  <span class="value text-gray-700 flex-1 font-medium">{{
                    type.deviceCount
                  }}</span>
                </div>
                <div class="info-item flex mb-3">
                  <span class="label text-gray-500 w-24">描述:</span>
                  <span class="value text-gray-700 flex-1">{{
                    type.description
                  }}</span>
                </div>
              </div>

              <div
                class="device-type-actions p-4 border-t border-gray-100 flex gap-2 justify-center"
              >
                <el-button
                  type="primary"
                  size="small"
                  @click="editDeviceType(type)"
                  class="rounded-full px-4"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button
                  :type="type.status === 'active' ? 'warning' : 'success'"
                  size="small"
                  @click="toggleStatus(type)"
                  class="rounded-full px-4"
                >
                  <el-icon>
                    <component :is="type.status === 'active' ? Close : Check" />
                  </el-icon>
                  {{ type.status === "active" ? "禁用" : "启用" }}
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="deleteDeviceType(type)"
                  class="rounded-full px-4"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 添加/编辑设备类型对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="550px"
      class="rounded-xl overflow-hidden"
    >
      <el-form
        :model="typeForm"
        :rules="rules"
        ref="typeFormRef"
        label-width="100px"
        class="p-4"
      >
        <el-form-item label="类型名称" prop="name">
          <el-input
            v-model="typeForm.name"
            placeholder="请输入类型名称"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item label="类型标识" prop="type">
          <el-input
            v-model="typeForm.type"
            placeholder="请输入类型标识（英文）"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item label="支持传感器" prop="sensors">
          <el-select
            v-model="typeForm.sensors"
            multiple
            placeholder="请选择支持的传感器"
            style="width: 100%"
            class="rounded-lg"
          >
            <el-option label="温度" value="温度" />
            <el-option label="湿度" value="湿度" />
            <el-option label="光照强度" value="光照强度" />
            <el-option label="土壤湿度" value="土壤湿度" />
            <el-option label="土壤温度" value="土壤温度" />
            <el-option label="PM2.5" value="PM2.5" />
            <el-option label="PM10" value="PM10" />
            <el-option label="CO2" value="CO2" />
            <el-option label="风速" value="风速" />
            <el-option label="风向" value="风向" />
          </el-select>
        </el-form-item>
        <el-form-item label="主题颜色" prop="color">
          <el-color-picker
            v-model="typeForm.color"
            show-alpha
            :predefine="predefineColors"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="typeForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
            class="rounded-lg"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex gap-2 justify-end">
          <el-button @click="dialogVisible = false" class="rounded-full px-4">
            取消
          </el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            class="rounded-full px-4"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Cpu,
  Plus,
  Edit,
  Delete,
  Check,
  Close,
  Sunny,
  Cloudy,
  Watermelon,
  Odometer,
  Search,
  Refresh,
} from "@element-plus/icons-vue";
import {
  getDeviceTypeList,
  addDeviceType as addDeviceTypeApi,
  updateDeviceType as updateDeviceTypeApi,
  toggleDeviceTypeStatus,
  deleteDeviceType as deleteDeviceTypeApi,
} from "@/api/auth";

const selectedType = ref(null);
const dialogVisible = ref(false);
const isEdit = ref(false);
const typeFormRef = ref(null);

// 搜索表单
const searchForm = ref({
  name: "",
  status: null,
});

// 设备类型数据
const deviceTypes = ref([]);

// 统计数据
const totalCount = computed(() => deviceTypes.value.length);
const activeCount = computed(
  () => deviceTypes.value.filter((t) => t.status === "active").length,
);
const inactiveCount = computed(
  () => deviceTypes.value.filter((t) => t.status === "inactive").length,
);

// 过滤后的设备类型列表
const filteredTypes = computed(() => {
  let result = deviceTypes.value;
  if (searchForm.value.name) {
    result = result.filter((t) =>
      t.name.toLowerCase().includes(searchForm.value.name.toLowerCase()),
    );
  }
  if (searchForm.value.status) {
    result = result.filter((t) => t.status === searchForm.value.status);
  }
  return result;
});

// 搜索
const handleSearch = () => {
  // 触发计算属性重新计算
};

// 重置搜索
const handleReset = () => {
  searchForm.value.name = "";
  searchForm.value.status = null;
};

// 表单数据
const typeForm = ref({
  id: "",
  name: "",
  type: "",
  sensors: [],
  color: "#409EFF",
  description: "",
  deviceCount: 0,
  status: "active",
});

// 预定义颜色
const predefineColors = [
  "#409EFF",
  "#67C23A",
  "#E6A23C",
  "#F56C6C",
  "#909399",
  "#E0A800",
];

// 对话框标题
const dialogTitle = computed(() =>
  isEdit.value ? "编辑设备类型" : "添加设备类型",
);

// 表单验证规则
const rules = {
  name: [
    { required: true, message: "请输入类型名称", trigger: "blur" },
    { min: 2, max: 20, message: "长度在 2 到 20 个字符", trigger: "blur" },
  ],
  type: [
    { required: true, message: "请输入类型标识", trigger: "blur" },
    {
      pattern: /^[a-z_]+$/,
      message: "只能输入小写字母和下划线",
      trigger: "blur",
    },
  ],
  sensors: [
    {
      required: true,
      message: "请选择支持的传感器",
      trigger: "change",
      type: "array",
      min: 1,
    },
  ],
  color: [{ required: true, message: "请选择主题颜色", trigger: "change" }],
};

// 获取设备类型图标
const getTypeIcon = (type) => {
  const iconMap = {
    temperature_humidity: Odometer,
    light: Sunny,
    soil: Watermelon,
    air: Cloudy,
    wind: Cloudy,
    comprehensive: Cpu,
  };
  return iconMap[type] || Cpu;
};

// 获取设备类型列表
const fetchDeviceTypes = async () => {
  try {
    const response = await getDeviceTypeList();
    if (response.data.code === 1) {
      const data = response.data.data || [];
      deviceTypes.value = data.map((item) => ({
        ...item,
        sensors: parseSensors(item.sensors),
      }));
    } else {
      ElMessage.error(response.data.msg || "获取设备类型列表失败");
    }
  } catch (error) {
    console.error("获取设备类型列表失败", error);
    ElMessage.error("获取设备类型列表失败");
  }
};

// 解析 sensors 字段
const parseSensors = (sensors) => {
  if (!sensors) return [];
  if (Array.isArray(sensors)) return sensors;
  try {
    return JSON.parse(sensors);
  } catch {
    return [];
  }
};

// 添加设备类型
const addDeviceType = () => {
  isEdit.value = false;
  typeForm.value = {
    id: "",
    name: "",
    type: "",
    sensors: [],
    color: "#409EFF",
    description: "",
    deviceCount: 0,
    status: "active",
  };
  dialogVisible.value = true;
};

// 编辑设备类型
const editDeviceType = (type) => {
  isEdit.value = true;
  typeForm.value = {
    ...type,
    sensors: parseSensors(type.sensors),
  };
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!typeFormRef.value) return;

  await typeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          const response = await updateDeviceTypeApi({
            id: typeForm.value.id,
            name: typeForm.value.name,
            type: typeForm.value.type,
            sensors: JSON.stringify(typeForm.value.sensors),
            color: typeForm.value.color,
            description: typeForm.value.description,
          });
          if (response.data.code === 1) {
            ElMessage.success("设备类型更新成功");
            dialogVisible.value = false;
            await fetchDeviceTypes();
          } else {
            ElMessage.error(response.data.msg || "更新失败");
          }
        } else {
          const response = await addDeviceTypeApi({
            name: typeForm.value.name,
            type: typeForm.value.type,
            sensors: JSON.stringify(typeForm.value.sensors),
            color: typeForm.value.color,
            description: typeForm.value.description,
          });
          if (response.data.code === 1) {
            ElMessage.success("设备类型添加成功");
            dialogVisible.value = false;
            await fetchDeviceTypes();
          } else {
            ElMessage.error(response.data.msg || "添加失败");
          }
        }
      } catch (error) {
        console.error("操作失败", error);
        ElMessage.error("操作失败");
      }
    }
  });
};

// 切换状态
const toggleStatus = async (type) => {
  try {
    const response = await toggleDeviceTypeStatus(type.id);
    if (response.data.code === 1) {
      const newStatus = type.status === "active" ? "inactive" : "active";
      type.status = newStatus;
      ElMessage.success(
        `${type.name} 已${newStatus === "active" ? "启用" : "禁用"}`,
      );
    } else {
      ElMessage.error(response.data.msg || "操作失败");
    }
  } catch (error) {
    console.error("切换状态失败", error);
    ElMessage.error("操作失败");
  }
};

// 删除设备类型
const deleteDeviceType = (type) => {
  ElMessageBox.confirm(`确定要删除设备类型 ${type.name} 吗？`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        const response = await deleteDeviceTypeApi(type.id);
        if (response.data.code === 1) {
          const index = deviceTypes.value.findIndex((t) => t.id === type.id);
          if (index > -1) {
            deviceTypes.value.splice(index, 1);
          }
          ElMessage.success("删除成功");
        } else {
          ElMessage.error(response.data.msg || "删除失败");
        }
      } catch (error) {
        console.error("删除设备类型失败", error);
        ElMessage.error("删除失败");
      }
    })
    .catch(() => {});
};

onMounted(() => {
  fetchDeviceTypes();
});
</script>

<style scoped>
.device-type-manager {
  margin: 20px 0;
}

.search-card {
  margin-bottom: 20px;
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
  font-weight: 600;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.device-type-grid {
  margin-top: 20px;
}

.device-type-card {
  transition: all 0.3s;
  cursor: pointer;
}

.device-type-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.device-type-card.active {
  border: 2px solid #67c23a;
}

.device-type-header {
  text-align: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.type-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
}

.device-type-header h3 {
  margin: 0 0 10px;
  color: #2c3e50;
  font-size: 16px;
}

.device-type-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  width: 80px;
  color: #909399;
}

.info-item .value {
  flex: 1;
  color: #606266;
}

.device-type-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

@media (max-width: 768px) {
  .device-type-actions {
    flex-wrap: wrap;
  }

  .device-type-actions .el-button {
    flex: 1;
    margin-bottom: 5px;
  }
}
</style>
