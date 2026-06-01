package com.paymentplatform.embeddedpayments.transaction.infrastructure.repository;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.TransactionStatusHistory;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionStatusHistoryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionStatusHistoryRepositoryImpl implements TransactionStatusHistoryRepository {

    private final TransactionStatusHistoryJpaRepository jpaRepository;

    public TransactionStatusHistoryRepositoryImpl(TransactionStatusHistoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TransactionStatusHistory save(TransactionStatusHistory history) {
        return jpaRepository.save(history);
    }

    @Override
    public List<TransactionStatusHistory> findByTransactionIdOrderByCreatedAtAsc(UUID transactionId) {
        return jpaRepository.findByTransactionIdOrderByCreatedAtAsc(transactionId);
    }
}
