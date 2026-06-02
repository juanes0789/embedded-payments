package com.paymentplatform.embeddedpayments.transaction.domain.repository;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.TransactionStatusHistory;
import java.util.List;
import java.util.UUID;

public interface TransactionStatusHistoryRepository {

    TransactionStatusHistory save(TransactionStatusHistory history);

    List<TransactionStatusHistory> findByTransactionIdOrderByCreatedAtAsc(UUID transactionId);
}
