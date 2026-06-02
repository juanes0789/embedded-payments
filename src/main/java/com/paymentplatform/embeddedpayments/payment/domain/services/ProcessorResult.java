package com.paymentplatform.embeddedpayments.payment.domain.services;

public record ProcessorResult(
    String status,
    String processorReference,
    String message
) {
}

