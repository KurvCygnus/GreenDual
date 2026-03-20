package com.wenhua.tcpservice.deep.tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//时间工具
//用于在每次回复中展示时间
public class TimeTool implements WorkerTool{

    @Override
    public String work(String content) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

        // 格式化时间并转换为字符串
        String formattedDate = now.format(formatter);

        formattedDate="["+formattedDate+"]";

        // 返回格式化后的时间字符串
        return formattedDate;
    }
    @Override
    public String getInstruction() {
        //没有说明
        return "";
    }
}
