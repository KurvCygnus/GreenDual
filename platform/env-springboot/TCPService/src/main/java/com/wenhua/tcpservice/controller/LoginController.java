package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.pojo.User;
import com.wenhua.tcpservice.service.UserService;
import com.wenhua.tcpservice.utils.JwtUtils;
import com.wenhua.tcpservice.utils.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
public class LoginController {

    @Autowired
    private UserService userService;

    //登录
    @PostMapping("/login")
    public Result login(@RequestBody User user){
        Log.d("等登录请求");
        User u = userService.selectUser(user.getUsername(), user.getPassword());
        if (u!=null){

            //成功,是不是该?下发令牌?
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", u.getUserId()); // 假设User类有一个getId()方法
            // 你可以添加其他需要的信息到claims中

            // 生成JWT令牌
            String jwt = JwtUtils.generateJwt(claims);

            // 构造响应
            Map<String, Object> response = new HashMap<>();
            response.put("name", u.getUsername());//返回用户名
            response.put("id", u.getUserId());//返回用户id
            response.put("roleId", u.getUserPermission());//返回用户职务
            response.put("token", jwt);

            System.out.println("用户信息");
            System.out.println(u);
            System.out.println("name="+u.getUsername());
            System.out.println("id="+u.getUserId());
            System.out.println("roleId="+u.getUserPermission());

            // 返回成功响应
            Log.d("登录成功");
            return Result.success(response);
        }

        //失败,提示用户失败

        return Result.error("USERNAME_OR_PASSWORD_ERROR");

    }
}
