package com.paymentplatform.embeddedpayments.payment.domain.repository;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    PaymentIntent save(PaymentIntent paymentIntent);

    Optional<PaymentIntent> findById(UUID id);

    List<PaymentIntent> findAll();
    List<PaymentIntent> findAllById(Iterable<UUID> ids);
}

