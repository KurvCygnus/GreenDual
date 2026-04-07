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

#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "ohos_init.h"
#include "cmsis_os2.h"

#include "lwip/sockets.h"
#include "wifi_connect.h"
#include "E53_IA1.h"

//全局配置
#include "../../../../../global_settings.h"

#define SERVER_IP SERVER_ADDR /* 服务器IP地址 */
#define SERVER_PORT 8888 /* 服务器端口 */

#define DEVICE_ID "DEVICE001" /* 设备ID */

int sockfd = -1;

// 发送设备上线消息
void sendDeviceOnline(void)
{
    char buf[256];
    sprintf(buf, "{\"returnIntent\":\"online\",\"id\":\"%s\"}", DEVICE_ID);
    send(sockfd, buf, strlen(buf), 0);
    printf("发送设备上线消息: %s\r\n", buf);
}

// 发送设备数据上报消息
void sendDeviceReport(void)
{
    char buf[256];
    sprintf(buf, "{\"returnIntent\":\"REPORT\",\"id\":\"%s\",\"temperature\":%.2f,\"humidity\":%.0f,\"lightIntensity\":%.0f}", 
            DEVICE_ID, E53_IA1_Data.Temperature, E53_IA1_Data.Humidity, E53_IA1_Data.Lux);
    send(sockfd, buf, strlen(buf), 0);
    printf("发送设备数据上报: %s\r\n", buf);
}

// 处理服务器发送的命令
void handleServerCommand(char *cmd)
{
    printf("收到服务器命令: %s\r\n", cmd);
    
    // 这里可以根据实际命令进行处理
    // 例如：控制灯光、电机等
}

// TCP客户端任务
static void TCPClientTask(void)
{
    struct sockaddr_in server_addr;
    char recvbuf[512];
    int ret;
    
    // 连接WiFi
    WifiConnect(WIFI_NAME, WIFI_PASSWORD);
    
    // 创建socket
    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) == -1)
    {
        perror("socket is error\r\n");
        return;
    }
    
    // 设置服务器地址
    bzero(&server_addr, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(SERVER_PORT);
    server_addr.sin_addr.s_addr = inet_addr(SERVER_IP);
    
    // 连接服务器
    if (connect(sockfd, (struct sockaddr *)&server_addr, sizeof(struct sockaddr)) == -1)
    {
        perror("connect is error\r\n");
        close(sockfd);
        return;
    }
    
    printf("连接服务器成功\r\n");
    
    // 发送设备上线消息
    sendDeviceOnline();
    
    // 主循环
    while (1)
    {
        // 接收服务器数据
        ret = recv(sockfd, recvbuf, sizeof(recvbuf), 0);
        if (ret > 0)
        {
            recvbuf[ret] = '\0';
            handleServerCommand(recvbuf);
        }
        else if (ret == 0)
        {
            printf("服务器断开连接\r\n");
            break;
        }
        else
        {
            printf("recv error\r\n");
        }
        
        // 读取传感器数据
        E53_IA1_Read_Data();
        
        // 发送数据上报消息
        sendDeviceReport();
        
        // 延时1秒
        sleep(1);
    }
    
    // 关闭socket
    close(sockfd);
    printf("TCP客户端已关闭\r\n");
}

static void TCPClientDemo(void)
{
    osThreadAttr_t attr;
    
    attr.name = "TCPClientTask";
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = 10240;
    attr.priority = osPriorityNormal;
    
    if (osThreadNew((osThreadFunc_t)TCPClientTask, NULL, &attr) == NULL)
    {
        printf("[TCPClientDemo] Falied to create TCPClientTask!\n");
    }
}

APP_FEATURE_INIT(TCPClientDemo);
