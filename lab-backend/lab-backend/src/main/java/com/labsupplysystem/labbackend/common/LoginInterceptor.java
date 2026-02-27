package com.labsupplysystem.labbackend.common;

import com.labsupplysystem.labbackend.entity.User;
import com.labsupplysystem.labbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行 OPTIONS 请求 (跨域预检)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 获取 Token
        String token = request.getHeader("Authorization");
        if (token == null || "".equals(token)) {
            response.setStatus(401); // 无权限
            return false;
        }

        // 3. 解析 Token
        Claims claims = JwtUtils.parseToken(token);
        if (claims == null) {
            response.setStatus(401);
            return false;
        }

        // 4. 获取用户信息并存入 ThreadLocal
        Long userId = Long.valueOf(claims.get("id").toString());
        User user = userService.getById(userId);
        if (user == null) {
            response.setStatus(401);
            return false;
        }
        
        // --- 核心：将用户存入上下文，后续 Controller 可直接用 ---
        UserContext.setUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束，清理 ThreadLocal，防止内存泄漏
        UserContext.remove();
    }
}