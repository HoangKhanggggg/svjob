package vn.vlu.svjob.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.vlu.svjob.entity.UserStatus;
import vn.vlu.svjob.repository.UserRepository;

import java.io.IOException;
import java.util.List;

/**
 * Chạy trước mọi request: đọc header "Authorization: Bearer <token>",
 * nếu token hợp lệ và tài khoản chưa bị khóa thì gắn người dùng vào SecurityContext.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Long userId = jwtUtil.parseUserId(header.substring(7));
            if (userId != null) {
                // Đọc lại từ CSDL để tài khoản vừa bị Admin khóa không dùng tiếp token cũ được (FR-20)
                userRepository.findById(userId)
                        .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                        .ifPresent(u -> {
                            UserPrincipal principal = new UserPrincipal(u.getId(), u.getEmail(), u.getRole());
                            var auth = new UsernamePasswordAuthenticationToken(principal, null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        }
        chain.doFilter(request, response);
    }
}
