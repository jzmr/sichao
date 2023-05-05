package com.sichao.common.config;

import com.sichao.common.interceptor.TokenRefreshInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: WebMVC配置类
 * @author: sjc
 * @createTime: 2023年04月30日 15:24
 */
@Configuration
public class webConfig implements WebMvcConfigurer {
    @Autowired
    private TokenRefreshInterceptor tokenRefreshInterceptor;

    @Override//配置拦截器
    public void addInterceptors(InterceptorRegistry registry) {
        //所有方法都要被token续签拦截器拦截
        registry.addInterceptor(tokenRefreshInterceptor).addPathPatterns("/**");

    }
}
