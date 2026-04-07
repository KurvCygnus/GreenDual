package com.wenhua.tcpservice.config;

//全局配置类
public class GlobalConfiguration {

    //前端origins - 支持多个端口
    public static final String ORIGINS="http://localhost:5173,http://localhost:5174,http://localhost:5175,http://8.130.125.122:8088";
    //public static final String ORIGINS="*";

    //TCP服务端口号
    public static int TCP_PORT=8888;

    //最大响应时间
    public static long MAX_RESPONSE_TIME=1500;

    //正常等待时间(执行全体操作之类的大工作量需要)
    public static long NORMAL_WAIT_TIME=3000;

    //环境监测设备请求前缀
    public static final String DEVICE_REQUEST_PREFIX="/device";

    //监测记录请求前缀
    public static final String LOG_REQUEST_PREFIX="/log";

    //用户请求前缀
    public static final String USER_REQUEST_PREFIX="/user";
    
    //ai
    public static final String AI_REQUEST_PREFIX="/ai";

    //环境数据阈值配置
    //温度阈值(°C)
    public static final float MIN_TEMPERATURE=0;
    public static final float MAX_TEMPERATURE=50;

    //湿度阈值(%)
    public static final int MIN_HUMIDITY=0;
    public static final int MAX_HUMIDITY=100;

    //光照强度阈值(lux)
    public static final int MIN_LIGHT_INTENSITY=0;
    public static final int MAX_LIGHT_INTENSITY=100000;

    //自动报警阈值
    public static final float ALERT_TEMPERATURE_HIGH=35;
    public static final float ALERT_TEMPERATURE_LOW=5;
    public static final int ALERT_HUMIDITY_HIGH=90;
    public static final int ALERT_HUMIDITY_LOW=20;
}
