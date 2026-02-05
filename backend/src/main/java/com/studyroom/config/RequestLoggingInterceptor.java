package com.studyroom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.Enumeration;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求基本信息，不读取请求体
        logRequestBasicInfo(request);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 如果需要记录响应信息，可以在这里记录
        logResponseInfo(request, response);
    }

    private void logRequestBasicInfo(HttpServletRequest request) {
        StringBuilder requestInfo = new StringBuilder();
        requestInfo.append("[REQUEST] ").append(request.getMethod()).append(" ").append(request.getRequestURI());

        // 记录查询参数
        String queryString = request.getQueryString();
        if (queryString != null) {
            requestInfo.append("?").append(queryString);
        }

        // 记录请求头
        requestInfo.append("\nHeaders: {");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            if (headerName.equalsIgnoreCase("authorization") && headerValue != null && headerValue.length() > 20) {
                // 对token进行脱敏处理
                headerValue = headerValue.substring(0, 10) + "...";
            }
            requestInfo.append(headerName).append("=").append(headerValue);
            if (headerNames.hasMoreElements()) {
                requestInfo.append(", ");
            }
        }
        requestInfo.append("}");

        // 如果是GET请求，不记录请求体
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            log.info(requestInfo.toString());
            return;
        }

        // 对于POST/PUT等请求，安全地记录请求体
        logRequestWithBody(request, requestInfo.toString());
    }

    private void logRequestWithBody(HttpServletRequest request, String baseInfo) {
        try {
            // 检查是否是包装过的请求
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                byte[] content = wrapper.getContentAsByteArray();

                if (content.length > 0) {
                    String requestBody = new String(content, request.getCharacterEncoding());
                    // 对敏感信息进行脱敏处理
                    requestBody = maskSensitiveData(requestBody);
                    log.info("{}\nBody: {}", baseInfo, requestBody);
                } else {
                    log.info(baseInfo);
                }
            } else {
                // 如果没有包装，只记录基本信息
                log.info(baseInfo);
            }
        } catch (Exception e) {
            // 记录请求体时出错，只记录基本信息
            log.warn("Failed to log request body: {}", e.getMessage());
            log.info(baseInfo);
        }
    }

    private void logResponseInfo(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (response instanceof ContentCachingResponseWrapper) {
                ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
                byte[] content = wrapper.getContentAsByteArray();

                if (content.length > 0 && content.length < 10000) { // 只记录较小响应
                    String responseBody = new String(content, response.getCharacterEncoding());
                    responseBody = maskSensitiveData(responseBody);
                    log.info("[RESPONSE] {} {} {} ({}ms)",
                            request.getMethod(),
                            request.getRequestURI(),
                            response.getStatus());
                }
            }
        } catch (Exception e) {
            // 记录响应时出错，忽略
        }
    }

    private String maskSensitiveData(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // 对密码等敏感信息进行脱敏
        content = content.replaceAll("\"password\"\\s*:\\s*\".*?\"", "\"password\":\"******\"");
        content = content.replaceAll("\"newPassword\"\\s*:\\s*\".*?\"", "\"newPassword\":\"******\"");
        content = content.replaceAll("\"oldPassword\"\\s*:\\s*\".*?\"", "\"oldPassword\":\"******\"");
        content = content.replaceAll("\"token\"\\s*:\\s*\".*?\"", "\"token\":\"******\"");
        content = content.replaceAll("\"accessToken\"\\s*:\\s*\".*?\"", "\"accessToken\":\"******\"");
        content = content.replaceAll("\"refreshToken\"\\s*:\\s*\".*?\"", "\"refreshToken\":\"******\"");

        return content;
    }
}