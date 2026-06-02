package com.paymentplatform.embeddedpayments.transaction.infrastructure.repository;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<PaymentTransaction, UUID> {

    java.util.Optional<PaymentTransaction> findByPaymentIntentId(UUID paymentIntentId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT t.* FROM transactions t JOIN payment_intents p ON t.payment_intent_id = p.id WHERE p.merchant_id = :merchantId ORDER BY t.created_at DESC", nativeQuery = true)
    java.util.List<PaymentTransaction> findByMerchantId(@org.springframework.data.repository.query.Param("merchantId") UUID merchantId);
}

