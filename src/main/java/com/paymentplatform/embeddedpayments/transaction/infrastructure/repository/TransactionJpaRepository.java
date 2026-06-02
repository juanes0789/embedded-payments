package com.paymentplatform.embeddedpayments.transaction.infrastructure.repository;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionJpaRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByPaymentIntentId(UUID paymentIntentId);

    @Query("SELECT t FROM PaymentTransaction t WHERE t.paymentIntentId IN (SELECT p.id FROM PaymentIntent p WHERE p.merchantId = :merchantId) ORDER BY t.createdAt DESC")
    List<PaymentTransaction> findByMerchantId(@Param("merchantId") UUID merchantId);

    @Query("SELECT t FROM PaymentTransaction t WHERE t.paymentIntentId IN (SELECT p.id FROM PaymentIntent p WHERE p.merchantId = :merchantId) ORDER BY t.createdAt DESC")
    Page<PaymentTransaction> findByMerchantId(@Param("merchantId") UUID merchantId, Pageable pageable);

    @Query("SELECT t FROM PaymentTransaction t WHERE t.paymentIntentId IN (SELECT p.id FROM PaymentIntent p WHERE p.merchantId = :merchantId) AND t.status = :status ORDER BY t.createdAt DESC")
    Page<PaymentTransaction> findByMerchantIdAndStatus(@Param("merchantId") UUID merchantId, @Param("status") String status, Pageable pageable);
}



