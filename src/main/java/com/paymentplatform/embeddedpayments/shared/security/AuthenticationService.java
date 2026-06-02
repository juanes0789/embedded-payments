package com.paymentplatform.embeddedpayments.shared.security;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * Obtiene el usuario autenticado actual
     */
    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }

    /**
     * Obtiene el rol del usuario autenticado
     */
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Priorizar roles conocidos para evitar devolver una autoridad incorrecta
            if (authentication.getAuthorities().stream().anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()))) {
                return "ROLE_ADMIN";
            }
            if (authentication.getAuthorities().stream().anyMatch(auth -> "ROLE_MERCHANT".equals(auth.getAuthority()))) {
                return "ROLE_MERCHANT";
            }
            if (authentication.getAuthorities().stream().anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority()))) {
                return "ROLE_USER";
            }

            // Si no es ninguno de los anteriores, devolver la primera autoridad disponible
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse(null);
            log.debug("getCurrentUserRole -> principal={} authorities={} resolvedRole={}",
                    authentication.getName(), authentication.getAuthorities(), role);
            return role;
        }
        return null;
    }

    /**
     * Verifica si el usuario es administrador
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        }
        return false;
    }

    /**
     * Verifica si el usuario es comercio
     */
    public boolean isMerchant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_MERCHANT".equals(auth.getAuthority()));
        }
        return false;
    }

    /**
     * Obtiene el ID del comercio del usuario autenticado (si es comercio)
     */
    public String getMerchantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> details = (java.util.Map<String, Object>) authentication.getDetails();
            Object merchantId = details.get("merchantId");
            if (merchantId != null) {
                log.debug("getMerchantId -> found in details: {} principal={}", merchantId, authentication.getName());
                return merchantId.toString();
            }
        }

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().anyMatch(auth -> "ROLE_MERCHANT".equals(auth.getAuthority()))) {
            log.debug("getMerchantId -> no details; using principal as merchant id: {}", authentication.getName());
            return authentication.getName();
        }

        return null;
    }

    /**
     * Obtiene el UUID del comercio del usuario autenticado
     */
    public UUID getCurrentMerchantId() {
        String merchantId = getMerchantId();
        if (merchantId != null) {
            return UUID.fromString(merchantId);
        }
        return null;
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String principal = authentication.getName();
        if (principal == null || principal.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException ex) {
            log.debug("getCurrentUserId -> principal is not a UUID: {}", principal);
            return null;
        }
    }
}
