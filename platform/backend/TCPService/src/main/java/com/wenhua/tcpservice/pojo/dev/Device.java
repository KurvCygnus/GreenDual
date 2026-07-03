package com.wenhua.tcpservice.pojo.dev;


import lombok.Data;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

@Data
//设备类
public class Device {

    // 私有成员变量
    private String id;//编号
    private String name;//设备名称
    private String ip;//ip
    private Integer port;//端口
    private Integer onLine;//在线状态

    //环境监测数据
    private Float temperature;//温度
    private Integer humidity;//湿度
    private Integer lightIntensity;//光照强度

    //设备类型
    private String deviceType;//设备类型

    //是否繁忙
    private int isBusy;
    public static int IS_BUSY_ON = 1;
    public static int IS_BUSY_OFF = 0;

    //及其特殊:是否损坏
    private int isBroken;
    public static int IS_BROKEN = 1;
    public static int IS_NOT_BROKEN = 0;

    //路灯相关字段
    private Integer lampStatus;//路灯开关状态: 0-关, 1-开
    private Integer brightness;//亮度: 0-100
    private Double longitude;//经度
    private Double latitude;//纬度
    private Float powerConsumption;//功耗(W)
    private String lastMaintenance;//上次维护日期
    private Integer faultStatus;//故障状态: 0-正常, 1-故障

    //路灯状态常量
    public static int LAMP_OFF = 0;
    public static int LAMP_ON = 1;
    public static int FAULT_NORMAL = 0;
    public static int FAULT_ERROR = 1;

    // 静态成员变量
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String IP = "ip";
    public static final String PORT = "port";
    public static final String TEMPERATURE = "temperature";
    public static final String HUMIDITY = "humidity";
    public static final String LIGHT_INTENSITY = "lightIntensity";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String CMD = "cmd";
    public static final String CMD_REPORT = "REPORT";
    public static final String CMD_SET = "SET";

    //IP特殊值
    public static String IP_NULL = "114514";

    //判断是否为空方法
    public boolean isEmpty() {
        //如果设备号和ip地址都为空或者空字符串
        return (id == null || id.isEmpty()) && (ip == null || ip.isEmpty());
    }

    //toString方法
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();

        // 获取当前对象的Class对象
        Class<?> clazz = this.getClass();

        // 获取所有声明的字段（包括私有字段）
        Field[] fields = clazz.getDeclaredFields();

        try {
            // 遍历所有字段
            for (Field field : fields) {
                // 检查字段是否是静态的，如果是则跳过
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                // 设置字段为可访问，以便访问私有字段
                field.setAccessible(true);

                // 获取字段的值
                Object value = field.get(this);

                // 如果值不为null，则将其添加到JSON对象中
                if (value != null) {
                    jsonObject.put(field.getName(), value);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            // 可以选择抛出异常或返回null，取决于你的错误处理策略
            return null;
        }

        // 返回JSON字符串表示
        return jsonObject.toString();
    }

    /* 只要ip和id相同就会判定为一个设备 */

    // 重写equals方法
    @Override
    public boolean equals(Object o) {
        // 如果对象是它本身，则返回true
        if (this == o) return true;
        // 如果对象不是Device类型或者为null，则返回false
        if (o == null || getClass() != o.getClass()) return false;
        // 将对象转换为Device类型
        Device device = (Device) o;
        // 比较id和ip是否相等
        return Objects.equals(id, device.id) && Objects.equals(ip, device.ip);
    }

    // 重写hashCode方法
    @Override
    public int hashCode() {
        // 使用Objects.hash方法生成哈希码，该方法能够处理null值
        return Objects.hash(id, ip);
    }

    ///////////////////


}
