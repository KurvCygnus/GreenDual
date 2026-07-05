package com.wenhua.tcpservice.config;

import com.wenhua.tcpservice.mapper.UserMapper;
import com.wenhua.tcpservice.utils.PasswordUtils;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 应用启动时，将数据库中遗留的明文密码自动升级为 SHA-256 加盐哈希.
 * <p>
 * 旧数据库中的密码以明文存储，首次启动此版本后，所有明文密码将被
 * {@link PasswordUtils#encode(String, String)} 转换并更新到数据库.
 */
@Slf4j @Component @AllArgsConstructor
public final class PasswordMigrationRunner implements CommandLineRunner
{
    private final UserMapper userMapper;
    
    private static final Pattern PASSWORD_REGEX = Pattern.compile("[0-9a-f]{64}");

    @Override public void run(String... args)
    {
        userMapper.selectAll().stream().mapToInt(
            user ->
            {
                final @Nullable var name = user.getUsername();
                final @Nullable var password = user.getPassword();
                
                // 跳过已经是 64 字符 hex 哈希的用户
                if(password == null || name == null || (password.length() == 64 && PASSWORD_REGEX.matcher(password).matches()))
                    return 0;
                
                final var hashed = PasswordUtils.encode(password, name);
                userMapper.updatePassword(user.getUserId(), hashed);
                
                return 1;
            }
        ).reduce(Integer::sum).ifPresent(count -> log.info("已自动迁移 {} 个用户的密码从明文到 SHA-256 加盐哈希", count));
    }
}