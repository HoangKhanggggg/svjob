package vn.vlu.svjob.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.vlu.svjob.security.JwtAuthFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Phân quyền theo URL (FR-03). Thứ tự các dòng quan trọng: dòng cụ thể đặt trước dòng chung.
 * Sai vai trò -> 403, chưa đăng nhập / token hết hạn -> 401.
 */
@Configuration
public class SecurityConfig {

    private static final String STUDENT = "STUDENT";
    private static final String EMPLOYER = "EMPLOYER";
    private static final String ADMIN = "ADMIN";

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors-origin}")
    private String corsOrigin;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Khách (chưa đăng nhập)
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stats", "/api/categories", "/api/jobs", "/api/jobs/*",
                                "/api/employers/*", "/api/users/*/reviews").permitAll()
                        // Sinh viên
                        .requestMatchers("/api/students/**").hasRole(STUDENT)
                        .requestMatchers(HttpMethod.POST, "/api/jobs/*/applications").hasRole(STUDENT)
                        .requestMatchers(HttpMethod.PATCH, "/api/applications/*/withdraw").hasRole(STUDENT)
                        // Nhà tuyển dụng
                        .requestMatchers("/api/employers/me/**").hasRole(EMPLOYER)
                        .requestMatchers(HttpMethod.GET, "/api/jobs/*/applications").hasRole(EMPLOYER)
                        .requestMatchers(HttpMethod.PATCH, "/api/applications/*/status").hasRole(EMPLOYER)
                        .requestMatchers(HttpMethod.POST, "/api/jobs").hasRole(EMPLOYER)
                        .requestMatchers(HttpMethod.PUT, "/api/jobs/*").hasRole(EMPLOYER)
                        .requestMatchers(HttpMethod.PATCH, "/api/jobs/*/status").hasRole(EMPLOYER)
                        .requestMatchers(HttpMethod.DELETE, "/api/jobs/*").hasRole(EMPLOYER)
                        // Đánh giá: SV hoặc NTD
                        .requestMatchers(HttpMethod.POST, "/api/jobs/*/reviews").hasAnyRole(STUDENT, EMPLOYER)
                        // Quản trị viên
                        .requestMatchers("/api/admin/**").hasRole(ADMIN)
                        // Còn lại (auth/me, thông báo, tải CV): chỉ cần đăng nhập
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                writeJson(res, 401, "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn"))
                        .accessDeniedHandler((req, res, ex) ->
                                writeJson(res, 403, "Bạn không có quyền thực hiện chức năng này")))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void writeJson(jakarta.servlet.http.HttpServletResponse res, int status, String message)
            throws java.io.IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(corsOrigin.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
