package com.hmdp.config;

import com.hmdp.interception.LoginInterception;
import com.hmdp.interception.RefreshTokenInterception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Author: SongZeyao
 * @Date: 2023/2/28 - 17:59
 * @Description:
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterception loginInterception;

    @Autowired
    private RefreshTokenInterception refreshTokenInterception;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterception)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/shop/**",
                        "/voucher/**",
                        "/shop-type/**",
                        "/upload/**",
                        "/blog/hot",
                        "/user/code",
                        "/user/login"
                ).order(1);

        registry.addInterceptor(refreshTokenInterception)
                .addPathPatterns("/**").order(0);
    }
}
