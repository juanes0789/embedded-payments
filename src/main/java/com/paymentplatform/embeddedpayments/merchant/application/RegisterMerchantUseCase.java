package com.paymentplatform.embeddedpayments.merchant.application;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.merchant.domain.services.MerchantDomainService;
import com.paymentplatform.embeddedpayments.shared.audit.AuditEventService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RegisterMerchantUseCase {

    private final MerchantRepository merchantRepository;
    private final MerchantDomainService merchantDomainService;
    private final AuditEventService auditEventService;

    public RegisterMerchantUseCase(MerchantRepository merchantRepository,
                                   MerchantDomainService merchantDomainService,
                                   AuditEventService auditEventService) {
        this.merchantRepository = merchantRepository;
        this.merchantDomainService = merchantDomainService;
        this.auditEventService = auditEventService;
    }

    public Merchant execute(String name, String email) {
        Merchant merchant = merchantDomainService.buildNewMerchant(name, email);
        Merchant savedMerchant = merchantRepository.save(merchant);

        String origin = resolveOrigin();
        String actor = resolveActor();
        auditEventService.registerMerchantCreated(savedMerchant.getId(), origin, actor);

        return savedMerchant;
    }

    private String resolveOrigin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return isAdmin ? "ADMIN_REGISTRATION" : "SELF_REGISTRATION";
    }

    private String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        return authentication.getName();
    }
}

