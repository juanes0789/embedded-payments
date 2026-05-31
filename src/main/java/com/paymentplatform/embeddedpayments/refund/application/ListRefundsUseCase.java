package com.paymentplatform.embeddedpayments.refund.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import com.paymentplatform.embeddedpayments.refund.domain.repository.RefundRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ListRefundsUseCase {

    private final RefundRepository refundRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;

    public ListRefundsUseCase(RefundRepository refundRepository,
                              TransactionRepository transactionRepository,
                              PaymentRepository paymentRepository) {
        this.refundRepository = refundRepository;
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
    }

    public List<RefundView> execute(UUID merchantId) {
        return refundRepository.findAll().stream()
                .filter(refund -> belongsToMerchant(refund.getTransactionId(), merchantId))
                .sorted(Comparator.comparing(Refund::getCreatedAt).reversed())
                .map(this::toView)
                .toList();
    }

    private boolean belongsToMerchant(UUID transactionId, UUID merchantId) {
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
        return intent.belongsTo(merchantId);
    }

    private RefundView toView(Refund refund) {
        PaymentTransaction transaction = transactionRepository.findById(refund.getTransactionId())
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "TRANSACTION_NOT_FOUND",
                        "Transaction not found",
                        List.of("transactionId: " + refund.getTransactionId())
                ));

        PaymentIntent intent = paymentRepository.findById(transaction.getPaymentIntentId())
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent not found",
                        List.of("paymentIntentId: " + transaction.getPaymentIntentId())
                ));

        return new RefundView(
                refund.getId(),
                intent.getMerchantId(),
                refund.getTransactionId(),
                refund.getAmount(),
                refund.getReason(),
                refund.getStatus(),
                refund.getCreatedAt(),
                refund.getUpdatedAt()
        );
    }

    public record RefundView(UUID id,
                             UUID merchantId,
                             UUID transactionId,
                             BigDecimal amount,
                             String reason,
                             String status,
                             Instant createdAt,
                             Instant updatedAt) {
    }
}

