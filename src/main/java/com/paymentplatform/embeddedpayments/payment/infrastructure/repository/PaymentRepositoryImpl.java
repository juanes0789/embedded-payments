package com.paymentplatform.embeddedpayments.payment.infrastructure.repository;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PaymentIntent save(PaymentIntent paymentIntent) {
        return jpaRepository.save(paymentIntent);
    }

    @Override
    public Optional<PaymentIntent> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<PaymentIntent> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<PaymentIntent> findAllById(Iterable<UUID> ids) {
        return jpaRepository.findAllById(ids);
    }
}

