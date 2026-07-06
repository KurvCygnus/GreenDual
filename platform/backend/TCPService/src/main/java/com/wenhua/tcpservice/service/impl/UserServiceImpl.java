package com.wenhua.tcpservice.service.impl;

import com.wenhua.tcpservice.mapper.UserMapper;
import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.User;
import com.wenhua.tcpservice.service.UserService;
import com.wenhua.tcpservice.utils.PasswordUtils;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j @Service @AllArgsConstructor
public final class UserServiceImpl implements UserService
{
    private final UserMapper userMapper;
    
    @Override public User selectUser(String username, String password)
    {
        // 通过用户名查找用户（不比较密码，因为密码是哈希存储的）
        User user = userMapper.selectUserByUsername(username);
        if(user == null)
            return null;
        
        // 验证密码（兼容明文和哈希）
        if(PasswordUtils.matches(password, username, user.getPassword()))
        {
            // 如果旧密码是明文，自动升级为哈希存储
            if(!user.getPassword().matches("[0-9a-f]{64}"))
            {
                String hashed = PasswordUtils.encode(password, username);
                userMapper.updatePassword(user.getUserId(), hashed);
                log.info("用户 {} 的密码已自动升级为哈希存储", username);
            }
            return user;
        }
        
        return null;
    }
    
    @Override @Nonnull public List<User> selectUsers(@Nonnull QueryParameter queryParameter) { return userMapper.selectUsers(queryParameter); }
    
    @Override public void updateUser(@Nonnull User user)
    {
        if(user.getPassword() != null && !user.getPassword().isEmpty())
        {
            // 需要用户名作为盐值，从数据库获取
            User existing = userMapper.selectUserById(user.getUserId());
            String salt = existing != null
                ? existing.getUsername()
                : (user.getUsername() != null ? user.getUsername() : "");
            user.setPassword(PasswordUtils.encode(user.getPassword(), salt));
        }
        userMapper.updateUser(user);
    }
    
    @Override public void addUser(@Nonnull User user)
    {
        user.setPassword(PasswordUtils.encode(user.getPassword(), user.getUsername()));
        userMapper.addUser(user);
    }
    
    @Override public void deleteUser(@Nonnull User user) { userMapper.deleteUser(user); }
}