package com.wenhua.tcpservice.exception;


//设备指令类型未定义异常
public class DeviceCmdTypeUndefinedException extends Exception{

    // 构造函数，接收一个字符串作为错误消息
    public DeviceCmdTypeUndefinedException(String message) {
        super(message); // 调用父类的构造函数，设置异常的消息
    }

    // 重写toString方法，以便在打印异常时显示更友好的信息
    @Override
    public String toString() {
        return "DeviceCmdTypeUndefinedException: " + getMessage();
    }
}
