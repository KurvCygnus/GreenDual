<template>
  <div class="device-manager">
    <!-- 统计卡片 -->
    <div class="stats-overview">
      <div class="stat-item">
        <div
          class="stat-icon"
          style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
        >
          <img
            src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20smart%20campus%20devices%20dashboard%20icon&image_size=square"
            alt="设备总数"
            style="width: 28px; height: 28px; object-fit: contain"
          />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ totalDevices }}</div>
          <div class="stat-label">设备总数</div>
        </div>
      </div>

      <div class="stat-item">
        <div
          class="stat-icon"
          style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)"
        >
          <img
            src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=green%20online%20status%20icon%20for%20smart%20devices&image_size=square"
            alt="在线设备"
            style="width: 28px; height: 28px; object-fit: contain"
          />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ onlineDevices }}</div>
          <div class="stat-label">在线设备</div>
        </div>
      </div>

      <div class="stat-item">
        <div
          class="stat-icon"
          style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)"
        >
          <img
            src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20street%20light%20icon%20on%20campus&image_size=square"
            alt="智慧路灯"
            style="width: 28px; height: 28px; object-fit: contain"
          />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ lightingDevices }}</div>
          <div class="stat-label">智慧路灯</div>
        </div>
      </div>

      <div class="stat-item">
        <div
          class="stat-icon"
          style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)"
        >
          <img
            src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20agriculture%20greenhouse%20icon&image_size=square"
            alt="智慧农业"
            style="width: 28px; height: 28px; object-fit: contain"
          />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ agricultureDevices }}</div>
          <div class="stat-label">智慧农业</div>
        </div>
      </div>
    </div>

    <!-- 设备列表 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">
          <el-icon :size="20" color="#409EFF"><Cpu /></el-icon>
          <span>设备列表</span>
        </div>
        <div class="section-actions">
          <el-select
            v-model="deviceTypeFilter"
            placeholder="筛选设备类型"
            style="width: 140px; margin-right: 12px"
            size="small"
            clearable
            @change="handleDeviceTypeFilterChange"
          >
            <el-option label="全部设备" value="" />
            <el-option label="智慧路灯" value="智慧路灯" />
            <el-option label="农业大棚（定制服务）" value="农业大棚" />
          </el-select>
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="card">卡片视图</el-radio-button>
            <el-radio-button label="table">表格视图</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 卡片视图 -->
      <div v-if="viewMode === 'card'" class="device-grid">
        <div
          v-for="device in filteredDevices"
          :key="device.id"
          class="device-card"
          :class="{ offline: !device.onLine }"
        >
          <div class="device-header">
            <div
              class="device-icon"
              :class="getDeviceIconClass(device.deviceType)"
            >
              <img
                v-if="device.deviceType === '智慧路灯'"
                :src="'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20street%20light%20on%20campus%20road&image_size=square'"
                :alt="device.deviceType"
                style="width: 24px; height: 24px; object-fit: contain"
              />
              <img
                v-else
                :src="'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20agriculture%20greenhouse%20with%20sensors&image_size=square'"
                :alt="device.deviceType"
                style="width: 24px; height: 24px; object-fit: contain"
              />
            </div>
            <div class="device-info">
              <div class="device-name">{{ device.name }}</div>
              <div class="device-type">
                {{ getDeviceTypeText(device.deviceType) }}
              </div>
            </div>
            <el-tag :type="device.onLine ? 'success' : 'info'" size="small">
              {{ device.onLine ? "在线" : "离线" }}
            </el-tag>
          </div>

          <div class="device-data">
            <div v-if="device.temperature !== undefined" class="data-row">
              <el-icon><MostlyCloudy /></el-icon>
              <span>温度: {{ device.temperature }}°C</span>
            </div>
            <div v-if="device.humidity !== undefined" class="data-row">
              <el-icon><Watermelon /></el-icon>
              <span>湿度: {{ device.humidity }}%</span>
            </div>
            <div v-if="device.lightIntensity !== undefined" class="data-row">
              <el-icon><Sunny /></el-icon>
              <span>光照: {{ device.lightIntensity }}lux</span>
            </div>
            <div v-if="device.brightness !== undefined" class="data-row">
              <el-icon><Sunny /></el-icon>
              <span>亮度: {{ device.brightness }}%</span>
            </div>
            <div v-if="device.soilTemperature !== undefined" class="data-row">
              <el-icon><TrendCharts /></el-icon>
              <span>土壤温度: {{ device.soilTemperature }}°C</span>
            </div>
            <div v-if="device.soilHumidity !== undefined" class="data-row">
              <el-icon><Watermelon /></el-icon>
              <span>土壤湿度: {{ device.soilHumidity }}%</span>
            </div>
          </div>

          <div class="device-actions">
            <el-button size="small" @click="viewDetail(device)">详情</el-button>
            <el-button size="small" type="primary" @click="editDevice(device)"
              >编辑</el-button
            >
            <el-button size="small" type="danger" @click="deleteDevice(device)"
              >删除</el-button
            >
          </div>
        </div>
      </div>

      <!-- 表格视图 -->
      <el-table
        v-else
        :data="filteredDevices"
        style="width: 100%"
        class="device-table"
      >
        <el-table-column prop="id" label="设备ID" width="120" />
        <el-table-column prop="name" label="设备名称" />
        <el-table-column label="设备类型">
          <template #default="scope">
            <el-tag :type="getDeviceTypeTagType(scope.row.deviceType)">
              {{ getDeviceTypeText(scope.row.deviceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="temperature" label="温度(°C)" width="100" />
        <el-table-column prop="humidity" label="湿度(%)" width="100" />
        <el-table-column prop="lightIntensity" label="光照(lux)" width="120" />
        <el-table-column prop="brightness" label="亮度(%)" width="100" />
        <el-table-column prop="onLine" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.onLine ? 'success' : 'info'">
              {{ scope.row.onLine ? "在线" : "离线" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="editDevice(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteDevice(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加设备对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加设备"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="120px"
      >
        <el-form-item label="设备ID" prop="id">
          <el-input
            v-model="addForm.id"
            placeholder="请输入设备ID，如：ENV001"
          />
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="addForm.deviceType"
            placeholder="请选择设备类型"
            style="width: 100%"
            @change="handleDeviceTypeChange"
          >
            <el-option label="智慧路灯" value="智慧路灯" />
            <el-option label="农业大棚（定制服务）" value="农业大棚" />
          </el-select>
        </el-form-item>
        <el-form-item label="扩展板类型" prop="extensionType">
          <el-select
            v-model="addForm.extensionType"
            placeholder="请选择扩展板类型"
            style="width: 100%"
          >
            <el-option
              v-if="addForm.deviceType === '智慧路灯'"
              label="智慧路灯扩展板"
              value="lighting"
            />
            <el-option
              v-if="addForm.deviceType === '农业大棚'"
              label="智慧农业扩展板"
              value="agriculture"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址" prop="ip">
          <el-input
            v-model="addForm.ip"
            placeholder="请输入IP地址，如：192.168.1.100"
          />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="addForm.port" :min="1" :max="65535" />
        </el-form-item>

        <!-- 基础传感器数据 -->
        <el-form-item label="温度(°C)" prop="temperature">
          <el-input-number
            v-model="addForm.temperature"
            :min="-50"
            :max="100"
            :precision="1"
          />
        </el-form-item>
        <el-form-item label="湿度(%)" prop="humidity">
          <el-input-number v-model="addForm.humidity" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="光照(lux)" prop="lightIntensity">
          <el-input-number
            v-model="addForm.lightIntensity"
            :min="0"
            :max="100000"
          />
        </el-form-item>

        <!-- 智慧路灯扩展板数据 -->
        <el-form-item
          v-if="addForm.extensionType === 'lighting'"
          label="亮度(%)"
          prop="brightness"
        >
          <el-input-number v-model="addForm.brightness" :min="0" :max="100" />
        </el-form-item>
        <el-form-item
          v-if="addForm.extensionType === 'lighting'"
          label="路灯状态"
          prop="lampStatus"
        >
          <el-switch
            v-model="addForm.lampStatus"
            active-text="开启"
            inactive-text="关闭"
          />
        </el-form-item>

        <!-- 智慧农业扩展板数据 -->
        <el-form-item
          v-if="addForm.extensionType === 'agriculture'"
          label="土壤温度(°C)"
          prop="soilTemperature"
        >
          <el-input-number
            v-model="addForm.soilTemperature"
            :min="-50"
            :max="100"
            :precision="1"
          />
        </el-form-item>
        <el-form-item
          v-if="addForm.extensionType === 'agriculture'"
          label="土壤湿度(%)"
          prop="soilHumidity"
        >
          <el-input-number v-model="addForm.soilHumidity" :min="0" :max="100" />
        </el-form-item>

        <el-form-item label="在线状态" prop="onLine">
          <el-switch
            v-model="addForm.onLine"
            active-text="在线"
            inactive-text="离线"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitAddDevice"
          :loading="addLoading"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看设备详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="设备详情" width="600px">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="设备ID">{{
          detailDevice.id
        }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{
          detailDevice.name
        }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{
          getDeviceTypeText(detailDevice.deviceType)
        }}</el-descriptions-item>
        <el-descriptions-item label="扩展板类型">{{
          getExtensionTypeText(detailDevice.extensionType)
        }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{
          detailDevice.ip
        }}</el-descriptions-item>
        <el-descriptions-item label="端口">{{
          detailDevice.port
        }}</el-descriptions-item>
        <el-descriptions-item label="在线状态">
          <el-tag :type="detailDevice.onLine ? 'success' : 'info'">
            {{ detailDevice.onLine ? "在线" : "离线" }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDevice.temperature" label="温度">
          {{ detailDevice.temperature }}°C
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDevice.humidity" label="湿度">
          {{ detailDevice.humidity }}%
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDevice.lightIntensity" label="光照">
          {{ detailDevice.lightIntensity }}lux
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDevice.brightness" label="亮度">
          {{ detailDevice.brightness }}%
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDevice.lampStatus" label="路灯状态">
          <el-tag :type="detailDevice.lampStatus ? 'success' : 'info'">
            {{ detailDevice.lampStatus ? "开启" : "关闭" }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item
          v-if="detailDevice.soilTemperature"
          label="土壤温度"
        >
          {{ detailDevice.soilTemperature }}°C
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDevice.soilHumidity" label="土壤湿度">
          {{ detailDevice.soilHumidity }}%
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailDialogVisible = false"
          >关闭</el-button
        >
      </template>
    </el-dialog>

    <!-- 编辑设备对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑设备"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="addFormRules"
        label-width="120px"
      >
        <el-form-item label="设备ID">
          <el-input v-model="editForm.id" disabled />
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="editForm.deviceType"
            placeholder="请选择设备类型"
            style="width: 100%"
            @change="handleDeviceTypeChange"
          >
            <el-option label="智慧路灯" value="智慧路灯" />
            <el-option label="农业大棚（定制服务）" value="农业大棚" />
          </el-select>
        </el-form-item>
        <el-form-item label="扩展板类型" prop="extensionType">
          <el-select
            v-model="editForm.extensionType"
            placeholder="请选择扩展板类型"
            style="width: 100%"
          >
            <el-option
              v-if="editForm.deviceType === '智慧路灯'"
              label="智慧路灯扩展板"
              value="lighting"
            />
            <el-option
              v-if="editForm.deviceType === '农业大棚'"
              label="智慧农业扩展板"
              value="agriculture"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址" prop="ip">
          <el-input v-model="editForm.ip" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="editForm.port" :min="1" :max="65535" />
        </el-form-item>

        <!-- 基础传感器数据 -->
        <el-form-item label="温度(°C)" prop="temperature">
          <el-input-number
            v-model="editForm.temperature"
            :min="-50"
            :max="100"
            :precision="1"
          />
        </el-form-item>
        <el-form-item label="湿度(%)" prop="humidity">
          <el-input-number v-model="editForm.humidity" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="光照(lux)" prop="lightIntensity">
          <el-input-number
            v-model="editForm.lightIntensity"
            :min="0"
            :max="100000"
          />
        </el-form-item>

        <!-- 智慧路灯扩展板数据 -->
        <el-form-item
          v-if="editForm.extensionType === 'lighting'"
          label="亮度(%)"
          prop="brightness"
        >
          <el-input-number v-model="editForm.brightness" :min="0" :max="100" />
        </el-form-item>
        <el-form-item
          v-if="editForm.extensionType === 'lighting'"
          label="路灯状态"
          prop="lampStatus"
        >
          <el-switch
            v-model="editForm.lampStatus"
            active-text="开启"
            inactive-text="关闭"
          />
        </el-form-item>

        <!-- 智慧农业扩展板数据 -->
        <el-form-item
          v-if="editForm.extensionType === 'agriculture'"
          label="土壤温度(°C)"
          prop="soilTemperature"
        >
          <el-input-number
            v-model="editForm.soilTemperature"
            :min="-50"
            :max="100"
            :precision="1"
          />
        </el-form-item>
        <el-form-item
          v-if="editForm.extensionType === 'agriculture'"
          label="土壤湿度(%)"
          prop="soilHumidity"
        >
          <el-input-number
            v-model="editForm.soilHumidity"
            :min="0"
            :max="100"
          />
        </el-form-item>

        <el-form-item label="在线状态" prop="onLine">
          <el-switch
            v-model="editForm.onLine"
            active-text="在线"
            inactive-text="离线"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitEditDevice"
          :loading="editLoading"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Search,
  Plus,
  Refresh,
  Cpu,
  Sunny,
  Watermelon,
  MostlyCloudy,
  TrendCharts,
} from "@element-plus/icons-vue";

import {
  getDeviceList,
  deleteDevice as deleteDeviceApi,
  addDevice as addDeviceApi,
  updateDevice as updateDeviceApi,
} from "../../api/auth.js";

// 状态
const searchKeyword = ref("");
const viewMode = ref("card");
const deviceTypeFilter = ref("");

// 设备列表
const deviceList = ref([]);

// 统计数据
const totalDevices = computed(() => deviceList.value.length);
const onlineDevices = computed(
  () => deviceList.value.filter((d) => d.onLine).length,
);
const lightingDevices = computed(
  () => deviceList.value.filter((d) => d.deviceType === "智慧路灯").length,
);
const agricultureDevices = computed(
  () => deviceList.value.filter((d) => d.deviceType === "农业大棚").length,
);

// 过滤后的设备列表
const filteredDevices = computed(() => {
  let result = deviceList.value;

  // 按设备类型筛选
  if (deviceTypeFilter.value) {
    result = result.filter(
      (device) => device.deviceType === deviceTypeFilter.value,
    );
  }

  // 按搜索关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter((device) => {
      // 确保device对象存在且属性有效
      if (!device) return false;
      return (
        (device.name && device.name.toLowerCase().includes(keyword)) ||
        (device.id && device.id.toLowerCase().includes(keyword)) ||
        (device.deviceType && device.deviceType.toLowerCase().includes(keyword))
      );
    });
  }

  return result;
});

// 添加设备对话框
const addDialogVisible = ref(false);
const addLoading = ref(false);
const addFormRef = ref(null);
const addForm = ref({
  id: "",
  name: "",
  deviceType: "智慧路灯",
  extensionType: "lighting",
  ip: "192.168.1.100",
  port: 8080,
  temperature: 25.0,
  humidity: 50,
  lightIntensity: 500,
  brightness: 50,
  lampStatus: true,
  soilTemperature: 20.0,
  soilHumidity: 40,
  onLine: true,
});

const addFormRules = {
  id: [
    { required: true, message: "请输入设备ID", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 20 个字符", trigger: "blur" },
  ],
  name: [
    { required: true, message: "请输入设备名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
  ],
  deviceType: [
    { required: true, message: "请选择设备类型", trigger: "change" },
  ],
  extensionType: [
    { required: true, message: "请选择扩展板类型", trigger: "change" },
  ],
  ip: [
    { required: true, message: "请输入IP地址", trigger: "blur" },
    {
      pattern: /^(\d{1,3}\.){3}\d{1,3}$/,
      message: "IP地址格式不正确",
      trigger: "blur",
    },
  ],
  port: [{ required: true, message: "请输入端口", trigger: "blur" }],
};

// 查看设备详情
const detailDialogVisible = ref(false);
const detailDevice = ref({});

// 编辑设备
const editDialogVisible = ref(false);
const editLoading = ref(false);
const editFormRef = ref(null);
const editForm = ref({
  id: "",
  name: "",
  deviceType: "环境监测",
  extensionType: "basic",
  ip: "192.168.1.100",
  port: 8080,
  temperature: 25.0,
  humidity: 50,
  lightIntensity: 500,
  brightness: 50,
  lampStatus: true,
  soilTemperature: 20.0,
  soilHumidity: 40,
  onLine: true,
});

// 方法
const handleAdd = () => {
  addForm.value = {
    id: "",
    name: "",
    deviceType: "智慧路灯",
    extensionType: "lighting",
    ip: "192.168.1.100",
    port: 8080,
    temperature: 25.0,
    humidity: 50,
    lightIntensity: 500,
    brightness: 50,
    lampStatus: true,
    soilTemperature: 20.0,
    soilHumidity: 40,
    onLine: true,
  };
  addDialogVisible.value = true;
};

const handleDeviceTypeChange = (type) => {
  if (type === "智慧路灯") {
    addForm.value.extensionType = "lighting";
  } else if (type === "农业大棚") {
    addForm.value.extensionType = "agriculture";
  }
};

const submitAddDevice = async () => {
  if (!addFormRef.value) return;

  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      addLoading.value = true;
      try {
        // 转换数据格式以适配后端
        const deviceData = {
          ...addForm.value,
          onLine: addForm.value.onLine ? 1 : 0,
          lampStatus: addForm.value.lampStatus ? 1 : 0,
        };
        const response = await addDeviceApi(deviceData);
        if (response.data.code === 1) {
          ElMessage.success("设备添加成功");
          addDialogVisible.value = false;
          await refreshData();
        } else {
          ElMessage.error(response.data.msg || "添加失败");
        }
      } catch (error) {
        console.error("添加设备失败", error);
        ElMessage.error("添加设备失败");
      } finally {
        addLoading.value = false;
      }
    }
  });
};

const handleSearch = () => {
  refreshData();
};

const handleDeviceTypeFilterChange = () => {
  // 设备类型筛选变化时，filteredDevices会自动更新
  // 这里可以添加额外的逻辑，比如记录筛选日志等
};

const refreshData = async () => {
  try {
    const listResponse = await getDeviceList({
      match: searchKeyword.value,
      offset: 0,
      pageSize: 100,
      sort: "asc",
    });
    if (listResponse.data.code === 1) {
      deviceList.value = listResponse.data.data.map((device) => ({
        ...device,
        onLine: device.onLine === 1 || device.onLine === true,
        lampStatus: device.lampStatus === 1 || device.lampStatus === true,
      }));
    }
    // 如果没有数据，添加模拟数据
    if (deviceList.value.length === 0) {
      deviceList.value = getMockDevices();
    }
    ElMessage.success("数据已刷新");
  } catch (error) {
    ElMessage.error("数据刷新失败，使用本地数据");
    console.error(error);
    // 使用模拟数据
    deviceList.value = getMockDevices();
  }
};

// 模拟设备数据 - 校园场景
const getMockDevices = () => {
  return [
    // 智慧路灯设备
    {
      id: "LAMP001",
      name: "图书馆主路灯01",
      deviceType: "智慧路灯",
      extensionType: "lighting",
      ip: "192.168.1.101",
      port: 8080,
      onLine: true,
      temperature: 26,
      humidity: 65,
      lightIntensity: 850,
      brightness: 80,
      lampStatus: true,
      location: "图书馆正门",
      installTime: "2024-09-01"
    },
    {
      id: "LAMP002",
      name: "图书馆主路灯02",
      deviceType: "智慧路灯",
      extensionType: "lighting",
      ip: "192.168.1.102",
      port: 8080,
      onLine: true,
      temperature: 25,
      humidity: 68,
      lightIntensity: 920,
      brightness: 75,
      lampStatus: true,
      location: "图书馆侧门",
      installTime: "2024-09-01"
    },
    {
      id: "LAMP003",
      name: "教学楼A区路灯01",
      deviceType: "智慧路灯",
      extensionType: "lighting",
      ip: "192.168.1.103",
      port: 8080,
      onLine: false,
      temperature: 24,
      humidity: 70,
      lightIntensity: 1200,
      brightness: 0,
      lampStatus: false,
      location: "教学楼A区广场",
      installTime: "2024-08-15"
    },
    {
      id: "LAMP004",
      name: "教学楼B区路灯01",
      deviceType: "智慧路灯",
      extensionType: "lighting",
      ip: "192.168.1.104",
      port: 8080,
      onLine: true,
      temperature: 27,
      humidity: 62,
      lightIntensity: 680,
      brightness: 90,
      lampStatus: true,
      location: "教学楼B区入口",
      installTime: "2024-08-15"
    },
    {
      id: "LAMP005",
      name: "学生宿舍1号楼路灯",
      deviceType: "智慧路灯",
      extensionType: "lighting",
      ip: "192.168.1.105",
      port: 8080,
      onLine: true,
      temperature: 23,
      humidity: 72,
      lightIntensity: 450,
      brightness: 85,
      lampStatus: true,
      location: "学生宿舍1号楼前",
      installTime: "2024-07-20"
    },
    {
      id: "LAMP006",
      name: "学生宿舍2号楼路灯",
      deviceType: "智慧路灯",
      extensionType: "lighting",
      ip: "192.168.1.106",
      port: 8080,
      onLine: true,
      temperature: 24,
      humidity: 69,
      lightIntensity: 520,
      brightness: 80,
      lampStatus: true,
      location: "学生宿舍2号楼前",
      installTime: "2024-07-20"
    },
    {
      id: "LAMP007",
      name: "体育馆周边路灯01",
      deviceType: "智慧路灯",
      extensionType: "lighting",
      ip: "192.168.1.107",
      port: 8080,
      onLine: true,
      temperature: 28,
      humidity: 58,
      lightIntensity: 1500,
      brightness: 60,
      lampStatus: true,
      location: "体育馆北侧",
      installTime: "2024-06-10"
    },
    {
      id: "LAMP008",
      name: "食堂前路灯",
      deviceType: "智慧路灯",
      extensionType: "lighting",
      ip: "192.168.1.108",
      port: 8080,
      onLine: true,
      temperature: 29,
      humidity: 75,
      lightIntensity: 380,
      brightness: 95,
      lampStatus: true,
      location: "食堂正门前",
      installTime: "2024-06-10"
    },
    // 生态监测设备（绿植区域）
    {
      id: "ECO001",
      name: "图书馆绿植区监测站",
      deviceType: "生态监测",
      extensionType: "agriculture",
      ip: "192.168.1.201",
      port: 8080,
      onLine: true,
      temperature: 24,
      humidity: 65,
      lightIntensity: 850,
      soilTemperature: 22,
      soilHumidity: 58,
      location: "图书馆室内绿植区",
      installTime: "2024-09-15"
    },
    {
      id: "ECO002",
      name: "教学楼A区花园监测站",
      deviceType: "生态监测",
      extensionType: "agriculture",
      ip: "192.168.1.202",
      port: 8080,
      onLine: true,
      temperature: 26,
      humidity: 70,
      lightIntensity: 1200,
      soilTemperature: 23,
      soilHumidity: 45,
      location: "教学楼A区后花园",
      installTime: "2024-09-15"
    },
    {
      id: "ECO003",
      name: "行政楼景观带监测站",
      deviceType: "生态监测",
      extensionType: "agriculture",
      ip: "192.168.1.203",
      port: 8080,
      onLine: true,
      temperature: 25,
      humidity: 62,
      lightIntensity: 680,
      soilTemperature: 21,
      soilHumidity: 72,
      location: "行政楼前景观带",
      installTime: "2024-08-20"
    },
    {
      id: "ECO004",
      name: "学生宿舍绿化带监测站",
      deviceType: "生态监测",
      extensionType: "agriculture",
      ip: "192.168.1.204",
      port: 8080,
      onLine: true,
      temperature: 23,
      humidity: 68,
      lightIntensity: 920,
      soilTemperature: 20,
      soilHumidity: 55,
      location: "学生宿舍区绿化带",
      installTime: "2024-08-20"
    },
    {
      id: "ECO005",
      name: "体育馆周边绿植监测站",
      deviceType: "生态监测",
      extensionType: "agriculture",
      ip: "192.168.1.205",
      port: 8080,
      onLine: false,
      temperature: 28,
      humidity: 55,
      lightIntensity: 1500,
      soilTemperature: 25,
      soilHumidity: 38,
      location: "体育馆周边绿化带",
      installTime: "2024-07-15"
    },
    {
      id: "ECO006",
      name: "食堂景观植物监测站",
      deviceType: "生态监测",
      extensionType: "agriculture",
      ip: "192.168.1.206",
      port: 8080,
      onLine: true,
      temperature: 27,
      humidity: 75,
      lightIntensity: 450,
      soilTemperature: 24,
      soilHumidity: 80,
      location: "食堂室内景观区",
      installTime: "2024-07-15"
    }
  ];
};

const viewDetail = (device) => {
  detailDevice.value = { ...device };
  detailDialogVisible.value = true;
};

const editDevice = (device) => {
  editForm.value = {
    id: device.id,
    name: device.name,
    deviceType: device.deviceType || "环境监测",
    extensionType: device.extensionType || "basic",
    ip: device.ip || "192.168.1.100",
    port: device.port || 8080,
    temperature: device.temperature || 25.0,
    humidity: device.humidity || 50,
    lightIntensity: device.lightIntensity || 500,
    brightness: device.brightness || 50,
    lampStatus: device.lampStatus === 1 || device.lampStatus === true,
    soilTemperature: device.soilTemperature || 20.0,
    soilHumidity: device.soilHumidity || 40,
    onLine: device.onLine === 1 || device.onLine === true,
  };
  editDialogVisible.value = true;
};

const submitEditDevice = async () => {
  if (!editFormRef.value) return;

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true;
      try {
        const deviceData = {
          ...editForm.value,
          onLine: editForm.value.onLine ? 1 : 0,
          lampStatus: editForm.value.lampStatus ? 1 : 0,
        };
        const response = await updateDeviceApi(deviceData);
        if (response.data.code === 1) {
          ElMessage.success("设备更新成功");
          editDialogVisible.value = false;
          await refreshData();
        } else {
          ElMessage.error(response.data.msg || "更新失败");
        }
      } catch (error) {
        console.error("更新设备失败", error);
        ElMessage.error("更新设备失败");
      } finally {
        editLoading.value = false;
      }
    }
  });
};

const deleteDevice = async (device) => {
  ElMessageBox.confirm(`确定要删除设备 ${device.name} 吗？`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      const response = await deleteDeviceApi([device.id]);
      if (response.data.code === 1) {
        ElMessage.success("删除成功");
        await refreshData();
      } else {
        ElMessage.error(response.data.msg || "删除失败");
      }
    } catch (error) {
      ElMessage.error("删除失败");
      console.error(error);
    }
  });
};

// 辅助方法
const getDeviceIcon = (deviceType) => {
  switch (deviceType) {
    case "智慧路灯":
      return Sunny;
    case "农业大棚":
      return Watermelon;
    default:
      return Cpu;
  }
};

const getDeviceIconClass = (deviceType) => {
  switch (deviceType) {
    case "智慧路灯":
      return "icon-lighting";
    case "农业大棚":
      return "icon-agriculture";
    default:
      return "icon-basic";
  }
};

const getDeviceTypeText = (deviceType) => {
  return deviceType || "未知类型";
};

const getDeviceTypeTagType = (deviceType) => {
  switch (deviceType) {
    case "智慧路灯":
      return "warning";
    case "农业大棚":
      return "success";
    default:
      return "primary";
  }
};

const getExtensionTypeText = (extensionType) => {
  switch (extensionType) {
    case "lighting":
      return "智慧路灯扩展板";
    case "agriculture":
      return "智慧农业扩展板";
    case "basic":
      return "基础监测扩展板";
    default:
      return "未知扩展板";
  }
};

onMounted(async () => {
  await refreshData();
});
</script>

<style scoped>
.device-manager {
  padding: 0;
  background: transparent;
  min-height: auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 0 10px;
}

.header-left h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.subtitle {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  border-radius: 20px !important;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.search-input:hover {
  border-color: #38bdf8;
  box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.1);
}

.add-btn,
.refresh-btn {
  border-radius: 20px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(148, 163, 184, 0.3);
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-item {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border-color: #38bdf8;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-item:hover .stat-icon {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.stat-item:hover .stat-value {
  color: #38bdf8;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.section-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.section-actions {
  display: flex;
  gap: 10px;
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding: 24px;
}

.device-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.device-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
  border-color: #38bdf8;
}

.device-card.offline {
  opacity: 0.7;
  border-color: #e2e8f0;
}

.device-card.offline:hover {
  border-color: #f87171;
  box-shadow: 0 12px 30px rgba(248, 113, 113, 0.1);
}

.device-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.device-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  transition: all 0.3s ease;
}

.icon-lighting {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.icon-agriculture {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.icon-basic {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.device-type {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.device-data {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.data-row {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #64748b;
  font-weight: 500;
  font-size: 14px;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.data-row:last-child {
  border-bottom: none;
}

.data-row el-icon {
  color: #38bdf8;
  font-size: 18px;
}

.device-actions {
  display: flex;
  gap: 10px;
  padding: 15px 20px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}

.device-actions .el-button {
  flex: 1;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.device-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.device-table {
  border-radius: 12px;
  overflow: hidden;
}

.device-table .el-table__header-wrapper {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.device-table .el-table__body-wrapper {
  background: rgba(255, 255, 255, 0.95);
}

@media (max-width: 1200px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }

  .device-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }

  .device-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .section-actions {
    width: 100%;
    justify-content: space-between;
  }

  .device-actions {
    flex-wrap: wrap;
  }

  .device-actions .el-button {
    flex: 1;
    margin-bottom: 8px;
  }
}
</style>
