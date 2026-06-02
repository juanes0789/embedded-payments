package com.paymentplatform.embeddedpayments.payment.domain.services;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PaymentDomainService {

    public PaymentIntent createPaymentIntent(UUID merchantId, BigDecimal amount, String currency) {
        return createPaymentIntent(merchantId, amount, currency, null);
    }

    public PaymentIntent createPaymentIntent(UUID merchantId, BigDecimal amount, String currency, String description) {
        if (merchantId == null) {
            throw new DomainException("merchantId is required");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new DomainException("amount must be greater than zero");
        }

        if (currency == null || currency.isBlank() || currency.length() != 3) {
            throw new DomainException("currency must be a 3-letter ISO code");
        }

        return new PaymentIntent(
                UUID.randomUUID(),
                merchantId,
                null,
                amount,
                currency.toUpperCase(Locale.ROOT),
                "CREATED",
                description,
                java.time.Instant.now(),
                java.time.Instant.now()
        );
    }
}

