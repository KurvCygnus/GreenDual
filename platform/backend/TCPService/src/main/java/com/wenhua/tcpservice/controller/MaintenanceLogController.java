package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.LogMapper;
import com.wenhua.tcpservice.mapper.UserMapper;
import com.wenhua.tcpservice.pojo.MaintenanceLog;
import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.service.LogService;
import com.wenhua.tcpservice.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j @RestController @AllArgsConstructor
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
@Tag(name = "维修日志", description = "维修日志CRUD接口")
public final class MaintenanceLogController
{
    private LogService logService;
    private LogMapper logMapper;
    private UserMapper userMapper;
    
    @Operation(summary = "添加维修日志")
    @PostMapping(GlobalConfiguration.LOG_REQUEST_PREFIX + "/add")
    public Result addMaintenanceLog(@RequestBody MaintenanceLog maintenanceLog)
    {
        log.debug(maintenanceLog.toString());
        logService.addLog(maintenanceLog);
        return Result.success();
    }
    
    @Operation(summary = "查询维修日志")
    @PostMapping(GlobalConfiguration.LOG_REQUEST_PREFIX + "/select")
    public Result selectMaintenanceLog(@RequestBody QueryParameter queryParameter)
    {
        //交给服务
        List<MaintenanceLog> select = logService.select(queryParameter);
        
        //数量也查
        int i = logMapper.selectCount(queryParameter);
        log.debug("页面数量: {}", i);
        return Result.success(select, i);
    }
    
    @Operation(summary = "删除维修日志", description = "需要管理员权限")
    @PostMapping(GlobalConfiguration.LOG_REQUEST_PREFIX + "/delete")
    public Result deleteMaintenanceLog(@RequestBody List<Integer> ids, HttpServletRequest request)
    {
        if(!JwtUtils.isAdmin(request, userMapper::selectUserById))
            return Result.error("权限不足");
        logService.deleteLog(ids);
        return Result.success();
    }
}
