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

    <!-- 操作栏 -->
    <el-card class="operation-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>路灯控制</span>
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Sunny,
  CircleCheck,
  Moon,
  Warning,
  Lightning,
  SwitchButton,
  CircleClose,
  MagicStick,
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
});
</script>

<style scoped>
.lamp-manager {
  padding: 20px;
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
</style>
