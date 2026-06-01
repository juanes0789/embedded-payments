package com.paymentplatform.embeddedpayments.transaction.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.TransactionStatusHistory;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionStatusHistoryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GetTransactionDetailUseCase {

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionStatusHistoryRepository transactionStatusHistoryRepository;

    public GetTransactionDetailUseCase(TransactionRepository transactionRepository,
                                       PaymentRepository paymentRepository,
                                       TransactionStatusHistoryRepository transactionStatusHistoryRepository) {
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.transactionStatusHistoryRepository = transactionStatusHistoryRepository;
    }

    public TransactionDetailDto execute(UUID merchantId, UUID transactionId) {
        PaymentTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "TRANSACTION_NOT_FOUND",
                        "Transaction not found",
                        List.of("transactionId: " + transactionId)
                ));

        PaymentIntent intent = paymentRepository.findById(transaction.getPaymentIntentId())
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Associated payment intent not found",
                        List.of("paymentIntentId: " + transaction.getPaymentIntentId())
                ));

        if (!intent.getMerchantId().equals(merchantId)) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "TRANSACTION_ACCESS_DENIED",
                    "Transaction does not belong to authenticated merchant",
                    List.of("merchantId: " + merchantId, "transactionId: " + transactionId)
            );
        }

        List<TransactionStatusHistory> historyList = transactionStatusHistoryRepository
                .findByTransactionIdOrderByCreatedAtAsc(transactionId);

        List<StatusHistoryDto> statusHistory = historyList.stream()
                .map(h -> new StatusHistoryDto(
                        h.getId(),
                        h.getPreviousStatus(),
                        h.getNewStatus().equalsIgnoreCase("SUCCEEDED") ? "COMPLETED" : h.getNewStatus(),
                        h.getChangedBy(),
                        h.getReasonCode(),
                        h.getCreatedAt()
                ))
                .toList();

        return new TransactionDetailDto(
                transaction.getId(),
                merchantId,
                transaction.getAmount(),
                transaction.getCurrency() != null ? transaction.getCurrency() : "USD",
                transaction.getStatus().equalsIgnoreCase("SUCCEEDED") ? "COMPLETED" : transaction.getStatus(),
                transaction.getReasonCode(),
                "customer@example.com", // Por defecto
                "John Doe", // Por defecto
                transaction.getCreatedAt(),
                transaction.getCreatedAt(), // updatedAt
                statusHistory
        );
    }

    public record TransactionDetailDto(
            UUID id,
            UUID merchantId,
            BigDecimal amount,
            String currency,
            String status,
            String reasonCode,
            String customerEmail,
            String customerName,
            Instant createdAt,
            Instant updatedAt,
            List<StatusHistoryDto> statusHistory
    ) {}

    public record StatusHistoryDto(
            UUID id,
            String previousStatus,
            String newStatus,
            String changedBy,
            String reasonCode,
            Instant createdAt
    ) {}
}
