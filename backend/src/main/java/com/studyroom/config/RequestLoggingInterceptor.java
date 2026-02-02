package com.studyroom.config;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();
    private static final ThreadLocal<String> REQUEST_BODY = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        START_TIME.set(System.currentTimeMillis());

        // 记录请求信息
        logRequest(request);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 此处可以记录模型视图信息，但通常不需要
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 计算请求处理时间
        long duration = System.currentTimeMillis() - START_TIME.get();

        // 记录响应信息
        logResponse(request, response, duration, ex);

        // 清理ThreadLocal
        START_TIME.remove();
        REQUEST_BODY.remove();
    }

    /**
     * 记录请求信息
     */
    private void logRequest(HttpServletRequest request) {
        StringBuilder logBuilder = new StringBuilder();

        // 基本请求信息
        logBuilder.append("[REQUEST] ")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURL());

        // 请求参数
        String queryString = request.getQueryString();
        if (StrUtil.isNotBlank(queryString)) {
            logBuilder.append("?").append(queryString);
        }

        // 请求头信息
        Map<String, String> headers = getHeaders(request);
        if (!headers.isEmpty()) {
            logBuilder.append("\nHeaders: " + headers);
        }

        // 请求体信息（仅记录非二进制数据）
        try {
            String contentType = request.getContentType();
            if (contentType != null && contentType.startsWith("application/json")) {
                String requestBody = readRequestBody(request);
                REQUEST_BODY.set(requestBody);
                if (StrUtil.isNotBlank(requestBody)) {
                    logBuilder.append("\nBody: " + requestBody);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to read request body: {}", e.getMessage());
        }

        log.info(logBuilder.toString());
    }

    /**
     * 记录响应信息
     */
    private void logResponse(HttpServletRequest request, HttpServletResponse response, long duration, Exception ex) {
        StringBuilder logBuilder = new StringBuilder();

        // 基本响应信息
        logBuilder.append("[RESPONSE] ")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURL())
                .append(" ")
                .append(response.getStatus())
                .append(" (").append(duration).append("ms)");

        // 异常信息
        if (ex != null) {
            logBuilder.append("\nException: " + ex.getMessage());
        }

        log.info(logBuilder.toString());
    }

    /**
     * 获取请求头信息
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    /**
     * 读取请求体信息
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
        return IoUtil.read(reader);
    }
}
