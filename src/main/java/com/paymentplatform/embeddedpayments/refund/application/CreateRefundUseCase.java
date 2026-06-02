package com.paymentplatform.embeddedpayments.refund.application;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
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
public class CreateRefundUseCase {

    private final RefundRepository refundRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final RefundDomainService refundDomainService;

    public CreateRefundUseCase(RefundRepository refundRepository,
                               TransactionRepository transactionRepository,
                               PaymentRepository paymentRepository,
                               MerchantRepository merchantRepository,
                               RefundDomainService refundDomainService) {
        this.refundRepository = refundRepository;
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
        this.refundDomainService = refundDomainService;
    }

    @Transactional
    public Refund execute(UUID merchantId, UUID transactionId, BigDecimal amount, String reason) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "MERCHANT_NOT_FOUND",
                        "Merchant not found",
                        List.of("merchantId: " + merchantId)
                ));

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

        if (!merchant.isActive()) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "MERCHANT_INACTIVE",
                    "Merchant must be active to create refunds",
                    List.of("merchantId: " + merchantId, "status: " + merchant.getStatus())
            );
        }

        if (!paymentIntent.getMerchantId().equals(merchantId)) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "TRANSACTION_ACCESS_DENIED",
                    "Transaction does not belong to authenticated merchant",
                    List.of("merchantId: " + merchantId, "transactionId: " + transactionId)
            );
        }

        if (!"SUCCEEDED".equalsIgnoreCase(transaction.getStatus()) && !"REFUNDED".equalsIgnoreCase(transaction.getStatus())) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "TRANSACTION_NOT_REFUNDABLE",
                    "Transaction cannot be refunded in its current state",
                    List.of("transactionId: " + transactionId, "status: " + transaction.getStatus())
            );
        }

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

        Refund refund = refundDomainService.create(transactionId, amount, reason);
        Refund savedRefund = refundRepository.save(refund);

        BigDecimal refundableRemaining = transaction.getAmount().subtract(alreadyRefunded.add(amount));
        if (refundableRemaining.compareTo(BigDecimal.ZERO) == 0) {
            transaction.markRefunded();
            transactionRepository.save(transaction);
        }

        return savedRefund;
    }
}
