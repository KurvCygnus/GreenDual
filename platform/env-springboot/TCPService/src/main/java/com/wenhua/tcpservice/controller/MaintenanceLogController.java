package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.LogMapper;
import com.wenhua.tcpservice.mapper.UserMapper;
import com.wenhua.tcpservice.pojo.MaintenanceLog;
import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.pojo.User;
import com.wenhua.tcpservice.service.LogService;
import com.wenhua.tcpservice.utils.JwtUtils;
import com.wenhua.tcpservice.utils.Log;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


//必须带上这个前端才能获取数据
@Slf4j
@RestController
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
public class MaintenanceLogController {

    @Autowired
    private LogService logService;

    @Autowired
    private LogMapper logMapper;

    @Autowired
    private UserMapper userMapper;

    //添加日志
    @PostMapping(GlobalConfiguration.LOG_REQUEST_PREFIX + "/add")
    public Result addMaintenanceLog(@RequestBody MaintenanceLog maintenanceLog) {
        Log.d(maintenanceLog.toString());
        logService.addLog(maintenanceLog);
        return Result.success();
    }

    //查询日志
    @PostMapping(GlobalConfiguration.LOG_REQUEST_PREFIX + "/select")
    public Result selectMaintenanceLog(@RequestBody QueryParameter queryParameter) {
        //交给服务
        List<MaintenanceLog> select = logService.select(queryParameter);

        //数量也查
        int i = logMapper.selectCount(queryParameter);
        Result result = Result.success(select);
        result.setTotalPages(i);
        Log.d("页面数量"+i);
        return result;
    }

    //删除日志
    /* <<<需要管理员权限>>> */
    @PostMapping(GlobalConfiguration.LOG_REQUEST_PREFIX + "/delete")
    public Result deleteMaintenanceLog(@RequestBody List<Integer> ids, HttpServletRequest request){
        if (!isRoot(request)){
            return Result.error("权限不足");
        }
        logService.deleteLog(ids);
        return Result.success();
    }
    //查询用户是否是管理员方法
    private boolean isRoot(HttpServletRequest request){
        Integer userId = JwtUtils.getUserId(request);
        User user = userMapper.selectUserById(userId);
        System.out.println("用户权位"+user.getUserPermission());
        if(user.getUserPermission()<2){
            return false;
        }
        return true;
    }
}
