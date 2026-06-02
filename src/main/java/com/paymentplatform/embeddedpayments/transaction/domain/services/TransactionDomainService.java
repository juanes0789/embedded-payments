package com.paymentplatform.embeddedpayments.transaction.domain.services;

import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TransactionDomainService {

    public PaymentTransaction register(UUID paymentIntentId, BigDecimal amount) {
        return register(paymentIntentId, amount, "USD", "SUCCEEDED", null);
    }

    public PaymentTransaction register(UUID paymentIntentId, BigDecimal amount, String status, String reasonCode) {
        return register(paymentIntentId, amount, "USD", status, reasonCode);
    }

    public PaymentTransaction register(UUID paymentIntentId, BigDecimal amount, String currency, String status, String reasonCode) {
        if (paymentIntentId == null) {
            throw new DomainException("paymentIntentId is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new DomainException("amount must be greater than zero");
        }

        String validStatus = status == null || status.isBlank() ? "SUCCEEDED" : status.toUpperCase();
        if (!validStatus.equals("SUCCEEDED") && !validStatus.equals("FAILED") && !validStatus.equals("PENDING")) {
            throw new DomainException("Invalid transaction status");
        }

        return new PaymentTransaction(
                UUID.randomUUID(),
                paymentIntentId,
                amount,
                currency != null ? currency : "USD",
                validStatus,
                Instant.now(),
                Instant.now(),
                reasonCode
        );
    }
}

