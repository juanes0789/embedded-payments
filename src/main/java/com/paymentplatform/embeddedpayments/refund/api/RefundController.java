package com.paymentplatform.embeddedpayments.refund.api;

import com.paymentplatform.embeddedpayments.refund.application.GetRefundUseCase;
import com.paymentplatform.embeddedpayments.refund.application.CreateRefundUseCase;
import com.paymentplatform.embeddedpayments.refund.application.ListRefundsUseCase;
import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.shared.security.AuthenticationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/refunds")
public class RefundController {

    private final CreateRefundUseCase createRefundUseCase;
    private final ListRefundsUseCase listRefundsUseCase;
    private final GetRefundUseCase getRefundUseCase;
    private final AuthenticationService authenticationService;
    private final com.paymentplatform.embeddedpayments.refund.application.RequestRefundByCustomerUseCase requestRefundByCustomerUseCase;
    private final com.paymentplatform.embeddedpayments.refund.application.ApproveRefundUseCase approveRefundUseCase;
    private final com.paymentplatform.embeddedpayments.refund.application.RejectRefundUseCase rejectRefundUseCase;

    public RefundController(CreateRefundUseCase createRefundUseCase,
                            ListRefundsUseCase listRefundsUseCase,
                            GetRefundUseCase getRefundUseCase,
                            AuthenticationService authenticationService,
                            com.paymentplatform.embeddedpayments.refund.application.RequestRefundByCustomerUseCase requestRefundByCustomerUseCase,
                            com.paymentplatform.embeddedpayments.refund.application.ApproveRefundUseCase approveRefundUseCase,
                            com.paymentplatform.embeddedpayments.refund.application.RejectRefundUseCase rejectRefundUseCase) {
        this.createRefundUseCase = createRefundUseCase;
        this.listRefundsUseCase = listRefundsUseCase;
        this.getRefundUseCase = getRefundUseCase;
        this.authenticationService = authenticationService;
        this.requestRefundByCustomerUseCase = requestRefundByCustomerUseCase;
        this.approveRefundUseCase = approveRefundUseCase;
        this.rejectRefundUseCase = rejectRefundUseCase;
    }

    // Customer requests a refund for a transaction -> creates a pending refund
    @PostMapping("/request")
    public ResponseEntity<RefundResponse> requestRefund(@Valid @RequestBody RequestRefundRequest request) {
        var userId = authenticationService.getCurrentUserId();
        if (userId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid user", List.of());
        }

        var refund = requestRefundByCustomerUseCase.execute(userId, request.transactionId(), request.amount(), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(new RefundResponse(
                refund.getId(), null, refund.getTransactionId(), refund.getAmount(), refund.getReason(), refund.getStatus(), refund.getCreatedAt(), refund.getUpdatedAt()
        ));
    }

    public record RequestRefundRequest(@NotNull UUID transactionId,
                                       @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                                       @NotBlank String reason) {
    }

    @PostMapping
    public ResponseEntity<RefundResponse> create(@Valid @RequestBody CreateRefundRequest request) {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        Refund refund = createRefundUseCase.execute(merchantId, request.transactionId(), request.amount(), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RefundResponse(
                        refund.getId(),
                        merchantId,
                        refund.getTransactionId(),
                        refund.getAmount(),
                        refund.getReason(),
                        refund.getStatus(),
                        refund.getCreatedAt(),
                        refund.getUpdatedAt()
                ));
    }

    @GetMapping
    public ResponseEntity<PaginatedRefundResponse> list(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        List<RefundResponse> refunds = listRefundsUseCase.execute(merchantId).stream()
                .map(view -> new RefundResponse(
                        view.id(),
                        view.merchantId(),
                        view.transactionId(),
                        view.amount(),
                        view.reason(),
                        view.status(),
                        view.createdAt(),
                        view.updatedAt()
                ))
                .toList();

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((safePage - 1) * safePageSize, refunds.size());
        int toIndex = Math.min(fromIndex + safePageSize, refunds.size());

        return ResponseEntity.ok(new PaginatedRefundResponse(
                refunds.subList(fromIndex, toIndex),
                refunds.size(),
                safePage,
                safePageSize
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundResponse> getById(@PathVariable UUID id) {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        var view = getRefundUseCase.execute(merchantId, id);
        return ResponseEntity.ok(new RefundResponse(
                view.id(),
                view.merchantId(),
                view.transactionId(),
                view.amount(),
                view.reason(),
                view.status(),
                view.createdAt(),
                view.updatedAt()
        ));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RefundResponse> approve(@PathVariable UUID id) {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        var refund = approveRefundUseCase.execute(merchantId, id);
        return ResponseEntity.ok(new RefundResponse(refund.getId(), merchantId, refund.getTransactionId(), refund.getAmount(), refund.getReason(), refund.getStatus(), refund.getCreatedAt(), refund.getUpdatedAt()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RefundResponse> reject(@PathVariable UUID id) {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        var refund = rejectRefundUseCase.execute(merchantId, id);
        return ResponseEntity.ok(new RefundResponse(refund.getId(), merchantId, refund.getTransactionId(), refund.getAmount(), refund.getReason(), refund.getStatus(), refund.getCreatedAt(), refund.getUpdatedAt()));
    }

    public record CreateRefundRequest(@NotNull UUID transactionId,
                                      @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                                      @NotBlank String reason) {
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

    public record PaginatedRefundResponse(List<RefundResponse> items,
                                          int total,
                                          int page,
                                          int pageSize) {
    }
}
