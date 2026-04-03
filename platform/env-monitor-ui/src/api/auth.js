import axios from "axios";

const api = "http://localhost:18081";

// 请求拦截器 - 添加token
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers["token"] = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

export function login(data) {
  return axios.post(api + "/login", data);
}

export function getDeviceStats() {
  return axios.get(api + "/device/stats");
}

export function getDeviceList(params) {
  return axios.post(api + "/device/select", params);
}

export function getDeviceDetail(id) {
  return axios.get(api + `/device/detail/${id}`);
}

export function addDevice(device) {
  return axios.post(api + "/device/add", device);
}

export function updateDevice(device) {
  return axios.post(api + "/device/update", device);
}

export function deleteDevice(ids) {
  return axios.post(api + "/device/delete", ids);
}

export function updateDeviceStatus(params) {
  return axios.post(api + "/device/status", params);
}

export function batchUpdateDeviceStatus(params) {
  return axios.post(api + "/device/status/batch", params);
}

// ========== 路灯管理API ==========

export function getLampList() {
  return axios.get(api + "/lamp/list");
}

export function controlLamp(data) {
  return axios.post(api + "/lamp/control", data);
}

export function adjustBrightness(data) {
  return axios.post(api + "/lamp/brightness", data);
}

export function batchControlLamp(data) {
  return axios.post(api + "/lamp/batchControl", data);
}

export function getLampStats() {
  return axios.get(api + "/lamp/stats");
}

export function getFaultLamps() {
  return axios.get(api + "/lamp/faults");
}

export function smartAdjust(data) {
  return axios.post(api + "/lamp/smartAdjust", data);
}

// ========== 监测记录API ==========

export function getRecordList(params) {
  return axios.post(api + "/user/record/list", params);
}

export function getRecordStats() {
  return axios.post(api + "/user/record/stats");
}

export function getRecordDetail(id) {
  return axios.post(api + "/user/record/detail", { id });
}

export function deleteRecord(id) {
  return axios.post(api + "/user/record/delete", { id });
}

export function addRecord(record) {
  return axios.post(api + "/user/record/add", record);
}

// ========== 用户管理API ==========

export function getUserList(params) {
  return axios.post(api + "/user/select", params);
}

export function addUser(user) {
  return axios.post(api + "/user/add", user);
}

export function updateUser(user) {
  return axios.post(api + "/user/update", user);
}

export function deleteUser(userId) {
  return axios.post(api + "/user/delete", { userId });
}

export function resetPassword(userId, newPassword) {
  return axios.post(api + "/user/resetPassword", { userId, newPassword });
}

export function getUserStats() {
  return axios.post(api + "/user/stats");
}

// ========== 设备类型管理API ==========

export function getDeviceTypeList() {
  return axios.post(api + "/user/deviceType/list");
}

export function addDeviceType(deviceType) {
  return axios.post(api + "/user/deviceType/add", deviceType);
}

export function updateDeviceType(deviceType) {
  return axios.post(api + "/user/deviceType/update", deviceType);
}

export function toggleDeviceTypeStatus(id) {
  return axios.post(api + "/user/deviceType/toggleStatus", { id });
}

export function deleteDeviceType(id) {
  return axios.post(api + "/user/deviceType/delete", { id });
}

// ========== AI功能API ==========

const DEEPSEEK_API_KEY = "sk-d3447a51c4c349c7bd269d1c9654cb25";
const DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";

export async function aiChat(sessionId, message) {
  try {
    const response = await fetch(DEEPSEEK_API_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${DEEPSEEK_API_KEY}`,
      },
      body: JSON.stringify({
        model: "deepseek-chat",
        messages: [
          {
            role: "system",
            content: `你是一个环境监测和智能路灯控制系统的AI助手。你的职责是：
1. 分析环境数据（温度、湿度、光照强度）
2. 提供路灯控制建议（基于光照、天气状况）
3. 生成节能优化方案
4. 诊断设备故障和提供维护建议
5. 基于真实监测数据给出实用建议

请用专业、简洁的语言回答用户问题，提供具体的数值分析和可行的建议。`,
          },
          {
            role: "user",
            content: message,
          },
        ],
        temperature: 0.7,
        max_tokens: 2000,
      }),
    });

    if (!response.ok) {
      throw new Error(`DeepSeek API error: ${response.status}`);
    }

    const data = await response.json();
    const aiResponse = data.choices[0].message.content;

    return {
      data: {
        code: 1,
        data: {
          response: aiResponse,
        },
      },
    };
  } catch (error) {
    console.error("DeepSeek API调用失败:", error);
    throw error;
  }
}

export function aiClearHistory(sessionId) {
  return axios.post(api + "/ai/clear", { sessionId });
}

export function aiGetHistory(sessionId) {
  return axios.post(api + "/ai/history", { sessionId });
}

export function aiAnalyzeEnvironment(sessionId, data) {
  return axios.post(api + "/ai/analyze", { sessionId, ...data });
}

export function aiPredictTrend(sessionId, predictType, hours, historicalData) {
  return axios.post(api + "/ai/predict", {
    sessionId,
    predictType,
    hours,
    historicalData,
  });
}

// ========== 校园能源管理API ==========

export function getCampusOverview() {
  return axios.get(api + "/campus/overview");
}

export function getEnergyStatistics(areaId) {
  return axios.get(api + "/campus/energy/statistics", { params: { areaId } });
}

export function getBillHistory(areaId, months = 6) {
  return axios.get(api + "/campus/energy/bill/history", {
    params: { areaId, months },
  });
}

export function createBill(areaId, billMonth) {
  return axios.post(api + "/campus/energy/bill/generate", {
    areaId,
    billMonth,
  });
}

export function getEnergyDashboard(areaId) {
  return axios.get(api + "/campus/energy/dashboard", { params: { areaId } });
}

export function getTeachingStatistics() {
  return axios.get(api + "/campus/teaching/statistics");
}

export function getTeachingCases(course) {
  return axios.get(api + "/campus/teaching/cases", { params: { course } });
}

export function createTeachingCase(deviceId, caseType) {
  return axios.post(api + "/campus/teaching/case/create", {
    deviceId,
    caseType,
  });
}

export function optimizeLighting(deviceId) {
  return axios.post(api + "/campus/lighting/optimize", { deviceId });
}

export function applyLightingOptimization(deviceId, brightness) {
  return axios.post(api + "/campus/lighting/apply", { deviceId, brightness });
}
