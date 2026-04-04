package com.paymentplatform.embeddedpayments.merchant.domain.services;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.shared.exception.ConflictException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MerchantDomainService {

    private final MerchantRepository merchantRepository;

    public MerchantDomainService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public Merchant buildNewMerchant(String name, String email) {
        String normalizedEmail = email.trim().toLowerCase();

        merchantRepository.findByEmail(normalizedEmail).ifPresent(existing -> {
            throw new ConflictException(
                    "MERCHANT_ALREADY_EXISTS",
                    "Merchant already exists for email",
                    List.of("email: " + normalizedEmail)
            );
        });

        return new Merchant(UUID.randomUUID(), name.trim(), normalizedEmail, "INACTIVE");
    }
}

