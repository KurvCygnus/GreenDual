<template>
  <div class="lamp-map-container">
    <!-- 地图容器 -->
    <div ref="mapContainer" class="map-container">
      <div v-if="!mapLoaded" class="map-loading">
        <el-skeleton :rows="10" animated />
      </div>
    </div>

    <!-- 地图控制按钮 -->
    <div class="map-controls">
      <el-button type="primary" size="small" @click="resetView">
        <el-icon><Refresh /></el-icon>
        重置视图
      </el-button>
      <el-button type="success" size="small" @click="showAllLamps">
        <el-icon><View /></el-icon>
        显示全部
      </el-button>
    </div>

    <!-- 图例 -->
    <div class="map-legend">
      <div class="legend-item">
        <span class="legend-icon online"></span>
        <span>在线</span>
      </div>
      <div class="legend-item">
        <span class="legend-icon offline"></span>
        <span>离线</span>
      </div>
      <div class="legend-item">
        <span class="legend-icon current"></span>
        <span>当前选中</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from "vue";
import { Refresh, View } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const props = defineProps({
  lamps: {
    type: Array,
    default: () => [],
  },
  currentLamp: {
    type: Object,
    default: null,
  },
});

const mapContainer = ref(null);
const mapLoaded = ref(false);
let map = null;
let markers = [];
let AMap = null;

// 高德地图Key
const AMAP_KEY = "21d80a7334cb846ee23029681f541a06";

// 初始化地图
const initMap = () => {
  if (!mapContainer.value || !AMap) return;

  try {
    // 计算中心点
    const validLamps = getValidLamps();
    let center = [114.4212, 30.5086]; // 默认武汉文华学院光谷创业街附近 [lng, lat]

    if (props.currentLamp && hasValidCoords(props.currentLamp)) {
      center = [
        parseFloat(props.currentLamp.longitude),
        parseFloat(props.currentLamp.latitude),
      ];
    } else if (validLamps.length > 0) {
      center = [
        parseFloat(validLamps[0].longitude),
        parseFloat(validLamps[0].latitude),
      ];
    }

    // 创建地图
    map = new AMap.Map(mapContainer.value, {
      zoom: 13,
      center: center,
      viewMode: "2D",
      mapStyle: "amap://styles/normal",
    });

    // 添加工具栏
    AMap.plugin(["AMap.ToolBar", "AMap.Scale"], () => {
      map.addControl(new AMap.ToolBar());
      map.addControl(new AMap.Scale());
    });

    mapLoaded.value = true;
    addMarkers();
  } catch (error) {
    console.error("地图初始化失败:", error);
    ElMessage.error("地图初始化失败");
  }
};

// 获取有效坐标的路灯
const getValidLamps = () => {
  return props.lamps.filter(hasValidCoords);
};

// 检查是否有有效坐标
const hasValidCoords = (lamp) => {
  return (
    lamp &&
    lamp.longitude &&
    lamp.latitude &&
    !isNaN(parseFloat(lamp.longitude)) &&
    !isNaN(parseFloat(lamp.latitude)) &&
    parseFloat(lamp.longitude) !== 0 &&
    parseFloat(lamp.latitude) !== 0
  );
};

// 添加标记点
const addMarkers = () => {
  if (!map || !AMap) return;

  // 清除现有标记
  markers.forEach((marker) => map.remove(marker));
  markers = [];

  const validLamps = getValidLamps();

  if (validLamps.length === 0) {
    return;
  }

  const markerBounds = [];

  validLamps.forEach((lamp) => {
    const lng = parseFloat(lamp.longitude);
    const lat = parseFloat(lamp.latitude);

    // 确定标记样式
    const isCurrent = props.currentLamp && props.currentLamp.id === lamp.id;
    const isOnline = lamp.onLine === 1 || lamp.onLine === true;

    // 创建自定义图标
    const icon = createCustomIcon(isOnline, isCurrent);

    // 创建标记
    const marker = new AMap.Marker({
      position: [lng, lat],
      icon: icon,
      title: lamp.name,
    });

    // 创建信息窗体
    const infoWindow = createInfoWindow(lamp);
    marker.on("click", () => {
      infoWindow.open(map, marker.getPosition());
    });

    // 如果是当前选中的路灯，自动打开信息窗体
    if (isCurrent) {
      map.setCenter([lng, lat]);
      map.setZoom(15);
      setTimeout(() => {
        infoWindow.open(map, marker.getPosition());
      }, 300);
    }

    markerBounds.push([lng, lat]);
    markers.push(marker);
  });

  // 添加所有标记到地图
  map.add(markers);

  // 如果没有选中特定路灯，自适应视图
  if (!props.currentLamp && markerBounds.length > 0) {
    map.setFitView(markers, false, [50, 50, 50, 50]);
  }
};

// 创建自定义图标
const createCustomIcon = (isOnline, isCurrent) => {
  const color = isCurrent ? "#409EFF" : isOnline ? "#67C23A" : "#909399";
  const size = isCurrent ? 32 : 28;

  return new AMap.Icon({
    image: `data:image/svg+xml;charset=utf-8,${encodeURIComponent(`
      <svg width="${size}" height="${size}" viewBox="0 0 24 24" style="filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));">
        <path fill="${color}" d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
        <circle cx="12" cy="9" r="2.5" fill="white"/>
      </svg>
    `)}`,
    size: new AMap.Size(size, size),
    imageSize: new AMap.Size(size, size),
  });
};

// 创建信息窗体
const createInfoWindow = (lamp) => {
  const statusColor = lamp.onLine ? "#67c23a" : "#909399";
  const statusText = lamp.onLine ? "在线" : "离线";
  const lampStatus = lamp.lampStatus ? "开启" : "关闭";
  const faultStatus = lamp.faultStatus || lamp.isBroken ? "故障" : "正常";
  const faultColor = lamp.faultStatus || lamp.isBroken ? "#f56c6c" : "#67c23a";

  const content = `
    <div style="min-width: 200px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; padding: 10px;">
      <h4 style="margin: 0 0 10px 0; color: #303133; font-size: 15px; border-bottom: 1px solid #ebeef5; padding-bottom: 8px;">
        ${lamp.name}
      </h4>
      <div style="margin: 6px 0; color: #606266; font-size: 13px;">
        <span style="color: #909399;">状态:</span>
        <span style="color: ${statusColor}; font-weight: 500;">${statusText}</span>
      </div>
      <div style="margin: 6px 0; color: #606266; font-size: 13px;">
        <span style="color: #909399;">开关:</span> ${lampStatus}
      </div>
      <div style="margin: 6px 0; color: #606266; font-size: 13px;">
        <span style="color: #909399;">亮度:</span> ${lamp.brightness || 0}%
      </div>
      <div style="margin: 6px 0; color: #606266; font-size: 13px;">
        <span style="color: #909399;">故障:</span>
        <span style="color: ${faultColor};">${faultStatus}</span>
      </div>
      <div style="margin: 6px 0; color: #606266; font-size: 13px;">
        <span style="color: #909399;">IP:</span> ${lamp.ip || "--"}
      </div>
      <div style="margin: 6px 0; color: #606266; font-size: 13px;">
        <span style="color: #909399;">位置:</span> 湖北省武汉市文华学院光谷创业街附近
      </div>
      <div style="margin: 6px 0; color: #606266; font-size: 12px;">
        <span style="color: #909399;">坐标:</span> ${lamp.longitude}, ${lamp.latitude}
      </div>
    </div>
  `;

  return new AMap.InfoWindow({
    content: content,
    offset: new AMap.Pixel(0, -28),
  });
};

// 重置视图
const resetView = () => {
  if (!map) return;
  const defaultCenter = [114.4212, 30.5086]; // 武汉文华学院光谷创业街附近
  map.setCenter(defaultCenter);
  map.setZoom(15);
};

// 显示全部路灯
const showAllLamps = () => {
  if (!map || markers.length === 0) return;
  map.setFitView(markers, false, [50, 50, 50, 50]);
};

// 加载高德地图API
const loadAMap = () => {
  return new Promise((resolve, reject) => {
    if (typeof window.AMap !== "undefined") {
      AMap = window.AMap;
      resolve();
      return;
    }

    const script = document.createElement("script");
    script.type = "text/javascript";
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`;
    script.onload = () => {
      AMap = window.AMap;
      resolve();
    };
    script.onerror = () => {
      reject(new Error("高德地图加载失败"));
    };
    document.head.appendChild(script);
  });
};

// 监听路灯数据变化
watch(
  () => props.lamps,
  () => {
    if (mapLoaded.value) {
      addMarkers();
    }
  },
  { deep: true },
);

// 监听当前选中路灯变化
watch(
  () => props.currentLamp,
  () => {
    if (mapLoaded.value) {
      addMarkers();
    }
  },
  { deep: true },
);

onMounted(async () => {
  try {
    await loadAMap();
    initMap();
  } catch (error) {
    console.error("地图加载失败:", error);
    ElMessage.warning("地图加载失败，请检查网络连接");
  }
});

onUnmounted(() => {
  if (map) {
    map.destroy();
    map = null;
  }
});
</script>

<style scoped>
.lamp-map-container {
  position: relative;
  width: 100%;
  height: 450px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.map-container {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
}

.map-loading {
  width: 100%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.map-controls {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1000;
  display: flex;
  gap: 8px;
}

.map-legend {
  position: absolute;
  bottom: 20px;
  left: 10px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  padding: 10px 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  font-size: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  margin: 6px 0;
}

.legend-icon {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.legend-icon.online {
  background: #67c23a;
}

.legend-icon.offline {
  background: #909399;
}

.legend-icon.current {
  background: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.3);
}

/* Leaflet 自定义样式 */
:deep(.custom-marker) {
  background: transparent;
  border: none;
}

:deep(.leaflet-popup-content-wrapper) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.leaflet-popup-content) {
  margin: 0;
  padding: 0;
}

:deep(.leaflet-container) {
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}
</style>
