package com.wenhua.tcpservice.config;

import com.wenhua.tcpservice.utils.JwtUtils;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 将 {@code application.properties} 中的 {@code jwt.*} 配置项注入到 {@link JwtUtils} 静态字段.
 */
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public final class JwtConfigInitializer
{
    /**
     *  JWT 签名密钥
     */
    private String signKey;

    /**
     *  JWT 过期时间（毫秒）
     */
    private long expire;

    @PostConstruct public void init()
    {
        if(signKey != null && !signKey.isBlank())
            JwtUtils.setSignKey(signKey);

        if(expire > 0)
            JwtUtils.setExpire(expire);
    }
}