package com.paymentplatform.embeddedpayments.transaction.infrastructure.repository;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.TransactionStatusHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionStatusHistoryJpaRepository extends JpaRepository<TransactionStatusHistory, UUID> {

    List<TransactionStatusHistory> findByTransactionIdOrderByCreatedAtAsc(UUID transactionId);
}
