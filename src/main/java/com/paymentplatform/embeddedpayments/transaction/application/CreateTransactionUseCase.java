package com.paymentplatform.embeddedpayments.transaction.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import com.paymentplatform.embeddedpayments.transaction.domain.services.TransactionDomainService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionDomainService transactionDomainService;
    private final com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionStatusHistoryRepository transactionStatusHistoryRepository;

    public CreateTransactionUseCase(TransactionRepository transactionRepository,
                                    PaymentRepository paymentRepository,
                                    TransactionDomainService transactionDomainService,
                                    com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionStatusHistoryRepository transactionStatusHistoryRepository) {
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.transactionDomainService = transactionDomainService;
        this.transactionStatusHistoryRepository = transactionStatusHistoryRepository;
    }

    public PaymentTransaction execute(UUID merchantId, UUID paymentIntentId, BigDecimal amount) {
        return execute(merchantId, paymentIntentId, amount, "SUCCEEDED", null);
    }

    public PaymentTransaction execute(UUID merchantId, UUID paymentIntentId, BigDecimal amount, String status, String reasonCode) {
        PaymentIntent intent = paymentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent not found",
                        List.of("paymentIntentId: " + paymentIntentId)
                ));

        if (!intent.getMerchantId().equals(merchantId)) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "PAYMENT_INTENT_ACCESS_DENIED",
                    "Payment intent does not belong to authenticated merchant",
                    List.of("merchantId: " + merchantId, "paymentIntentId: " + paymentIntentId)
            );
        }

        PaymentTransaction transaction = transactionDomainService.register(paymentIntentId, amount, intent.getCurrency(), status, reasonCode);
        PaymentTransaction savedTransaction = transactionRepository.save(transaction);

        // Guardar en la bitácora inmutable el cambio de estado inicial (HU 4.1)
        com.paymentplatform.embeddedpayments.transaction.domain.entity.TransactionStatusHistory history = 
                new com.paymentplatform.embeddedpayments.transaction.domain.entity.TransactionStatusHistory(
                        UUID.randomUUID(),
                        savedTransaction.getId(),
                        null,
                        savedTransaction.getStatus(),
                        "API_CLIENT", // Actor originario
                        savedTransaction.getReasonCode(),
                        savedTransaction.getCreatedAt()
                );
        transactionStatusHistoryRepository.save(history);

        return savedTransaction;
    }
}
