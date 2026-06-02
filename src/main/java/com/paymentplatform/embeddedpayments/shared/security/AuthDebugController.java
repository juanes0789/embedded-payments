package com.paymentplatform.embeddedpayments.shared.security;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de diagnóstico (solo para desarrollo) que devuelve lo que el backend
 * ve en el SecurityContext para la petición actual.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthDebugController {

    @GetMapping("/debug")
    public ResponseEntity<?> debug() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }

        Map<String, Object> body = Map.of(
                "authenticated", authentication.isAuthenticated(),
                "principal", authentication.getName(),
                "authorities", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
                "details", authentication.getDetails()
        );

        return ResponseEntity.ok(body);
    }
}

