package vn.vlu.svjob.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vlu.svjob.dto.AuthDtos.LoginRequest;
import vn.vlu.svjob.dto.AuthDtos.LoginResponse;
import vn.vlu.svjob.dto.AuthDtos.RegisterRequest;
import vn.vlu.svjob.dto.AuthDtos.UserResponse;
import vn.vlu.svjob.security.UserPrincipal;
import vn.vlu.svjob.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal me) {
        return authService.me(me.id());
    }
}
