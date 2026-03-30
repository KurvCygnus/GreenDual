package com.wenhua.tcpservice.service.impl;

import com.wenhua.tcpservice.mapper.LogMapper;
import com.wenhua.tcpservice.pojo.MaintenanceLog;
import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.service.LogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class LogServiceImpl implements LogService {

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
        log.info(queryParameter.toString());
        log.info("---------------------");
        queryParameter.calculateOffset();
        return logMapper.select(queryParameter);
    }

    @Override
    public void deleteLog(List<Integer> ids) {
        ids.forEach(logMapper::deleteLog);
    }


}
