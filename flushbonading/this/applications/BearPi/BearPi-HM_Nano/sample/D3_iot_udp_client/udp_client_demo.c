/*
 * Copyright (c) 2020 Nanjing Xiaoxiongpai Intelligent Technology Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * 环境监测设备客户端
 * 用于连接平台并上报环境数据
 * 支持环境监测和路灯控制功能
 */

#include <stdio.h>
#include <unistd.h>

#include "ohos_init.h"
#include "cmsis_os2.h"

#include "wifi_device.h"
#include "lwip/netifapi.h"
#include "lwip/api_shell.h"
#include <netdb.h>
#include <string.h>
#include <stdlib.h>
#include "lwip/sockets.h"
#include "wifi_connect.h"

//全局配置
#include "../../../../../global_settings.h"
#include "string_tool.c"
#include "data_processing.c"

//环境监测设备控制
#include "../my_led/led_example.c"

#define _PROT_ SERVER_PORT

//重连配置
#define MAX_RECONNECT_ATTEMPTS 5
#define RECONNECT_DELAY_MS 3000

//心跳间隔(秒)
#define HEARTBEAT_INTERVAL 30

//在sock_fd 进行监听，在 new_fd 接收新的链接
int sock_fd;

int addr_length;

//连接状态
static int isConnected = 0;
static int reconnectAttempts = 0;

//心跳计数器
static time_t lastHeartbeat = 0;

////////////////////////////////////

//获取本机IP地址
static char* getLocalIP(void) {
    struct netif *netif = netif_find("wlan0");
    if (netif != NULL) {
        char *ip = ipaddr_ntoa(&netif->ip_addr);
        return newString(ip);
    }
    return newString(IP_UNKNOWN);
}

//连接服务器
static int connectToServer(struct sockaddr_in *send_addr) {
    //创建socket
    if ((sock_fd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
        perror("create socket failed!\r\n");
        return -1;
    }

    //设置socket选项，启用keepalive
    int keepalive = 1;
    setsockopt(sock_fd, SOL_SOCKET, SO_KEEPALIVE, &keepalive, sizeof(keepalive));

    //初始化预连接的服务端地址
    send_addr->sin_family = AF_INET;
    send_addr->sin_port = htons(_PROT_);
    send_addr->sin_addr.s_addr = inet_addr(SERVER_IP);
    addr_length = sizeof(*send_addr);

    //连接服务器
    if (connect(sock_fd, (struct sockaddr *)send_addr, addr_length) == -1) {
        perror("connect failed!\r\n");
        close(sock_fd);
        return -1;
    }

    printf("成功连接到服务器 %s:%d\r\n", SERVER_IP, SERVER_PORT);
    return 0;
}

//处理服务器命令
static void handleServerCommand(char *recvBuf, struct sockaddr_in *send_addr) {
    printf("收到服务器命令: %s\r\n", recvBuf);
    
    //处理数据
    processingData(recvBuf);
    
    //发送响应
    if (send_data != NULL) {
        send(sock_fd, send_data, strlen(send_data), 0);
        printf("已发送响应数据: %s\r\n", send_data);
    }
}

//心跳检测
static void sendHeartbeat(void) {
    cJSON *heartbeat = cJSON_CreateObject();
    cJSON_AddStringToObject(heartbeat, "type", "heartbeat");
    cJSON_AddStringToObject(heartbeat, CMD_RETURN_ID, deviceInfo->id);
    cJSON_AddNumberToObject(heartbeat, "timestamp", time(NULL));
    cJSON_AddNumberToObject(heartbeat, CMD_RETURN_ONLINE_STATUS, deviceInfo->onlineStatus);
    
    char * jsonData = cJSON_Print(heartbeat);
    cJSON_Delete(heartbeat);
    
    send(sock_fd, jsonData, strlen(jsonData), 0);
    free(jsonData);
    
    lastHeartbeat = time(NULL);
    printf("心跳包已发送\r\n");
}

//构建完整的上报数据
static char* buildReportData(void) {
    cJSON *report = cJSON_CreateObject();
    cJSON_AddStringToObject(report, CMD_RETURN, CMD_RETURN_REPORT);
    cJSON_AddStringToObject(report, CMD_RETURN_ID, deviceInfo->id);
    cJSON_AddStringToObject(report, CMD_RETURN_NAME, deviceInfo->name);
    cJSON_AddStringToObject(report, CMD_RETURN_IP, deviceInfo->ip);
    cJSON_AddNumberToObject(report, CMD_RETURN_ONLINE_STATUS, deviceInfo->onlineStatus);
    cJSON_AddNumberToObject(report, CMD_RETURN_TEMPERATURE, deviceInfo->temperature);
    cJSON_AddNumberToObject(report, CMD_RETURN_HUMIDITY, deviceInfo->humidity);
    cJSON_AddNumberToObject(report, CMD_RETURN_LIGHT_INTENSITY, deviceInfo->lightIntensity);
    cJSON_AddNumberToObject(report, CMD_RETURN_AUTO, deviceInfo->autoMonitor);
    cJSON_AddNumberToObject(report, CMD_RETURN_INTERVAL, deviceInfo->reportInterval);
    cJSON_AddNumberToObject(report, CMD_RETURN_ALERT, deviceInfo->alertStatus);
    cJSON_AddStringToObject(report, CMD_RETURN_DEVICE_TYPE, deviceInfo->deviceType);
    cJSON_AddStringToObject(report, CMD_RETURN_FIRMWARE_VERSION, deviceInfo->firmwareVersion);
    cJSON_AddNumberToObject(report, CMD_RETURN_UPTIME, deviceInfo->uptime);
    cJSON_AddNumberToObject(report, CMD_RETURN_SIGNAL_STRENGTH, deviceInfo->signalStrength);
    cJSON_AddNumberToObject(report, CMD_RETURN_BATTERY_LEVEL, deviceInfo->batteryLevel);
    
    //添加路灯相关字段
    cJSON_AddNumberToObject(report, CMD_RETURN_LAMP_STATUS, deviceInfo->lampStatus);
    cJSON_AddNumberToObject(report, CMD_RETURN_BRIGHTNESS, deviceInfo->brightness);
    cJSON_AddNumberToObject(report, CMD_RETURN_FAULT_STATUS, deviceInfo->faultStatus);
    cJSON_AddNumberToObject(report, CMD_RETURN_POWER_CONSUMPTION, deviceInfo->powerConsumption);
    
    char * jsonData = cJSON_Print(report);
    cJSON_Delete(report);
    
    return jsonData;
}

static void TCPClientTask(void)
{
    //初始化信息
    initInfo();

    //服务器的地址信息
    struct sockaddr_in send_addr;
    socklen_t addr_length = sizeof(send_addr);
    char recvBuf[2048];

    //连接Wifi
    printf("正在连接WiFi: %s\r\n", WIFI_NAME);
    WifiConnect(WIFI_NAME, WIFI_PASSWORD);
    printf("WiFi连接成功\r\n");

    //获取本机IP
    char *localIP = getLocalIP();
    printf("本机IP: %s\r\n", localIP);
    
    //更新设备IP
    if(deviceInfo->ip != NULL) {
        free(deviceInfo->ip);
    }
    deviceInfo->ip = localIP;

    //主循环
    while (1) {
        //尝试连接服务器
        if (!isConnected) {
            if (connectToServer(&send_addr) == 0) {
                isConnected = 1;
                reconnectAttempts = 0;
                
                //发送上线消息
                cJSON *onlineMsg = cJSON_CreateObject();
                cJSON_AddStringToObject(onlineMsg, CMD_RETURN, CMD_RETURN_ONLINE);
                cJSON_AddStringToObject(onlineMsg, CMD_RETURN_ID, deviceInfo->id);
                cJSON_AddStringToObject(onlineMsg, CMD_RETURN_IP, deviceInfo->ip);
                cJSON_AddNumberToObject(onlineMsg, CMD_RETURN_ONLINE_STATUS, deviceInfo->onlineStatus);
                cJSON_AddStringToObject(onlineMsg, CMD_RETURN_DEVICE_TYPE, deviceInfo->deviceType);
                cJSON_AddStringToObject(onlineMsg, CMD_RETURN_FIRMWARE_VERSION, deviceInfo->firmwareVersion);
                cJSON_AddNumberToObject(onlineMsg, CMD_RETURN_LAMP_STATUS, deviceInfo->lampStatus);
                cJSON_AddNumberToObject(onlineMsg, CMD_RETURN_BRIGHTNESS, deviceInfo->brightness);
                
                char * jsonData = cJSON_Print(onlineMsg);
                cJSON_Delete(onlineMsg);
                
                send(sock_fd, jsonData, strlen(jsonData), 0);
                printf("已发送上线消息: %s\r\n", jsonData);
                free(jsonData);
                
                lastHeartbeat = time(NULL);
            } else {
                reconnectAttempts++;
                printf("连接失败，尝试重连 (%d/%d)\r\n", reconnectAttempts, MAX_RECONNECT_ATTEMPTS);
                
                if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                    printf("达到最大重连次数，等待较长时间后重试\r\n");
                    sleep(30);
                    reconnectAttempts = 0;
                } else {
                    usleep(RECONNECT_DELAY_MS * 1000);
                }
                continue;
            }
        }

        //清空接收缓冲区
        bzero(recvBuf, sizeof(recvBuf));

        //根据自动监测状态决定是否主动上报数据
        if(deviceInfo->autoMonitor == VALUE_AUTO_ON) {
            //自动监测开启，按照设定的间隔上报数据
            //先读取传感器数据并上报
            readSensors();
            checkAlert();
            
            //计算功耗(模拟)
            if(deviceInfo->lampStatus == LAMP_ON) {
                deviceInfo->powerConsumption = deviceInfo->brightness * 0.5f; // 简单模拟功耗计算
            } else {
                deviceInfo->powerConsumption = 0.0f;
            }
            
            //构建上报数据
            char * jsonData = buildReportData();
            
            //发送数据到服务器
            if (send(sock_fd, jsonData, strlen(jsonData), 0) < 0) {
                printf("发送数据失败，断开连接\r\n");
                isConnected = 0;
                close(sock_fd);
                free(jsonData);
                continue;
            }
            
            printf("已上报数据: 温度=%.1f°C, 湿度=%d%%, 光照=%dlux, 路灯=%s, 亮度=%d%%\r\n", 
                   deviceInfo->temperature, deviceInfo->humidity, deviceInfo->lightIntensity,
                   deviceInfo->lampStatus == LAMP_ON ? "开" : "关", deviceInfo->brightness);
            free(jsonData);
            
            //等待服务器响应或命令
            fd_set read_fds;
            struct timeval timeout;
            
            FD_ZERO(&read_fds);
            FD_SET(sock_fd, &read_fds);
            
            timeout.tv_sec = deviceInfo->reportInterval / 1000;
            timeout.tv_usec = (deviceInfo->reportInterval % 1000) * 1000;
            
            int select_result = select(sock_fd + 1, &read_fds, NULL, NULL, &timeout);
            
            if (select_result > 0 && FD_ISSET(sock_fd, &read_fds)) {
                //有数据可读
                int recv_len = recv(sock_fd, recvBuf, sizeof(recvBuf) - 1, 0);
                if (recv_len > 0) {
                    recvBuf[recv_len] = '\0';
                    handleServerCommand(recvBuf, &send_addr);
                } else if (recv_len == 0) {
                    printf("服务器断开连接\r\n");
                    isConnected = 0;
                    close(sock_fd);
                    continue;
                } else {
                    printf("接收数据错误\r\n");
                }
            }
            
            //检查是否需要发送心跳
            time_t currentTime = time(NULL);
            if (difftime(currentTime, lastHeartbeat) >= HEARTBEAT_INTERVAL) {
                sendHeartbeat();
            }
        } else {
            //自动监测关闭，等待服务器命令
            fd_set read_fds;
            struct timeval timeout;
            
            FD_ZERO(&read_fds);
            FD_SET(sock_fd, &read_fds);
            
            timeout.tv_sec = 1;
            timeout.tv_usec = 0;
            
            int select_result = select(sock_fd + 1, &read_fds, NULL, NULL, &timeout);
            
            if (select_result > 0 && FD_ISSET(sock_fd, &read_fds)) {
                int recv_len = recv(sock_fd, recvBuf, sizeof(recvBuf) - 1, 0);
                if (recv_len > 0) {
                    recvBuf[recv_len] = '\0';
                    handleServerCommand(recvBuf, &send_addr);
                } else if (recv_len == 0) {
                    printf("服务器断开连接\r\n");
                    isConnected = 0;
                    close(sock_fd);
                    continue;
                } else {
                    printf("接收数据错误\r\n");
                }
            }
            
            //检查是否需要发送心跳
            time_t currentTime = time(NULL);
            if (difftime(currentTime, lastHeartbeat) >= HEARTBEAT_INTERVAL) {
                sendHeartbeat();
            }
        }
    }

    //关闭socket
    close(sock_fd);
}

static void EnvMonitorDemo(void)
{
    osThreadAttr_t attr;

    attr.name = "TCPClientTask";
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = 10240;
    attr.priority = osPriorityNormal;

    if (osThreadNew((osThreadFunc_t)TCPClientTask, NULL, &attr) == NULL) {
        printf("[EnvMonitorDemo] Falied to create TCPClientTask!\n");
    }
}

SYS_RUN(EnvMonitorDemo);
