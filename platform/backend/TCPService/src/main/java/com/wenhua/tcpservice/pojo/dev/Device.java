package com.wenhua.tcpservice.pojo.dev;

import lombok.Data;

/**
 * 设备实体（TCP 设备 + 路灯）。
 */
@Data
public final class Device
{
    private String id;
    private String name;
    private String ip;
    private Integer port;
    private Integer onLine;
    
    private Float temperature;
    private Integer humidity;
    private Integer lightIntensity;
    
    private String deviceType;
    
    private int isBusy;
    public static final int IS_BUSY_ON = 1;
    public static final int IS_BUSY_OFF = 0;
    
    private int isBroken;
    public static final int IS_BROKEN = 1;
    public static final int IS_NOT_BROKEN = 0;
    
    private Integer lampStatus;
    private Integer brightness;
    private Double longitude;
    private Double latitude;
    private Float powerConsumption;
    private String lastMaintenance;
    private Integer faultStatus;
    
    public static final int LAMP_OFF = 0;
    public static final int LAMP_ON = 1;
    public static final int FAULT_NORMAL = 0;
    public static final int FAULT_ERROR = 1;
}