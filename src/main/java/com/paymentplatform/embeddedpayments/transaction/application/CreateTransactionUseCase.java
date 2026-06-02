package com.paymentplatform.embeddedpayments.transaction.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import com.paymentplatform.embeddedpayments.transaction.domain.services.TransactionDomainService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionDomainService transactionDomainService;

    public CreateTransactionUseCase(TransactionRepository transactionRepository,
                                    PaymentRepository paymentRepository,
                                    MerchantRepository merchantRepository,
                                    TransactionDomainService transactionDomainService) {
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
        this.transactionDomainService = transactionDomainService;
    }

    @Transactional
    public PaymentTransaction execute(UUID merchantId, UUID paymentIntentId, BigDecimal amount) {
        PaymentIntent intent = paymentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent not found",
                        List.of("paymentIntentId: " + paymentIntentId)
                ));

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "MERCHANT_NOT_FOUND",
                        "Merchant not found",
                        List.of("merchantId: " + merchantId)
                ));

        if (!intent.getMerchantId().equals(merchantId)) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "PAYMENT_INTENT_ACCESS_DENIED",
                    "Payment intent does not belong to authenticated merchant",
                    List.of("merchantId: " + merchantId, "paymentIntentId: " + paymentIntentId)
            );
        }

        if (!merchant.isActive()) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "MERCHANT_INACTIVE",
                    "Merchant must be active to process payments",
                    List.of("merchantId: " + merchantId, "status: " + merchant.getStatus())
            );
        }

        if (!"CREATED".equalsIgnoreCase(intent.getStatus()) && !"PROCESSING".equalsIgnoreCase(intent.getStatus())) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "PAYMENT_INTENT_NOT_PROCESSABLE",
                    "Payment intent cannot be processed in its current state",
                    List.of("paymentIntentId: " + paymentIntentId, "status: " + intent.getStatus())
            );
        }

        transactionRepository.findByPaymentIntentId(paymentIntentId).ifPresent(existing -> {
            if (!existing.isTerminal()) {
                throw new DomainException(
                        HttpStatus.CONFLICT,
                        "TRANSACTION_ALREADY_EXISTS",
                        "A transaction is already being processed for this payment intent",
                        List.of("transactionId: " + existing.getId(), "paymentIntentId: " + paymentIntentId)
                );
            }
        });

        if ("CREATED".equalsIgnoreCase(intent.getStatus())) {
            intent.markProcessing();
            paymentRepository.save(intent);
        }

        PaymentTransaction transaction = transactionDomainService.register(paymentIntentId, amount, intent.getCurrency());
        return transactionRepository.save(transaction);
    }
}
