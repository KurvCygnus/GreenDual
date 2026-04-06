package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.UserMapper;
import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.pojo.User;
import com.wenhua.tcpservice.service.UserService;
import com.wenhua.tcpservice.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
@AllArgsConstructor
@Slf4j
public class UserController {

    private UserService userService;
    private UserMapper userMapper;

    @PostMapping(GlobalConfiguration.USER_REQUEST_PREFIX + "/select")
    public Result selectUser(@RequestBody QueryParameter queryParameter, HttpServletRequest request) {
        if (!isRoot(request)) {
            return Result.error("权限不足");
        }
        log.debug("{}", queryParameter);
        List<User> users = userService.selectUsers(queryParameter);
        log.debug("查询到了用户: {}", users);
        Result result = Result.success(users);
        result.setTotalPages(userMapper.selectUserCount(queryParameter));
        return result;
    }

    @PostMapping(GlobalConfiguration.USER_REQUEST_PREFIX + "/update")
    public Result updateUser(@RequestBody User user, HttpServletRequest request) {
        if (!isRoot(request)) {
            return Result.error("权限不足");
        }
        log.debug("{}", user);
        userService.updateUser(user);
        return Result.success();
    }

    @PostMapping(GlobalConfiguration.USER_REQUEST_PREFIX + "/add")
    public Result addUser(@RequestBody User user, HttpServletRequest request) {
        if (!isRoot(request)) {
            return Result.error("权限不足");
        }
        User existing = userMapper.selectUserByUsername(user.getUsername());
        if (existing != null) {
            return Result.error("用户名已存在");
        }
        log.debug("{}", user);
        userService.addUser(user);
        return Result.success();
    }

    @PostMapping(GlobalConfiguration.USER_REQUEST_PREFIX + "/delete")
    public Result deleteUser(@RequestBody User user, HttpServletRequest request) {
        if (!isRoot(request)) {
            return Result.error("权限不足");
        }
        if (user.getUserId() == 1) {
            return Result.error("不能删除管理员账户");
        }
        log.debug("{}", user);
        userService.deleteUser(user);
        return Result.success();
    }

    @PostMapping(GlobalConfiguration.USER_REQUEST_PREFIX + "/resetPassword")
    public Result resetPassword(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        if (!isRoot(request)) {
            return Result.error("权限不足");
        }
        try {
            Integer userId = (Integer) params.get("userId");
            String newPassword = (String) params.get("newPassword");
            
            if (userId == null || newPassword == null || newPassword.isEmpty()) {
                return Result.error("参数错误");
            }
            
            userMapper.updatePassword(userId, newPassword);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            log.error("重置密码失败: {}", e.getMessage());
            return Result.error("重置密码失败");
        }
    }

    @PostMapping(GlobalConfiguration.USER_REQUEST_PREFIX + "/stats")
    public Result getUserStats(HttpServletRequest request) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", userMapper.countAll());
            stats.put("admin", userMapper.countByPermission(2));
            stats.put("normal", userMapper.countByPermission(1));
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计失败: {}", e.getMessage());
            return Result.error("获取用户统计失败");
        }
    }

    private boolean isRoot(HttpServletRequest request) {
        Integer userId = JwtUtils.getUserId(request);
        User user = userMapper.selectUserById(userId);
        log.info("用户权位: {}", user.getUserPermission());
        return user.getUserPermission() >= 2;
    }
}
