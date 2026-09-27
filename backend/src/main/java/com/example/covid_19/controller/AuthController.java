package com.example.covid_19.controller;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 👈 記得引入
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.covid_19.dto.LoginRequest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 👈 統一從 application.properties 讀取密鑰！
    @Value("${jwt.secret}")
    private String jwtSecret;

    private final long jwtExpirationMs = 86400000L; // 1 天有效

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. 從資料庫查詢使用者
        String userSql = "SELECT user_id, user_name, password, is_active FROM users WHERE user_name = :username";
        MapSqlParameterSource params = new MapSqlParameterSource("username", request.getUsername());

        List<Map<String, Object>> users = jdbcTemplate.queryForList(userSql, params);

        if (users.isEmpty()) {
            return ResponseEntity.status(401).body("帳號或密碼錯誤");
        }

        Map<String, Object> user = users.get(0);
        String storedPassword = (String) user.get("password");
        Boolean isActive = (Boolean) user.get("is_active");
        Integer userId = (Integer) user.get("id");

        // 2. 檢查帳號是否被停用
        if (isActive != null && !isActive) {
            return ResponseEntity.status(403).body("此帳號已被停用");
        }

        // 3. 比對密碼 (直接比對明碼)
        if (!request.getPassword().equals(storedPassword)) {
            return ResponseEntity.status(401).body("帳號或密碼錯誤");
        }

        // 4. 查詢該使用者的角色
        String roleSql = "SELECT r.role_name FROM roles r " +
                "JOIN user_has_role uhr ON r.role_id = uhr.role_id " +
                "WHERE uhr.user_id = :userId";
        List<String> roles = jdbcTemplate.queryForList(roleSql, new MapSqlParameterSource("userId", userId),
                String.class);
        String primaryRole = roles.isEmpty() ? "USER" : roles.get(0);

        // 5. 產生 JWT Token (使用跟 JwtTokenFilter 完全相同的 SecretKey 邏輯)
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject(request.getUsername())
                .claim("role", primaryRole)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key) // 👈 帶入正確的 Key
                .compact();

        // 6. 回傳給前端對應的格式 { token, role }
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role", primaryRole);

        return ResponseEntity.ok(response);
    }
}