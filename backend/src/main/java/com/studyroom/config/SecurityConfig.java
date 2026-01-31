package com.studyroom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // 允许访问Swagger/Knife4j相关路径
                .requestMatchers(
                    "/doc.html",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/swagger-resources"
                ).permitAll()
                // 允许访问登录和注册接口
                .requestMatchers("/auth/**").permitAll()
                // 允许访问测试接口
                .requestMatchers("/test/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            // 暂时禁用CSRF保护，方便测试
            .csrf(csrf -> csrf.disable())
            // 禁用HTTP Basic认证
            .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
