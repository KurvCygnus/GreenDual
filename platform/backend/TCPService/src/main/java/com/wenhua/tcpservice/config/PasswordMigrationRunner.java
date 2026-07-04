package com.wenhua.tcpservice.config;

import com.wenhua.tcpservice.mapper.UserMapper;
import com.wenhua.tcpservice.pojo.User;
import com.wenhua.tcpservice.utils.PasswordUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动时，将数据库中遗留的明文密码自动升级为 SHA-256 加盐哈希.
 * <p>
 * 旧数据库中的密码以明文存储，首次启动此版本后，所有明文密码将被
 * {@link PasswordUtils#encode(String, String)} 转换并更新到数据库.
 */
@Slf4j
@Component
@AllArgsConstructor
public final class PasswordMigrationRunner implements CommandLineRunner
{
    private final UserMapper userMapper;

    @Override
    public void run(String... args)
    {
        List<User> allUsers = userMapper.selectAll();
        int migrated = 0;

        for (User user : allUsers)
        {
            String pw = user.getPassword();
            // 跳过已经是 64 字符 hex 哈希的用户
            if (pw != null && pw.length() == 64 && pw.matches("[0-9a-f]{64}"))
                continue;

            String hashed = PasswordUtils.encode(pw, user.getUsername());
            userMapper.updatePassword(user.getUserId(), hashed);
            migrated++;
        }

        if (migrated > 0)
            log.info("已自动迁移 {} 个用户的密码从明文到 SHA-256 加盐哈希", migrated);
    }
}