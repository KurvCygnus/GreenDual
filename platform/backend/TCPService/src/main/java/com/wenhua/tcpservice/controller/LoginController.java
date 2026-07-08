package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.pojo.User;
import com.wenhua.tcpservice.service.UserService;
import com.wenhua.tcpservice.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
@AllArgsConstructor
@Tag(name = "登录认证", description = "用户登录、令牌获取接口")
public final class LoginController
{
    private UserService userService;
    
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT令牌")
    @PostMapping("/login")
    public Result login(@RequestBody User user)
    {
        log.debug("等登录请求");
        final var u = userService.selectUser(user.getUsername(), user.getPassword());
        if(u != null)
        {
            
            //成功,是不是该?下发令牌?
            final var claims = new HashMap<String, Object>();
            claims.put("userId", u.getUserId()); // 假设User类有一个getId()方法
            // 你可以添加其他需要的信息到claims中
            
            // 生成JWT令牌
            final var jwt = JwtUtils.generateJwt(claims);
            final var refreshToken = JwtUtils.generateRefreshToken(claims);
            
            // 构造响应
            final var response = new HashMap<String, Object>();
            response.put("name", u.getUsername());//返回用户名
            response.put("id", u.getUserId());//返回用户id
            response.put("roleId", u.getUserPermission());//返回用户职务
            response.put("token", jwt);
            response.put("refreshToken", refreshToken);
            
            log.info(
                """
                用户信息
                {}
                name = {}
                id = {}
                roleId = {}
                """,
                u,
                u.getUsername(),
                u.getUserId(),
                u.getUserPermission()
            );
            
            // 返回成功响应
            log.debug("登录成功");
            return Result.success(response);
        }
        
        //失败,提示用户失败
        
        return Result.error("USERNAME_OR_PASSWORD_ERROR");
        
    }
    
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public Result refresh(@RequestBody Map<String, String> request)
    {
        final var refreshToken = request.get("refreshToken");
        if(refreshToken == null || refreshToken.isBlank())
            return Result.error("REFRESH_TOKEN_REQUIRED");
        
        try
        {
            final var claims = JwtUtils.parseRefreshToken(refreshToken);
            final var userId = claims.get("userId", Integer.class);
            if(userId == null)
                return Result.error("INVALID_TOKEN");
            
            final var newClaims = new HashMap<String, Object>();
            newClaims.put("userId", userId);
            
            final var newToken = JwtUtils.generateJwt(newClaims);
            
            final var response = new HashMap<String, Object>();
            response.put("token", newToken);
            
            return Result.success(response);
        }
        catch(ExpiredJwtException e)
        {
            log.info("刷新令牌已过期");
            return Result.error("REFRESH_TOKEN_EXPIRED");
        }
        catch(Exception e)
        {
            log.info("刷新令牌无效");
            return Result.error("INVALID_REFRESH_TOKEN");
        }
    }
}
