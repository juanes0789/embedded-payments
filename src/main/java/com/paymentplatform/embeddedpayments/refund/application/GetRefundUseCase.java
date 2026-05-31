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
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GetRefundUseCase {

    private final RefundRepository refundRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;

    public GetRefundUseCase(RefundRepository refundRepository,
                            TransactionRepository transactionRepository,
                            PaymentRepository paymentRepository) {
        this.refundRepository = refundRepository;
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
    }

    public RefundView execute(UUID merchantId, UUID refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "REFUND_NOT_FOUND",
                        "Refund not found",
                        List.of("refundId: " + refundId)
                ));

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

        if (!intent.belongsTo(merchantId)) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "REFUND_ACCESS_DENIED",
                    "Refund does not belong to authenticated merchant",
                    List.of("merchantId: " + merchantId, "refundId: " + refundId)
            );
        }

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

