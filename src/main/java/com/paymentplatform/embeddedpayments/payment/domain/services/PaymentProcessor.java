package com.paymentplatform.embeddedpayments.payment.domain.services;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProcessor {
    ProcessorResult process(UUID paymentIntentId, UUID customerId, BigDecimal amount, String currency);
}

