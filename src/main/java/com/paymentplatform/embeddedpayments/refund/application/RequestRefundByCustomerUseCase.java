package com.paymentplatform.embeddedpayments.refund.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import com.paymentplatform.embeddedpayments.refund.domain.repository.RefundRepository;
import com.paymentplatform.embeddedpayments.refund.domain.services.RefundDomainService;
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
public class RequestRefundByCustomerUseCase {

    private final RefundRepository refundRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final RefundDomainService refundDomainService;

    public RequestRefundByCustomerUseCase(RefundRepository refundRepository,
                                          TransactionRepository transactionRepository,
                                          PaymentRepository paymentRepository,
                                          RefundDomainService refundDomainService) {
        this.refundRepository = refundRepository;
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.refundDomainService = refundDomainService;
    }

    @Transactional
    public Refund execute(UUID userId, UUID transactionId, BigDecimal amount, String reason) {
        if (userId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid user", List.of());
        }

        PaymentTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "TRANSACTION_NOT_FOUND",
                        "Transaction not found",
                        List.of("transactionId: " + transactionId)
                ));

        PaymentIntent paymentIntent = paymentRepository.findById(transaction.getPaymentIntentId())
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent not found",
                        List.of("paymentIntentId: " + transaction.getPaymentIntentId())
                ));

        if (paymentIntent.getCustomerId() == null || !paymentIntent.getCustomerId().equals(userId)) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "REFUND_ACCESS_DENIED",
                    "Refund request can only be created by the customer who paid",
                    List.of("userId: " + userId, "transactionId: " + transactionId)
            );
        }

        if (!"SUCCEEDED".equalsIgnoreCase(transaction.getStatus())) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "TRANSACTION_NOT_REFUNDABLE",
                    "Transaction cannot be refunded in its current state",
                    List.of("transactionId: " + transactionId, "status: " + transaction.getStatus())
            );
        }

        // sum already refunded
        BigDecimal alreadyRefunded = refundRepository.findByTransactionId(transactionId).stream()
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (alreadyRefunded.add(amount).compareTo(transaction.getAmount()) > 0) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "REFUND_AMOUNT_EXCEEDED",
                    "Refund amount exceeds available refundable amount",
                    List.of(
                            "transactionId: " + transactionId,
                            "transactionAmount: " + transaction.getAmount().toPlainString(),
                            "alreadyRefunded: " + alreadyRefunded.toPlainString(),
                            "requestedRefund: " + amount.toPlainString()
                    )
            );
        }

        Refund refund = refundDomainService.createWithStatus(transactionId, amount, reason, "PENDING");
        Refund savedRefund = refundRepository.save(refund);

        // set transaction to pending refund state
        transaction.requestRefund();
        transactionRepository.save(transaction);

        return savedRefund;
    }
}

