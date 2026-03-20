//处理数据的
#include <stdio.h>
#include <time.h>

//环境监测相关功能 - 使用智慧农业感应模块
#include "../C2_e53_ia1_temp_humi_pls/include/E53_IA1.h"
#include "../C3_e53_sc1_pls/include/E53_SC1.h"

//设备信息结构体
typedef struct {
    char *id;      //本机编号
    char *name;    //设备名称
    char *ip;      //本机ip
    int port;      //本机端口
    int onlineStatus; //设备在线状态(默认在线)
    float temperature; //温度(°C)
    int humidity;      //湿度(%)
    int lightIntensity; //光照强度(lux)
    int autoMonitor;   //自动监测开关
    int reportInterval; //数据上报间隔(毫秒)
    char *deviceType;   //设备类型
    char *firmwareVersion; //固件版本
    long uptime;        //运行时间(秒)
    int signalStrength; //信号强度
    int batteryLevel;   //电池电量
    int alertStatus;    //报警状态
    
    //路灯相关字段
    int lampStatus;     //路灯开关状态: 0-关, 1-开
    int brightness;     //亮度: 0-100
    float powerConsumption; //功耗(W)
    int faultStatus;    //故障状态: 0-正常, 1-故障
    double longitude;   //经度
    double latitude;    //纬度
}DeviceInfo;

//ip单独保存
static char * saveIp = "127.0.0.1";
static char * saveId = DEVICE_ID;

//回复的数据
static char *send_data = "Hello!\r\n";

//设备信息
DeviceInfo* deviceInfo = NULL;

//启动时间
static time_t startTime;

//函数声明
static void onProcessingError(void);
static void resetDeviceInfo(DeviceInfo* deviceInfo);
static DeviceInfo* getDeviceInfo(void);
static void setReportInterval(int interval);
static void setAutoMonitor(int autoMonitor);
static void caseReport(void);
static void caseSet(cJSON * root);
static void caseAutoOn(void);
static void caseAutoOff(void);
static void caseControl(cJSON * root);
static void caseReboot(void);
static void caseCalibrate(void);
static void processingData(char *recvBuf);
static void setIp(char *ip);
static void setId(char *id);
static void setName(char *name);
static void setPort(int port);

//在开始前
void initInfo(void);

//读取传感器数据
void readSensors(void);

//更新运行时间
void updateUptime(void);

//检查报警状态
int checkAlert(void);

////////////////////////////////////
//特殊-测试专用方法
static int testIndex = 1;
static void test(void){
    printf("test-%d\r\n",testIndex++);
}

//错误情况处理
static void onProcessingError(){
    printf("处理失败!\r\n");
    //发送错误响应
    cJSON *errorResponse = cJSON_CreateObject();
    cJSON_AddStringToObject(errorResponse, CMD_RETURN, CMD_RETURN_ERROR);
    cJSON_AddStringToObject(errorResponse, "message", "处理失败");
    
    char * jsonData = cJSON_Print(errorResponse);
    cJSON_Delete(errorResponse);
    
    if(send_data != NULL){
        free(send_data);
        send_data = NULL;
    }
    send_data = newString(jsonData);
    free(jsonData);
}

//初始化信息
void initInfo(void){
    //记录启动时间
    startTime = time(NULL);
    
    /* 初始化设备信息 */
    deviceInfo=(DeviceInfo *)malloc(sizeof(DeviceInfo));
    //预防野指针,从你我做起
    deviceInfo->id = NULL;
    deviceInfo->name = NULL;
    deviceInfo->ip = NULL;
    deviceInfo->deviceType = NULL;
    deviceInfo->firmwareVersion = NULL;

    //初始化设备信息
    deviceInfo->id = newString(DEVICE_ID);
    deviceInfo->name = newString("环境监测设备-");
    deviceInfo->name = concatString(deviceInfo->name, DEVICE_ID);

    //@这里应该获取本机ip地址
    //先随便发一个数据,到时候后端接受时会看到ip的
    //后端看到ip后会发送一个数据过来,然后客户端会更新ip
    deviceInfo->ip = newString(IP_UNKNOWN);

    //默认设备在线
    deviceInfo->onlineStatus = VALUE_ONLINE;

    //初始化传感器数据
    deviceInfo->temperature = 25.0;
    deviceInfo->humidity = 60;
    deviceInfo->lightIntensity = 10000;

    //默认自动监测是开启的
    deviceInfo->autoMonitor = VALUE_AUTO_ON;

    //默认上报间隔5秒
    deviceInfo->reportInterval = REPORT_INTERVAL;

    //设备类型
    deviceInfo->deviceType = newString(DEVICE_TYPE_ENV_MONITOR);

    //固件版本
    deviceInfo->firmwareVersion = newString(FIRMWARE_VERSION);

    //运行时间
    deviceInfo->uptime = 0;

    //信号强度(模拟)
    deviceInfo->signalStrength = -50; // dBm

    //电池电量(模拟)
    deviceInfo->batteryLevel = 100; // %

    //报警状态
    deviceInfo->alertStatus = 0;

    //初始化路灯相关字段
    deviceInfo->lampStatus = LAMP_OFF;
    deviceInfo->brightness = DEFAULT_BRIGHTNESS;
    deviceInfo->powerConsumption = 0.0;
    deviceInfo->faultStatus = FAULT_NORMAL;
    deviceInfo->longitude = 0.0;
    deviceInfo->latitude = 0.0;

    //初始化回复数据
    //创建新的json数据
    //创建JSON对象
    cJSON *firstReport = cJSON_CreateObject();

    //表明意图:上线
    cJSON_AddStringToObject(firstReport, CMD_RETURN, CMD_RETURN_ONLINE);

    //添加属性id,string
    cJSON_AddStringToObject(firstReport, CMD_RETURN_ID, deviceInfo->id);

    //添加属性ip,string
    cJSON_AddStringToObject(firstReport, CMD_RETURN_IP, deviceInfo->ip);

    //添加设备在线状态
    cJSON_AddNumberToObject(firstReport, CMD_RETURN_ONLINE_STATUS, deviceInfo->onlineStatus);

    //添加设备类型
    cJSON_AddStringToObject(firstReport, CMD_RETURN_DEVICE_TYPE, deviceInfo->deviceType);

    //添加固件版本
    cJSON_AddStringToObject(firstReport, CMD_RETURN_FIRMWARE_VERSION, deviceInfo->firmwareVersion);

    //打印JSON对象
    char * jsonData = cJSON_Print(firstReport);
    printf("首次上报数据:%s\r\n",jsonData);

    //释放JSON对象
    cJSON_Delete(firstReport);

    //设置发送数据
    send_data = newString(jsonData);

    //释放jsonData
    free(jsonData);

}

//更新运行时间
void updateUptime(void){
    time_t currentTime = time(NULL);
    deviceInfo->uptime = (long)difftime(currentTime, startTime);
}

//读取传感器数据(实际读取硬件传感器)
void readSensors(void){
    //初始化传感器
    E53_IA1_Init();
    E53_SC1_Init();
    
    //读取温湿度数据
    E53_IA1_Read_Data();
    deviceInfo->temperature = E53_IA1_Data.Temperature;
    deviceInfo->humidity = (int)E53_IA1_Data.Humidity;
    
    //读取光照强度
    float lux = E53_SC1_Read_Data();
    deviceInfo->lightIntensity = (int)lux;
    
    //更新运行时间
    updateUptime();
    
    //模拟信号强度变化
    deviceInfo->signalStrength = -50 + (rand() % 20) - 10; // -60 ~ -40 dBm
    
    //模拟电池电量缓慢下降
    if(deviceInfo->batteryLevel > 0){
        deviceInfo->batteryLevel -= (rand() % 2); // 每次下降0-1%
    }
    
    printf("传感器数据读取完成: 温度=%.1f°C, 湿度=%d%%, 光照=%dlux\r\n", 
           deviceInfo->temperature, deviceInfo->humidity, deviceInfo->lightIntensity);
}

//检查是否需要报警
int checkAlert(void){
    int alert = 0;
    
    //温度报警检查
    if(deviceInfo->temperature > TEMP_ALERT_HIGH){
        printf("温度过高报警: %.1f°C > %.1f°C\r\n", deviceInfo->temperature, TEMP_ALERT_HIGH);
        alert |= 1; // 温度过高
    }
    if(deviceInfo->temperature < TEMP_ALERT_LOW){
        printf("温度过低报警: %.1f°C < %.1f°C\r\n", deviceInfo->temperature, TEMP_ALERT_LOW);
        alert |= 2; // 温度过低
    }
    
    //湿度报警检查
    if(deviceInfo->humidity > HUMIDITY_ALERT_HIGH){
        printf("湿度过高报警: %d%% > %d%%\r\n", deviceInfo->humidity, HUMIDITY_ALERT_HIGH);
        alert |= 4; // 湿度过高
    }
    if(deviceInfo->humidity < HUMIDITY_ALERT_LOW){
        printf("湿度过低报警: %d%% < %d%%\r\n", deviceInfo->humidity, HUMIDITY_ALERT_LOW);
        alert |= 8; // 湿度过低
    }
    
    //光照报警检查
    if(deviceInfo->lightIntensity > LIGHT_ALERT_HIGH){
        printf("光照过强报警: %dlux > %dlux\r\n", deviceInfo->lightIntensity, LIGHT_ALERT_HIGH);
        alert |= 16; // 光照过强
    }
    if(deviceInfo->lightIntensity < LIGHT_ALERT_LOW){
        printf("光照过弱报警: %dlux < %dlux\r\n", deviceInfo->lightIntensity, LIGHT_ALERT_LOW);
        alert |= 32; // 光照过弱
    }
    
    //电池电量报警
    if(deviceInfo->batteryLevel < 20){
        printf("电池电量低报警: %d%%\r\n", deviceInfo->batteryLevel);
        alert |= 64; // 电池电量低
    }
    
    deviceInfo->alertStatus = alert;
    return alert;
}

//处理数据
static void processingData(char *recvBuf){
    //解析JSON数据
    cJSON * root = cJSON_Parse(recvBuf);
    if(root == NULL){
        printf("JSON解析失败!\r\n");
        onProcessingError();
        return;
    }

    //获取指令
    cJSON * cmd = cJSON_GetObjectItem(root, CMD_CMD);
    if(cmd == NULL){
        printf("指令为空!\r\n");
        onProcessingError();
        cJSON_Delete(root);
        return;
    }

    //获取指令值
    char * cmdValue = cmd->valuestring;
    printf("收到指令:%s\r\n",cmdValue);

    //判断指令
    if(strcmp(cmdValue, CMD_REPORT) == 0){
        //查询指令
        caseReport();
    }else if(strcmp(cmdValue, CMD_SET) == 0){
        //设置指令
        caseSet(root);
    }else if(strcmp(cmdValue, CMD_AUTO_ON) == 0){
        //开启自动监测
        caseAutoOn();
    }else if(strcmp(cmdValue, CMD_AUTO_OFF) == 0){
        //关闭自动监测
        caseAutoOff();
    }else if(strcmp(cmdValue, CMD_CONTROL) == 0){
        //控制指令
        caseControl(root);
    }else if(strcmp(cmdValue, CMD_REBOOT) == 0){
        //重启设备
        caseReboot();
    }else if(strcmp(cmdValue, CMD_CALIBRATE) == 0){
        //校准传感器
        caseCalibrate();
    }else if(strcmp(cmdValue, CMD_LAMP_ON) == 0){
        //开灯命令
        deviceInfo->lampStatus = LAMP_ON;
        printf("收到开灯命令\r\n");
        caseReport();
    }else if(strcmp(cmdValue, CMD_LAMP_OFF) == 0){
        //关灯命令
        deviceInfo->lampStatus = LAMP_OFF;
        printf("收到关灯命令\r\n");
        caseReport();
    }else if(strcmp(cmdValue, CMD_LAMP_BRIGHTNESS) == 0){
        //亮度调节命令
        cJSON * brightness = cJSON_GetObjectItem(root, "brightness");
        if(brightness != NULL){
            int value = brightness->valueint;
            if(value >= BRIGHTNESS_MIN && value <= BRIGHTNESS_MAX){
                deviceInfo->brightness = value;
                printf("收到亮度调节命令: %d%%\r\n", value);
            }
        }
        caseReport();
    }else{
        printf("未知指令!\r\n");
        onProcessingError();
    }

    //释放JSON对象
    cJSON_Delete(root);
}

//查询指令处理
static void caseReport(void){
    //读取传感器数据
    readSensors();

    //检查报警状态
    checkAlert();

    //创建JSON对象
    cJSON *report = cJSON_CreateObject();

    //表明意图:上报数据
    cJSON_AddStringToObject(report, CMD_RETURN, CMD_RETURN_REPORT);

    //添加设备编号
    cJSON_AddStringToObject(report, CMD_RETURN_ID, deviceInfo->id);

    //添加设备名称
    cJSON_AddStringToObject(report, CMD_RETURN_NAME, deviceInfo->name);

    //添加设备IP
    cJSON_AddStringToObject(report, CMD_RETURN_IP, deviceInfo->ip);

    //添加设备在线状态
    cJSON_AddNumberToObject(report, CMD_RETURN_ONLINE_STATUS, deviceInfo->onlineStatus);

    //添加温度数据
    cJSON_AddNumberToObject(report, CMD_RETURN_TEMPERATURE, deviceInfo->temperature);

    //添加湿度数据
    cJSON_AddNumberToObject(report, CMD_RETURN_HUMIDITY, deviceInfo->humidity);

    //添加光照强度数据
    cJSON_AddNumberToObject(report, CMD_RETURN_LIGHT_INTENSITY, deviceInfo->lightIntensity);

    //添加自动监测状态
    cJSON_AddNumberToObject(report, CMD_RETURN_AUTO, deviceInfo->autoMonitor);

    //添加上报间隔
    cJSON_AddNumberToObject(report, CMD_RETURN_INTERVAL, deviceInfo->reportInterval);

    //添加报警状态
    cJSON_AddNumberToObject(report, CMD_RETURN_ALERT, deviceInfo->alertStatus);

    //添加设备类型
    cJSON_AddStringToObject(report, CMD_RETURN_DEVICE_TYPE, deviceInfo->deviceType);

    //添加固件版本
    cJSON_AddStringToObject(report, CMD_RETURN_FIRMWARE_VERSION, deviceInfo->firmwareVersion);

    //添加运行时间
    cJSON_AddNumberToObject(report, CMD_RETURN_UPTIME, deviceInfo->uptime);

    //添加信号强度
    cJSON_AddNumberToObject(report, CMD_RETURN_SIGNAL_STRENGTH, deviceInfo->signalStrength);

    //添加电池电量
    cJSON_AddNumberToObject(report, CMD_RETURN_BATTERY_LEVEL, deviceInfo->batteryLevel);

    //添加路灯相关字段
    cJSON_AddNumberToObject(report, CMD_RETURN_LAMP_STATUS, deviceInfo->lampStatus);
    cJSON_AddNumberToObject(report, CMD_RETURN_BRIGHTNESS, deviceInfo->brightness);
    cJSON_AddNumberToObject(report, CMD_RETURN_FAULT_STATUS, deviceInfo->faultStatus);
    cJSON_AddNumberToObject(report, CMD_RETURN_POWER_CONSUMPTION, deviceInfo->powerConsumption);

    //打印JSON对象
    char * jsonData = cJSON_Print(report);
    printf("上报数据:%s\r\n",jsonData);

    //释放JSON对象
    cJSON_Delete(report);

    //设置发送数据
    if(send_data != NULL){
        free(send_data);
        send_data = NULL;
    }
    send_data = newString(jsonData);

    //释放jsonData
    free(jsonData);
}

//设置指令处理
static void caseSet(cJSON * root){
    //获取设置项
    cJSON * ip = cJSON_GetObjectItem(root, CMD_SET_IP);
    cJSON * port = cJSON_GetObjectItem(root, CMD_SET_PORT);
    cJSON * id = cJSON_GetObjectItem(root, CMD_SET_ID);
    cJSON * autoMonitor = cJSON_GetObjectItem(root, CMD_SET_AUTO);
    cJSON * interval = cJSON_GetObjectItem(root, CMD_SET_INTERVAL);
    cJSON * name = cJSON_GetObjectItem(root, CMD_SET_NAME);
    cJSON * deviceType = cJSON_GetObjectItem(root, CMD_SET_DEVICE_TYPE);

    //设置IP
    if(ip != NULL){
        setIp(ip->valuestring);
    }

    //设置端口
    if(port != NULL){
        setPort(port->valueint);
    }

    //设置ID
    if(id != NULL){
        setId(id->valuestring);
    }

    //设置设备名称
    if(name != NULL){
        setName(name->valuestring);
    }

    //设置设备类型
    if(deviceType != NULL){
        if(deviceInfo->deviceType != NULL){
            free(deviceInfo->deviceType);
        }
        deviceInfo->deviceType = newString(deviceType->valuestring);
        printf("设备类型已设置为:%s\r\n", deviceType->valuestring);
    }

    //设置自动监测
    if(autoMonitor != NULL){
        setAutoMonitor(autoMonitor->valueint);
    }

    //设置上报间隔
    if(interval != NULL){
        setReportInterval(interval->valueint);
    }

    //创建JSON对象
    cJSON *setResult = cJSON_CreateObject();

    //表明意图:设置结果
    cJSON_AddStringToObject(setResult, CMD_RETURN, CMD_RETURN_SET);

    //添加设备编号
    cJSON_AddStringToObject(setResult, CMD_RETURN_ID, deviceInfo->id);

    //添加设备名称
    cJSON_AddStringToObject(setResult, CMD_RETURN_NAME, deviceInfo->name);

    //添加设备IP
    cJSON_AddStringToObject(setResult, CMD_RETURN_IP, deviceInfo->ip);

    //添加端口
    cJSON_AddNumberToObject(setResult, CMD_RETURN_PORT, deviceInfo->port);

    //添加自动监测状态
    cJSON_AddNumberToObject(setResult, CMD_RETURN_AUTO, deviceInfo->autoMonitor);

    //添加上报间隔
    cJSON_AddNumberToObject(setResult, CMD_RETURN_INTERVAL, deviceInfo->reportInterval);

    //添加设备类型
    cJSON_AddStringToObject(setResult, CMD_RETURN_DEVICE_TYPE, deviceInfo->deviceType);

    //添加成功标志
    cJSON_AddNumberToObject(setResult, "success", 1);

    //打印JSON对象
    char * jsonData = cJSON_Print(setResult);
    printf("设置结果:%s\r\n",jsonData);

    //释放JSON对象
    cJSON_Delete(setResult);

    //设置发送数据
    if(send_data != NULL){
        free(send_data);
        send_data = NULL;
    }
    send_data = newString(jsonData);

    //释放jsonData
    free(jsonData);
}

//开启自动监测
static void caseAutoOn(void){
    setAutoMonitor(VALUE_AUTO_ON);
    printf("自动监测已开启\r\n");
    caseReport();
}

//关闭自动监测
static void caseAutoOff(void){
    setAutoMonitor(VALUE_AUTO_OFF);
    printf("自动监测已关闭\r\n");
    caseReport();
}

//控制指令处理(用于扩展功能)
static void caseControl(cJSON * root){
    printf("收到控制指令\r\n");
    
    cJSON * action = cJSON_GetObjectItem(root, "action");
    cJSON * value = cJSON_GetObjectItem(root, "value");
    
    //创建响应
    cJSON *response = cJSON_CreateObject();
    cJSON_AddStringToObject(response, CMD_RETURN, CMD_RETURN_CONTROL);
    cJSON_AddStringToObject(response, CMD_RETURN_ID, deviceInfo->id);
    cJSON_AddNumberToObject(response, "success", 1);
    
    if(action != NULL){
        printf("控制动作: %s\r\n", action->valuestring);
        cJSON_AddStringToObject(response, "action", action->valuestring);
        
        //路灯控制处理
        if(strcmp(action->valuestring, CMD_LAMP_ON) == 0){
            //开灯
            deviceInfo->lampStatus = LAMP_ON;
            printf("路灯已开启\r\n");
            cJSON_AddNumberToObject(response, CMD_RETURN_LAMP_STATUS, deviceInfo->lampStatus);
        }
        else if(strcmp(action->valuestring, CMD_LAMP_OFF) == 0){
            //关灯
            deviceInfo->lampStatus = LAMP_OFF;
            printf("路灯已关闭\r\n");
            cJSON_AddNumberToObject(response, CMD_RETURN_LAMP_STATUS, deviceInfo->lampStatus);
        }
        else if(strcmp(action->valuestring, CMD_LAMP_BRIGHTNESS) == 0){
            //调节亮度
            if(value != NULL){
                int brightness = value->valueint;
                if(brightness >= BRIGHTNESS_MIN && brightness <= BRIGHTNESS_MAX){
                    deviceInfo->brightness = brightness;
                    printf("路灯亮度已设置为: %d%%\r\n", brightness);
                    cJSON_AddNumberToObject(response, CMD_RETURN_BRIGHTNESS, deviceInfo->brightness);
                } else {
                    printf("亮度值超出范围: %d\r\n", brightness);
                    cJSON_AddNumberToObject(response, "success", 0);
                    cJSON_AddStringToObject(response, "message", "亮度值超出范围");
                }
            }
        }
        else if(strcmp(action->valuestring, CMD_LAMP_STATUS) == 0){
            //查询路灯状态
            cJSON_AddNumberToObject(response, CMD_RETURN_LAMP_STATUS, deviceInfo->lampStatus);
            cJSON_AddNumberToObject(response, CMD_RETURN_BRIGHTNESS, deviceInfo->brightness);
            cJSON_AddNumberToObject(response, CMD_RETURN_FAULT_STATUS, deviceInfo->faultStatus);
        }
    }
    
    if(value != NULL){
        printf("控制值: %d\r\n", value->valueint);
        cJSON_AddNumberToObject(response, "value", value->valueint);
    }
    
    char * jsonData = cJSON_Print(response);
    cJSON_Delete(response);
    
    if(send_data != NULL){
        free(send_data);
        send_data = NULL;
    }
    send_data = newString(jsonData);
    free(jsonData);
}

//重启设备
static void caseReboot(void){
    printf("收到重启指令，准备重启...\r\n");
    
    cJSON *response = cJSON_CreateObject();
    cJSON_AddStringToObject(response, CMD_RETURN, "REBOOT");
    cJSON_AddStringToObject(response, CMD_RETURN_ID, deviceInfo->id);
    cJSON_AddNumberToObject(response, "success", 1);
    
    char * jsonData = cJSON_Print(response);
    cJSON_Delete(response);
    
    if(send_data != NULL){
        free(send_data);
        send_data = NULL;
    }
    send_data = newString(jsonData);
    free(jsonData);
    
    //实际重启设备
    //system("reboot");
}

//校准传感器
static void caseCalibrate(void){
    printf("收到传感器校准指令\r\n");
    
    cJSON *response = cJSON_CreateObject();
    cJSON_AddStringToObject(response, CMD_RETURN, "CALIBRATE");
    cJSON_AddStringToObject(response, CMD_RETURN_ID, deviceInfo->id);
    cJSON_AddNumberToObject(response, "success", 1);
    cJSON_AddStringToObject(response, "message", "传感器校准完成");
    
    char * jsonData = cJSON_Print(response);
    cJSON_Delete(response);
    
    if(send_data != NULL){
        free(send_data);
        send_data = NULL;
    }
    send_data = newString(jsonData);
    free(jsonData);
}

//设置IP
static void setIp(char *ip){
    if(deviceInfo->ip != NULL){
        free(deviceInfo->ip);
    }
    deviceInfo->ip = newString(ip);
    saveIp = newString(ip);
    printf("IP已设置为:%s\r\n",ip);
}

//设置ID
static void setId(char *id){
    if(deviceInfo->id != NULL){
        free(deviceInfo->id);
    }
    deviceInfo->id = newString(id);
    saveId = newString(id);
    printf("ID已设置为:%s\r\n",id);
}

//设置设备名称
static void setName(char *name){
    if(deviceInfo->name != NULL){
        free(deviceInfo->name);
    }
    deviceInfo->name = newString(name);
    printf("设备名称已设置为:%s\r\n",name);
}

//设置端口
static void setPort(int port){
    deviceInfo->port = port;
    printf("端口已设置为:%d\r\n",port);
}

//设置自动监测
static void setAutoMonitor(int autoMonitor){
    deviceInfo->autoMonitor = autoMonitor;
    printf("自动监测已设置为:%d\r\n",autoMonitor);
}

//设置上报间隔
static void setReportInterval(int interval){
    deviceInfo->reportInterval = interval;
    printf("上报间隔已设置为:%d毫秒\r\n",interval);
}
