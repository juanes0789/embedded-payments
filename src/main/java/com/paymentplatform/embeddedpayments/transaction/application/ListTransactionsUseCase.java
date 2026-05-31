package com.paymentplatform.embeddedpayments.transaction.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ListTransactionsUseCase {

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;

    public ListTransactionsUseCase(TransactionRepository transactionRepository,
                                   PaymentRepository paymentRepository) {
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
    }

    public List<TransactionView> execute(UUID merchantId) {
        return transactionRepository.findAll().stream()
                .filter(transaction -> belongsToMerchant(transaction, merchantId))
                .sorted(Comparator.comparing(PaymentTransaction::getCreatedAt).reversed())
                .map(this::toView)
                .toList();
    }

    private boolean belongsToMerchant(PaymentTransaction transaction, UUID merchantId) {
        PaymentIntent intent = paymentRepository.findById(transaction.getPaymentIntentId())
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent not found",
                        List.of("paymentIntentId: " + transaction.getPaymentIntentId())
                ));
        return intent.belongsTo(merchantId);
    }

    private TransactionView toView(PaymentTransaction transaction) {
        PaymentIntent intent = paymentRepository.findById(transaction.getPaymentIntentId())
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent not found",
                        List.of("paymentIntentId: " + transaction.getPaymentIntentId())
                ));

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

