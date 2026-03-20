import axios from "axios";

const api = "http://localhost:18080";

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

export function aiChat(sessionId, message) {
  return axios.post(api + "/ai/chat", { sessionId, message });
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
