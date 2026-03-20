<template>
  <div class="device-type-manager">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon size="20" color="#67C23A"><Cpu /></el-icon>
          <span>设备类型管理</span>
          <el-button type="primary" size="small" @click="addDeviceType">
            <el-icon><Plus /></el-icon>
            添加设备类型
          </el-button>
        </div>
      </template>

      <div class="device-type-content">
        <el-row :gutter="20" class="device-type-grid">
          <el-col
            v-for="type in deviceTypes"
            :key="type.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            style="margin-bottom: 20px"
          >
            <el-card
              class="device-type-card"
              :class="{ active: selectedType === type.id }"
            >
              <div class="device-type-header">
                <div class="type-icon" :style="{ background: type.color }">
                  <el-icon :size="32" color="#fff">{{
                    getTypeIcon(type.type)
                  }}</el-icon>
                </div>
                <h3>{{ type.name }}</h3>
                <el-tag
                  :type="type.status === 'active' ? 'success' : 'info'"
                  size="small"
                >
                  {{ type.status === "active" ? "启用" : "禁用" }}
                </el-tag>
              </div>

              <div class="device-type-info">
                <div class="info-item">
                  <span class="label">设备类型:</span>
                  <span class="value">{{ type.type }}</span>
                </div>
                <div class="info-item">
                  <span class="label">支持传感器:</span>
                  <span class="value">{{ type.sensors.join(", ") }}</span>
                </div>
                <div class="info-item">
                  <span class="label">设备数量:</span>
                  <span class="value">{{ type.deviceCount }}</span>
                </div>
                <div class="info-item">
                  <span class="label">描述:</span>
                  <span class="value">{{ type.description }}</span>
                </div>
              </div>

              <div class="device-type-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="editDeviceType(type)"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button
                  :type="type.status === 'active' ? 'warning' : 'success'"
                  size="small"
                  @click="toggleStatus(type)"
                >
                  <el-icon>{{
                    type.status === "active" ? "Close" : "Check"
                  }}</el-icon>
                  {{ type.status === "active" ? "禁用" : "启用" }}
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="deleteDeviceType(type)"
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form
        :model="typeForm"
        :rules="rules"
        ref="typeFormRef"
        label-width="100px"
      >
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="typeForm.name" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="类型标识" prop="type">
          <el-input
            v-model="typeForm.type"
            placeholder="请输入类型标识（英文）"
          />
        </el-form-item>
        <el-form-item label="支持传感器" prop="sensors">
          <el-select
            v-model="typeForm.sensors"
            multiple
            placeholder="请选择支持的传感器"
            style="width: 100%"
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
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="typeForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
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

// 设备类型数据
const deviceTypes = ref([
  {
    id: "1",
    name: "温湿度传感器",
    type: "temperature_humidity",
    sensors: ["温度", "湿度"],
    deviceCount: 15,
    status: "active",
    color: "#409EFF",
    description: "监测环境温度和湿度",
  },
  {
    id: "2",
    name: "光照传感器",
    type: "light",
    sensors: ["光照强度"],
    deviceCount: 8,
    status: "active",
    color: "#E6A23C",
    description: "监测环境光照强度",
  },
  {
    id: "3",
    name: "土壤传感器",
    type: "soil",
    sensors: ["土壤湿度", "土壤温度"],
    deviceCount: 12,
    status: "active",
    color: "#67C23A",
    description: "监测土壤湿度和温度",
  },
  {
    id: "4",
    name: "空气质量传感器",
    type: "air",
    sensors: ["PM2.5", "PM10", "CO2"],
    deviceCount: 5,
    status: "active",
    color: "#F56C6C",
    description: "监测空气质量指标",
  },
  {
    id: "5",
    name: "风速传感器",
    type: "wind",
    sensors: ["风速", "风向"],
    deviceCount: 3,
    status: "active",
    color: "#909399",
    description: "监测风速和风向",
  },
  {
    id: "6",
    name: "综合环境监测站",
    type: "comprehensive",
    sensors: ["温度", "湿度", "光照", "土壤湿度", "土壤温度"],
    deviceCount: 2,
    status: "active",
    color: "#E0A800",
    description: "综合监测多种环境参数",
  },
]);

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
      deviceTypes.value = response.data.data || [];
    }
  } catch (error) {
    console.error("获取设备类型列表失败", error);
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
  typeForm.value = { ...type, sensors: [...type.sensors] };
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
          ElMessage.error(response.data.message || "删除失败");
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
