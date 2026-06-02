package com.paymentplatform.embeddedpayments.transaction.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateTransactionStatusUseCase {

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;

    public UpdateTransactionStatusUseCase(TransactionRepository transactionRepository,
                                          PaymentRepository paymentRepository) {
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentTransaction execute(UUID merchantId, UUID transactionId, String targetStatus) {
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

        String normalizedStatus = targetStatus == null ? "" : targetStatus.trim().toUpperCase();
        switch (normalizedStatus) {
            case "SUCCEEDED" -> {
                if (!"SUCCEEDED".equalsIgnoreCase(transaction.getStatus())) {
                    transaction.markSucceeded();
                    paymentRepository.save(markIntentSucceeded(intent));
                    return transactionRepository.save(transaction);
                }
                return transaction;
            }
            case "FAILED" -> {
                if (!"FAILED".equalsIgnoreCase(transaction.getStatus())) {
                    transaction.markFailed();
                    paymentRepository.save(markIntentFailed(intent));
                    return transactionRepository.save(transaction);
                }
                return transaction;
            }
            case "CANCELED" -> {
                if (!"CANCELED".equalsIgnoreCase(transaction.getStatus())) {
                    transaction.cancel();
                    paymentRepository.save(markIntentCanceled(intent));
                    return transactionRepository.save(transaction);
                }
                return transaction;
            }
            case "REFUNDED" -> {
                if (!"REFUNDED".equalsIgnoreCase(transaction.getStatus())) {
                    transaction.markRefunded();
                    paymentRepository.save(markIntentSucceeded(intent));
                    return transactionRepository.save(transaction);
                }
                return transaction;
            }
            default -> throw new DomainException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_TRANSACTION_STATUS",
                    "Unsupported transaction status",
                    List.of("status: " + targetStatus)
            );
        }
    }

    private PaymentIntent markIntentSucceeded(PaymentIntent intent) {
        if (!"SUCCEEDED".equalsIgnoreCase(intent.getStatus())) {
            intent.markSucceeded();
        }
        return intent;
    }

    private PaymentIntent markIntentFailed(PaymentIntent intent) {
        if (!"FAILED".equalsIgnoreCase(intent.getStatus())) {
            intent.markFailed();
        }
        return intent;
    }

    private PaymentIntent markIntentCanceled(PaymentIntent intent) {
        if (!"CANCELED".equalsIgnoreCase(intent.getStatus())) {
            intent.cancel();
        }
        return intent;
    }
}

