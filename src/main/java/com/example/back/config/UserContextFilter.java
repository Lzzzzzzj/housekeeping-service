package com.example.back.config;

import com.example.back.security.JwtUtil;
import com.example.back.security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 从请求头 Authorization (Bearer Token) 或 X-Member-Id 解析当前用户ID
 * 优先使用 JWT Token，如果没有则使用 X-Member-Id (开发阶段)
 */
@Component
@RequiredArgsConstructor
public class UserContextFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_MEMBER_ID = "X-Member-Id";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 优先从 JWT Token 解析
            String authHeader = request.getHeader(HEADER_AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());
                if (jwtUtil.validateToken(token)) {
                    try {
                        Long userId = jwtUtil.getUserIdFromToken(token);
                        Long memberId = jwtUtil.getMemberIdFromToken(token);
                        Integer userType = jwtUtil.getUserTypeFromToken(token);
                        if (userId != null) UserContext.setUserId(userId);
                        if (memberId != null) UserContext.setMemberId(memberId);
                        if (userType != null) UserContext.setUserType(userType);
                    } catch (Exception ignored) {
                        // Token 解析失败，忽略
                    }
                }
            } else {
                // 兼容开发阶段：从 X-Member-Id header 获取（需配合 ums_member 查 user_id）
                String memberIdStr = request.getHeader(HEADER_MEMBER_ID);
                if (memberIdStr != null && !memberIdStr.isBlank()) {
                    try {
                        UserContext.setMemberId(Long.parseLong(memberIdStr.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
