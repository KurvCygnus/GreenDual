package com.wenhua.tcpservice.config;

import com.wenhua.tcpservice.interceptor.LoginCheckInterceptor;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration @AllArgsConstructor
public class WebConfig implements WebMvcConfigurer
{
    private final LoginCheckInterceptor loginCheckInterceptor;

    @Override public void addInterceptors(@NonNull InterceptorRegistry registry)
    {
        registry.addInterceptor(loginCheckInterceptor).
            addPathPatterns("/**").
            excludePathPatterns(
                "/login",
                "/error",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/css/**",
                "/js/**",
                "/images/**",
                "/fonts/**",
                "/favicon.ico"
            );
    }
}
