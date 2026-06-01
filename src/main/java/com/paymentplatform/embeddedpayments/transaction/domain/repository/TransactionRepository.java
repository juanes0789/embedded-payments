package com.paymentplatform.embeddedpayments.transaction.domain.repository;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    PaymentTransaction save(PaymentTransaction transaction);

    Optional<PaymentTransaction> findById(UUID id);

    org.springframework.data.domain.Page<PaymentTransaction> findByMerchantId(UUID merchantId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<PaymentTransaction> findByMerchantIdAndStatus(UUID merchantId, String status, org.springframework.data.domain.Pageable pageable);
}

