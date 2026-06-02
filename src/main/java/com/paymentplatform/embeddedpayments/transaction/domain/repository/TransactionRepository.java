package com.paymentplatform.embeddedpayments.transaction.domain.repository;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    PaymentTransaction save(PaymentTransaction transaction);

    Optional<PaymentTransaction> findById(UUID id);

    Optional<PaymentTransaction> findByPaymentIntentId(UUID paymentIntentId);

    List<PaymentTransaction> findAll();
    List<PaymentTransaction> findByMerchantId(UUID merchantId);
}

