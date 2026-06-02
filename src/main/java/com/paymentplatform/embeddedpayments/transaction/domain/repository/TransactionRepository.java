package com.paymentplatform.embeddedpayments.transaction.domain.repository;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionRepository {

    PaymentTransaction save(PaymentTransaction transaction);

    Optional<PaymentTransaction> findById(UUID id);

    Optional<PaymentTransaction> findByPaymentIntentId(UUID paymentIntentId);

    List<PaymentTransaction> findAll();

    List<PaymentTransaction> findByMerchantId(UUID merchantId);

    Page<PaymentTransaction> findByMerchantId(UUID merchantId, Pageable pageable);

    Page<PaymentTransaction> findByMerchantIdAndStatus(UUID merchantId, String status, Pageable pageable);
}



