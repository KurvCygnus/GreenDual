package com.wenhua.tcpservice.service.impl;

import com.wenhua.tcpservice.mapper.LogMapper;
import com.wenhua.tcpservice.pojo.MaintenanceLog;
import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.service.LogService;
import com.wenhua.tcpservice.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogMapper logMapper;

    @Override
    public void addLog(MaintenanceLog maintenanceLog) {
        //添加日志
        //更新时间
        maintenanceLog.updateTimeToCurrent();

        //然后可以加入了
        logMapper.addLog(maintenanceLog);
    }

    //查询
    @Override
    public List<MaintenanceLog> select(QueryParameter queryParameter) {
        Log.d(queryParameter.toString());
        Log.d("---------------------");
        queryParameter.calculateOffset();
        List<MaintenanceLog> select = logMapper.select(queryParameter);
        return select;
    }

    @Override
    public void deleteLog(List<Integer> ids) {
        ids.forEach(id -> logMapper.deleteLog(id));
    }


}
