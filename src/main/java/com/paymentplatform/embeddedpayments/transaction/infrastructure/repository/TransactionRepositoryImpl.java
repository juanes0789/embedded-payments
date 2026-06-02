package com.paymentplatform.embeddedpayments.transaction.infrastructure.repository;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import java.util.Optional;
import java.util.UUID;
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
    public org.springframework.data.domain.Page<PaymentTransaction> findByMerchantId(UUID merchantId, org.springframework.data.domain.Pageable pageable) {
        return jpaRepository.findByMerchantId(merchantId, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<PaymentTransaction> findByMerchantIdAndStatus(UUID merchantId, String status, org.springframework.data.domain.Pageable pageable) {
        return jpaRepository.findByMerchantIdAndStatus(merchantId, status, pageable);
    }
}

