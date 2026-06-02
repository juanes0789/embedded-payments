package com.paymentplatform.embeddedpayments.transaction.domain.services;

import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TransactionDomainService {

    public PaymentTransaction register(UUID paymentIntentId, BigDecimal amount, String currency) {
        if (paymentIntentId == null) {
            throw new DomainException("paymentIntentId is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new DomainException("amount must be greater than zero");
        }
        if (currency == null || currency.isBlank() || currency.length() != 3) {
            throw new DomainException("currency must be a 3-letter ISO code");
        }

        return new PaymentTransaction(UUID.randomUUID(), paymentIntentId, amount, currency.toUpperCase(), "PENDING", Instant.now(), Instant.now());
    }
}

