package com.studyroom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.image.path}")
    private String imageUploadPath;

    @Value("${file.upload.image.url}")
    private String imageUploadUrl;

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    public WebMvcConfig(RequestLoggingInterceptor requestLoggingInterceptor) {
        this.requestLoggingInterceptor = requestLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册请求日志拦截器
        registry.addInterceptor(requestLoggingInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除静态资源和Swagger/Knife4j相关路径
                .excludePathPatterns(
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/upload/**",
                        "/error"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传的图片资源（考虑context-path）
        registry.addResourceHandler("/api" + imageUploadUrl + "**")
                .addResourceLocations("file:" + imageUploadPath);
        // 同时添加不带/api的映射，确保兼容性
        registry.addResourceHandler(imageUploadUrl + "**")
                .addResourceLocations("file:" + imageUploadPath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 使用模式匹配，允许所有 localhost 端口
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }
}