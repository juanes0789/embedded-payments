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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ListTransactionsUseCase.class);

    public ListTransactionsUseCase(TransactionRepository transactionRepository,
                                   PaymentRepository paymentRepository) {
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
    }

    public List<TransactionView> execute(UUID merchantId) {
        log.debug("ListTransactionsUseCase.execute start for merchantId={}", merchantId);
        var start = System.currentTimeMillis();

        var all = transactionRepository.findByMerchantId(merchantId);
        log.debug("Fetched {} transactions for merchantId {} from repository", all == null ? 0 : all.size(), merchantId);

        // Batch load related PaymentIntent entities to avoid N+1 queries
        var intentIds = all.stream().map(PaymentTransaction::getPaymentIntentId).distinct().toList();
        var intents = paymentRepository.findAllById(intentIds).stream()
                .collect(java.util.stream.Collectors.toMap(PaymentIntent::getId, i -> i));

        var result = all.stream()
                .sorted(Comparator.comparing(PaymentTransaction::getCreatedAt).reversed())
                .map(tx -> toView(tx, intents.get(tx.getPaymentIntentId())))
                .toList();

        var elapsed = System.currentTimeMillis() - start;
        log.debug("ListTransactionsUseCase.execute completed for merchantId={} -> {} items ({} ms)", merchantId, result.size(), elapsed);
        return result;
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
        // kept for compatibility; should not be used when batch intents are provided
        PaymentIntent intent = paymentRepository.findById(transaction.getPaymentIntentId())
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent not found",
                        List.of("paymentIntentId: " + transaction.getPaymentIntentId())
                ));

        return toView(transaction, intent);
    }

    private TransactionView toView(PaymentTransaction transaction, PaymentIntent intent) {
        if (intent == null) {
            throw new DomainException(HttpStatus.NOT_FOUND, "PAYMENT_INTENT_NOT_FOUND", "Payment intent not found", List.of("paymentIntentId: " + transaction.getPaymentIntentId()));
        }

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

