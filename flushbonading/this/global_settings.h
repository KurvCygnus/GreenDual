//全局配置-头文件
//global_settings.h
#include "lib/custom_tool/customTool.h"
///////////////////////////////////
//<<<配置ACTION>>>
//不同设备经常要改
//wifi名字
#define WIFI_NAME "遥遥领先"

//wifi密码
#define WIFI_PASSWORD "123456789"

//目标服务器IP - 平台服务器地址
#define SERVER_IP "localhost"

//目标服务器端口 - 平台TCP服务端口
#define SERVER_PORT 8888


//<<<配置END>>>

//下面的配置一般不用改
/* <<<本机相关ACTION>>> */
//本机编号
#define DEVICE_ID "ENV000100001"

//字段名-本机编号
#define FIELD_NAME_DEVICE_ID "id"

//字段名-本机ip
#define FIELD_NAME_DEVICE_IP "ip"

/* <<<本机相关END>>> */

/* <<<值ACTION>>>*/
//设备在线状态-在线
#define VALUE_ONLINE 1

//设备在线状态-离线
#define VALUE_OFFLINE 0

//自动监测开关-开
#define VALUE_AUTO_ON 1

//自动监测开关-关
#define VALUE_AUTO_OFF 0

//传感器数据上报间隔(毫秒)
#define REPORT_INTERVAL 5000

//温度报警阈值(°C)
#define TEMP_ALERT_HIGH 35.0
#define TEMP_ALERT_LOW 5.0

//湿度报警阈值(%)
#define HUMIDITY_ALERT_HIGH 90
#define HUMIDITY_ALERT_LOW 20

//光照报警阈值(lux)
#define LIGHT_ALERT_HIGH 80000
#define LIGHT_ALERT_LOW 100

/* <<<指令ACTION>>> */
//查询设备状态
#define CMD_REPORT "REPORT"
//设置设备参数
#define CMD_SET "SET"
//开启自动监测
#define CMD_AUTO_ON "AUTO_ON"
//关闭自动监测
#define CMD_AUTO_OFF "AUTO_OFF"
//控制设备(路灯开关等)
#define CMD_CONTROL "CONTROL"
//重启设备
#define CMD_REBOOT "REBOOT"
//校准传感器
#define CMD_CALIBRATE "CALIBRATE"

//设置-字段名-IP
#define CMD_SET_IP "ip"
//设置-字段名-端口
#define CMD_SET_PORT "port"
//设置-字段名-编号
#define CMD_SET_ID "id"
//设置-字段名-自动监测开关
#define CMD_SET_AUTO "auto"
//设置-字段名-上报间隔
#define CMD_SET_INTERVAL "interval"
//设置-字段名-设备名称
#define CMD_SET_NAME "name"
//设置-字段名-设备类型
#define CMD_SET_DEVICE_TYPE "deviceType"
//指令字段名-指令
#define CMD_CMD "cmd"

/* <<<指令END>>> */

//返回时字段名

//返回-字段名-设备在线状态
#define CMD_RETURN_ONLINE_STATUS "onlineStatus"
//返回-字段名-温度
#define CMD_RETURN_TEMPERATURE "temperature"
//返回-字段名-湿度
#define CMD_RETURN_HUMIDITY "humidity"
//返回-字段名-光照强度
#define CMD_RETURN_LIGHT_INTENSITY "lightIntensity"
//返回-字段名-端口
#define CMD_RETURN_PORT CMD_SET_PORT
//返回-字段名-自动监测开关
#define CMD_RETURN_AUTO CMD_SET_AUTO
//返回-字段名-IP
#define CMD_RETURN_IP CMD_SET_IP
//返回-字段名-设备编号
#define CMD_RETURN_ID CMD_SET_ID
//返回-字段名-上报间隔
#define CMD_RETURN_INTERVAL CMD_SET_INTERVAL
//返回-字段名-报警状态
#define CMD_RETURN_ALERT "alertStatus"
//返回-字段名-设备名称
#define CMD_RETURN_NAME "name"
//返回-字段名-设备类型
#define CMD_RETURN_DEVICE_TYPE "deviceType"
//返回-字段名-固件版本
#define CMD_RETURN_FIRMWARE_VERSION "firmwareVersion"
//返回-字段名-运行时间(秒)
#define CMD_RETURN_UPTIME "uptime"
//返回-字段名-信号强度
#define CMD_RETURN_SIGNAL_STRENGTH "signalStrength"
//返回-字段名-电池电量
#define CMD_RETURN_BATTERY_LEVEL "batteryLevel"

//返回-字段,用于表示返回行为
#define CMD_RETURN "returnIntent"
//对于查询指令的返回值
#define CMD_RETURN_REPORT CMD_REPORT
//对于设置指令的返回值
#define CMD_RETURN_SET CMD_SET
//特殊回复:表示设备上线
#define CMD_RETURN_ONLINE "online"
//特殊回复:表示控制响应
#define CMD_RETURN_CONTROL "CONTROL_RESPONSE"
//特殊回复:表示错误
#define CMD_RETURN_ERROR "ERROR"

//设备在线状态
#define DEVICE_ONLINE 1

//设备离线状态
#define DEVICE_OFFLINE 0

//ip-未知
#define IP_UNKNOWN "0.0.0.0"

//设备类型
#define DEVICE_TYPE_ENV_MONITOR "环境监测"
#define DEVICE_TYPE_STREET_LAMP "路灯"

//固件版本
#define FIRMWARE_VERSION "1.0.0"

//路灯相关配置
//路灯状态-关
#define LAMP_OFF 0
//路灯状态-开
#define LAMP_ON 1

//默认亮度(%)
#define DEFAULT_BRIGHTNESS 80

//亮度范围
#define BRIGHTNESS_MIN 0
#define BRIGHTNESS_MAX 100

//故障状态-正常
#define FAULT_NORMAL 0
//故障状态-故障
#define FAULT_ERROR 1

//路灯控制命令
#define CMD_LAMP_ON "LAMP_ON"
#define CMD_LAMP_OFF "LAMP_OFF"
#define CMD_LAMP_BRIGHTNESS "LAMP_BRIGHTNESS"
#define CMD_LAMP_STATUS "LAMP_STATUS"

//路灯返回字段
#define CMD_RETURN_LAMP_STATUS "lampStatus"
#define CMD_RETURN_BRIGHTNESS "brightness"
#define CMD_RETURN_FAULT_STATUS "faultStatus"
#define CMD_RETURN_POWER_CONSUMPTION "powerConsumption"

//全局变量
//extern DeviceInfo* deviceInfo;
