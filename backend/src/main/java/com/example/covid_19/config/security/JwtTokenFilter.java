package com.example.covid_19.config.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 💡 1. 第一行印出：確保這個過濾器確實有在跑每一個請求
        System.out.println(">>> 【JwtTokenFilter 啟動】請求路徑: " + request.getRequestURI());

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                System.out.println(">>> 【JWT 解析成功】使用者: " + username);

                List<String> permissionCodes = claims.get("permissions", List.class);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    if (permissionCodes != null) {
                        for (String perm : permissionCodes) {
                            authorities.add(new SimpleGrantedAuthority(perm));
                        }
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println(">>> 【SecurityContext 已授權】權限清單: " + authorities);
                }
            } catch (Exception e) {
                System.out.println(">>> 【JWT 驗證失敗】原因: " + e.getClass().getName() + " - " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            System.out.println(">>> 【JwtTokenFilter】沒有抓到 Authorization Header 或格式不符");
        }

        filterChain.doFilter(request, response);
    }
}