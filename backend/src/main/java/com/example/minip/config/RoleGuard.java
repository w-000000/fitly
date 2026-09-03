package com.example.minip.config;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RoleGuard {
    public ActorRole require(String rawRole, ActorRole... allowed) {
        ActorRole role;
        try {
            role = ActorRole.valueOf(rawRole == null ? "" : rawRole);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-Actor-Role 헤더가 필요합니다.");
        }
        if (Arrays.stream(allowed).noneMatch(role::equals)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다.");
        }
        return role;
    }
}
