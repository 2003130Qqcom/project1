package com.hmall.common.config;

import com.hmall.common.interceptor.UserInfoInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 公共 MVC 配置，自动注册 UserInfoInterceptor
 * 通过 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 自动装配
 */
@Configuration
@ConditionalOnClass(WebMvcConfigurer.class)
public class CommonWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterceptor());
    }
}
