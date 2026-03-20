package com.wenhua.tcpservice.deep;

import org.json.JSONObject;

import java.util.List;

//传话器-实现与AI的通信
public interface Talker {

    //方法:传话
    //参数:传话内容,返回值:传话结果
    public String talk(String content);

    //方法:插入一条AI和用户的对话记录
    //参数:对话内容,返回值:无
    public void insertRecord(String userContent, String aiContent);

    //方法:清空对话记录
    //参数:无,返回值:无
    public void clearRecord();

    //获取历史记录
    public List<JSONObject> getMessageHistory() ;
}
