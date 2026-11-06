package com.qms.controller;

import com.qms.model.dto.request.LoginRequest;
import com.qms.model.dto.response.AuthResponse;
import com.qms.model.entity.AppUser;
import com.qms.repository.AppUserRepository;
import com.qms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final AppUserRepository userRepository;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @PostMapping("/login")
    @Operation(summary = "Login — sets an httpOnly JWT cookie, returns display info only")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletResponse response) {
        AuthResponse auth = authService.login(request);
        response.addCookie(buildJwtCookie(auth.getToken()));

        auth.setToken("");
        return ResponseEntity.ok(auth);
    }

    @GetMapping("/me")
    @Operation(summary = "Returns current user info if the JWT cookie is valid")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        AppUser user = userRepository.findByUsername(principal.getUsername())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new AuthResponse("", user.getUsername(),
                user.getRole().name(), user.getDisplayName()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — clears the JWT cookie")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = buildJwtCookie("");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }

    private Cookie buildJwtCookie(String value) {
        Cookie cookie = new Cookie("jwt", value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);    // true in production (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge((int) (expirationMs / 1000));
        return cookie;
    }
}