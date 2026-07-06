package com.wenhua.tcpservice.config;

import com.wenhua.tcpservice.utils.JwtUtils;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 将 {@code application.properties} 中的 {@code jwt.*} 配置项注入到 {@link JwtUtils} 静态字段.
 */
@Setter
@Component
public final class JwtConfigInitializer
{
    /**
     *  JWT 签名密钥
     */
    private @Value("${jwt.sign-key}") String signKey;

    /**
     *  JWT 过期时间（毫秒）
     */
    private @Value("${jwt.expire}") long expire;

    /**
     *  刷新令牌过期时间（毫秒）
     */
    private @Value("${jwt.refresh-expire}") long refreshExpire;

    @PostConstruct public void init()
    {
        JwtUtils.setSignKey(signKey);
        JwtUtils.setExpire(expire);
        JwtUtils.setRefreshExpire(refreshExpire);
    }
}