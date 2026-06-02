package com.paymentplatform.embeddedpayments.transaction.infrastructure.repository;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;

    public TransactionRepositoryImpl(TransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PaymentTransaction save(PaymentTransaction transaction) {
        return jpaRepository.save(transaction);
    }

    @Override
    public Optional<PaymentTransaction> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<PaymentTransaction> findByPaymentIntentId(UUID paymentIntentId) {
        return jpaRepository.findByPaymentIntentId(paymentIntentId);
    }

    @Override
    public List<PaymentTransaction> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<PaymentTransaction> findByMerchantId(UUID merchantId) {
        return jpaRepository.findByMerchantId(merchantId);
    }

    @Override
    public Page<PaymentTransaction> findByMerchantId(UUID merchantId, Pageable pageable) {
        return jpaRepository.findByMerchantId(merchantId, pageable);
    }

    @Override
    public Page<PaymentTransaction> findByMerchantIdAndStatus(UUID merchantId, String status, Pageable pageable) {
        return jpaRepository.findByMerchantIdAndStatus(merchantId, status, pageable);
    }
}

