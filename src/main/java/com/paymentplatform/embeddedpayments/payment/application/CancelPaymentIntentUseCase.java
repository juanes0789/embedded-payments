package com.paymentplatform.embeddedpayments.payment.application;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
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
public class CancelPaymentIntentUseCase {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;

    public CancelPaymentIntentUseCase(PaymentRepository paymentRepository,
                                      TransactionRepository transactionRepository,
                                      MerchantRepository merchantRepository) {
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public PaymentIntent execute(UUID merchantId, UUID paymentIntentId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "MERCHANT_NOT_FOUND",
                        "Merchant not found",
                        List.of("merchantId: " + merchantId)
                ));

        PaymentIntent intent = paymentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent not found",
                        List.of("paymentIntentId: " + paymentIntentId)
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
                    "Merchant must be active to cancel payments",
                    List.of("merchantId: " + merchantId, "status: " + merchant.getStatus())
            );
        }

        if (!intent.isCancelable()) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "PAYMENT_INTENT_NOT_CANCELABLE",
                    "Payment intent cannot be canceled in its current state",
                    List.of("paymentIntentId: " + paymentIntentId, "status: " + intent.getStatus())
            );
        }

        transactionRepository.findByPaymentIntentId(paymentIntentId).ifPresent(transaction -> {
            if (!transaction.isTerminal()) {
                transaction.cancel();
                transactionRepository.save(transaction);
            }
        });

        intent.cancel();
        return paymentRepository.save(intent);
    }
}

