package com.paymentplatform.embeddedpayments.refund.domain.services;

import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RefundDomainService {

    public Refund create(UUID transactionId, BigDecimal amount, String reason) {
        if (transactionId == null) {
            throw new DomainException("transactionId is required");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new DomainException("refund amount must be greater than zero");
        }

        if (reason == null || reason.isBlank()) {
            throw new DomainException("refund reason is required");
        }

        return new Refund(UUID.randomUUID(), transactionId, amount, reason, "PENDING");
    }
}

