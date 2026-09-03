package com.example.minip.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAccountRepository users;
    private final AuthSessionRepository sessions;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    public AuthController(UserAccountRepository users, AuthSessionRepository sessions) { this.users = users; this.sessions = sessions; }

    @PostMapping("/signup") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public UserView signup(@Valid @RequestBody SignupRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        return UserView.from(users.save(new UserAccount(request.email(), passwords.encode(request.password()), request.name())));
    }

    @PostMapping("/login") @Transactional
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwords.matches(request.password(), user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        AuthSession session = sessions.save(new AuthSession(user));
        return new LoginResponse(session.getToken().toString(), "Bearer", session.getExpiresAt(), UserView.from(user));
    }

    public record SignupRequest(@NotBlank @Email String email, @NotBlank @Size(min = 8, max = 72) String password, @NotBlank @Size(max = 50) String name) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record UserView(Long id, String email, String name, String role, Instant createdAt) {
        static UserView from(UserAccount user) { return new UserView(user.getId(), user.getEmail(), user.getName(), user.getRole().name(), user.getCreatedAt()); }
    }
    public record LoginResponse(String accessToken, String tokenType, Instant expiresAt, UserView user) {}
}
