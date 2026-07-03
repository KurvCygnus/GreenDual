package com.wenhua.tcpservice.service;

import com.wenhua.tcpservice.pojo.MaintenanceLog;
import com.wenhua.tcpservice.pojo.QueryParameter;

import java.util.List;

//日志服务
public interface LogService {
    //添加日志
    void addLog(MaintenanceLog maintenanceLog);

    //查询
    List<MaintenanceLog> select(QueryParameter queryParameter);

    //删除
    void deleteLog(List<Integer> ids);
}
