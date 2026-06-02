package com.paymentplatform.embeddedpayments.refund.infrastructure.repository;

import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundJpaRepository extends JpaRepository<Refund, UUID> {

    java.util.List<Refund> findByTransactionId(UUID transactionId);
}

