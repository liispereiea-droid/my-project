package com.labsupplysystem.labbackend.config;

import com.labsupplysystem.labbackend.common.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(    // 排除登录和注册接口
                        "/api/user/login", 
                        "/api/user/add",   // 注意：如果是开放注册则排除，如果是管理员添加则不排除
                        "/api/user/register" 
                ); 
    }
}