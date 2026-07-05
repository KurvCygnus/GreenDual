package com.wenhua.tcpservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig
{
    @Bean public OpenAPI customOpenAPI()
    {
        return new OpenAPI().info(
            new Info().
                title("智绿云控 API").
                version("1.0.0").
                description("智绿云控 - AI协同的环境监测与智能路灯节能平台后端接口文档").
                //contact(new Contact().name("TODO")).
                license(new License().name("MIT"))
        );
    }
}