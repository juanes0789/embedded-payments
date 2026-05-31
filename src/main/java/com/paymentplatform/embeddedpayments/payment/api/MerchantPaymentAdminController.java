package com.paymentplatform.embeddedpayments.payment.api;

import com.paymentplatform.embeddedpayments.payment.application.CancelPaymentIntentUseCase;
import com.paymentplatform.embeddedpayments.shared.security.AuthenticationService;
import com.paymentplatform.embeddedpayments.refund.application.CreateRefundUseCase;
import com.paymentplatform.embeddedpayments.transaction.application.AuthorizeTransactionUseCase;
import com.paymentplatform.embeddedpayments.transaction.application.UpdateTransactionStatusUseCase;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/payments")
public class MerchantPaymentAdminController {

    private final AuthenticationService authenticationService;
    private final CancelPaymentIntentUseCase cancelPaymentIntentUseCase;
    private final AuthorizeTransactionUseCase authorizeTransactionUseCase;
    private final UpdateTransactionStatusUseCase updateTransactionStatusUseCase;
    private final CreateRefundUseCase createRefundUseCase;

    public MerchantPaymentAdminController(AuthenticationService authenticationService,
                                          CancelPaymentIntentUseCase cancelPaymentIntentUseCase,
                                          AuthorizeTransactionUseCase authorizeTransactionUseCase,
                                          UpdateTransactionStatusUseCase updateTransactionStatusUseCase,
                                          CreateRefundUseCase createRefundUseCase) {
        this.authenticationService = authenticationService;
        this.cancelPaymentIntentUseCase = cancelPaymentIntentUseCase;
        this.authorizeTransactionUseCase = authorizeTransactionUseCase;
        this.updateTransactionStatusUseCase = updateTransactionStatusUseCase;
        this.createRefundUseCase = createRefundUseCase;
    }

    @PatchMapping("/intents/{id}/cancel")
    public ResponseEntity<PaymentIntentResponse> cancelIntent(@PathVariable UUID id) {
        UUID merchantId = resolveMerchantId();
        PaymentIntent intent = cancelPaymentIntentUseCase.execute(merchantId, id);
        return ResponseEntity.ok(toResponse(intent));
    }

    @PatchMapping("/intents/{id}/authorize")
    public ResponseEntity<TransactionResponse> authorizeIntent(@PathVariable UUID id) {
        UUID merchantId = resolveMerchantId();
        PaymentTransaction transaction = authorizeTransactionUseCase.execute(merchantId, id);
        return ResponseEntity.ok(toResponse(merchantId, transaction));
    }

    @PatchMapping("/transactions/{id}/status")
    public ResponseEntity<TransactionResponse> updateTransactionStatus(@PathVariable UUID id,
                                                                      @Valid @RequestBody UpdateTransactionStatusRequest request) {
        UUID merchantId = resolveMerchantId();
        PaymentTransaction transaction = updateTransactionStatusUseCase.execute(merchantId, id, request.status());
        return ResponseEntity.ok(toResponse(merchantId, transaction));
    }

    @PostMapping("/transactions/{id}/refunds")
    public ResponseEntity<RefundResponse> refundTransaction(@PathVariable UUID id,
                                                            @Valid @RequestBody CreateRefundRequest request) {
        UUID merchantId = resolveMerchantId();
        Refund refund = createRefundUseCase.execute(merchantId, id, request.amount(), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(merchantId, refund));
    }

    private UUID resolveMerchantId() {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new com.paymentplatform.embeddedpayments.shared.exception.DomainException(
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Missing or invalid JWT merchant context",
                    List.of()
            );
        }
        return merchantId;
    }

    private PaymentIntentResponse toResponse(PaymentIntent intent) {
        return new PaymentIntentResponse(
                intent.getId(),
                intent.getMerchantId(),
                intent.getAmount(),
                intent.getCurrency(),
                intent.getStatus(),
                intent.getCreatedAt(),
                intent.getUpdatedAt()
        );
    }

    private TransactionResponse toResponse(UUID merchantId, PaymentTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                merchantId,
                transaction.getPaymentIntentId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    private RefundResponse toResponse(UUID merchantId, Refund refund) {
        return new RefundResponse(
                refund.getId(),
                merchantId,
                refund.getTransactionId(),
                refund.getAmount(),
                refund.getReason(),
                refund.getStatus(),
                refund.getCreatedAt(),
                refund.getUpdatedAt()
        );
    }

    public record UpdateTransactionStatusRequest(@NotBlank String status) {
    }

    public record CreateRefundRequest(@NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                                      @NotBlank String reason) {
    }

    public record PaymentIntentResponse(UUID id,
                                        UUID merchantId,
                                        BigDecimal amount,
                                        String currency,
                                        String status,
                                        Instant createdAt,
                                        Instant updatedAt) {
    }

    public record TransactionResponse(UUID id,
                                      UUID merchantId,
                                      UUID paymentIntentId,
                                      BigDecimal amount,
                                      String currency,
                                      String status,
                                      Instant createdAt,
                                      Instant updatedAt) {
    }

    public record RefundResponse(UUID id,
                                 UUID merchantId,
                                 UUID transactionId,
                                 BigDecimal amount,
                                 String reason,
                                 String status,
                                 Instant createdAt,
                                 Instant updatedAt) {
    }
}

