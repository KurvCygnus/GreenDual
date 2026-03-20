package com.wenhua.tcpservice.deep.tool;


import org.apache.commons.text.StringEscapeUtils;

//工具接口
public interface WorkerTool {

    //工作方法:
    //参数:工作者回复内容,返回值:返回的执行结果
    public String work(String content);

    //方法:获取说明书
    public String getInstruction();

    //默认矫正工具方法
    default String formCorrection(String str) {
        if (str == null) return "";
        return StringEscapeUtils.unescapeHtml4(str);
    }


}
