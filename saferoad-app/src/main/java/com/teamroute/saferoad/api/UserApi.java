package com.teamroute.saferoad.api;

import com.teamroute.saferoad.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserApi {

    private final UserService userService;

    @Data
    public static class PwChangeReq {
        @NotBlank private String currentPassword;
        @NotBlank private String newPassword;
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PwChangeReq req, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("ok", false));
        boolean ok = userService.changePassword(auth.getName(), req.getCurrentPassword(), req.getNewPassword());
        if (!ok) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "PASSWORD_MISMATCH"));
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
