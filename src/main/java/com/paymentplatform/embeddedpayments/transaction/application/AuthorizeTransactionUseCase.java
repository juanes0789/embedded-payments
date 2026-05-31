package com.paymentplatform.embeddedpayments.transaction.application;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorizeTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;

    public AuthorizeTransactionUseCase(TransactionRepository transactionRepository,
                                       PaymentRepository paymentRepository,
                                       MerchantRepository merchantRepository) {
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public PaymentTransaction execute(UUID merchantId, UUID paymentIntentId) {
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

        if (!intent.belongsTo(merchantId)) {
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
                    "Merchant must be active to authorize payments",
                    List.of("merchantId: " + merchantId, "status: " + merchant.getStatus())
            );
        }

        if ("CANCELED".equalsIgnoreCase(intent.getStatus()) || "FAILED".equalsIgnoreCase(intent.getStatus())) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "PAYMENT_INTENT_NOT_AUTHORIZABLE",
                    "Payment intent cannot be authorized in its current state",
                    List.of("paymentIntentId: " + paymentIntentId, "status: " + intent.getStatus())
            );
        }

        PaymentTransaction transaction = transactionRepository.findByPaymentIntentId(paymentIntentId)
                .orElseGet(() -> new PaymentTransaction(
                        UUID.randomUUID(),
                        paymentIntentId,
                        intent.getAmount(),
                        intent.getCurrency(),
                        "PENDING"
                ));

        if ("SUCCEEDED".equalsIgnoreCase(transaction.getStatus())) {
            return transaction;
        }

        if (!"PENDING".equalsIgnoreCase(transaction.getStatus())) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "TRANSACTION_NOT_AUTHORIZABLE",
                    "Transaction cannot be authorized in its current state",
                    List.of("transactionId: " + transaction.getId(), "status: " + transaction.getStatus())
            );
        }

        transaction.markSucceeded();
        intent.markSucceeded();

        paymentRepository.save(intent);
        return transactionRepository.save(transaction);
    }
}

