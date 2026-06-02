package com.paymentplatform.embeddedpayments.transaction.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GetTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;

    public GetTransactionUseCase(TransactionRepository transactionRepository,
                                 PaymentRepository paymentRepository) {
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
    }

    public TransactionView execute(UUID merchantId, UUID transactionId) {
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
                        "Payment intent not found",
                        List.of("paymentIntentId: " + transaction.getPaymentIntentId())
                ));

        if (!intent.belongsTo(merchantId)) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "TRANSACTION_ACCESS_DENIED",
                    "Transaction does not belong to authenticated merchant",
                    List.of("merchantId: " + merchantId, "transactionId: " + transactionId)
            );
        }

        return new TransactionView(
                transaction.getId(),
                intent.getMerchantId(),
                transaction.getPaymentIntentId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    public record TransactionView(UUID id,
                                  UUID merchantId,
                                  UUID paymentIntentId,
                                  java.math.BigDecimal amount,
                                  String currency,
                                  String status,
                                  Instant createdAt,
                                  Instant updatedAt) {
    }
}

