package com.paymentplatform.embeddedpayments.transaction.api;

import com.paymentplatform.embeddedpayments.transaction.application.CreateTransactionUseCase;
import com.paymentplatform.embeddedpayments.transaction.application.GetTransactionDetailUseCase;
import com.paymentplatform.embeddedpayments.transaction.application.ListTransactionsUseCase;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.shared.security.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;
    private final GetTransactionDetailUseCase getTransactionDetailUseCase;
    private final AuthenticationService authenticationService;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                 ListTransactionsUseCase listTransactionsUseCase,
                                 GetTransactionDetailUseCase getTransactionDetailUseCase,
                                 AuthenticationService authenticationService) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
        this.getTransactionDetailUseCase = getTransactionDetailUseCase;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        PaymentTransaction transaction = createTransactionUseCase.execute(
                merchantId,
                request.paymentIntentId(),
                request.amount(),
                request.status() != null ? request.status() : "SUCCEEDED",
                request.reasonCode()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TransactionResponse(
                        transaction.getId(),
                        transaction.getPaymentIntentId(),
                        transaction.getAmount(),
                        transaction.getStatus().equalsIgnoreCase("SUCCEEDED") ? "COMPLETED" : transaction.getStatus(),
                        transaction.getCreatedAt(),
                        transaction.getReasonCode()
                ));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {

        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key/token", List.of());
        }

        Page<ListTransactionsUseCase.TransactionDto> result = listTransactionsUseCase.execute(merchantId, status, page, pageSize);

        return ResponseEntity.ok(Map.of(
                "items", result.getContent(),
                "total", result.getTotalElements(),
                "page", page,
                "pageSize", pageSize
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetTransactionDetailUseCase.TransactionDetailDto> getById(@PathVariable UUID id) {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key/token", List.of());
        }

        GetTransactionDetailUseCase.TransactionDetailDto detail = getTransactionDetailUseCase.execute(merchantId, id);
        return ResponseEntity.ok(detail);
    }

    public record CreateTransactionRequest(
            @NotNull UUID paymentIntentId,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            String status,
            String reasonCode) {
    }

    public record TransactionResponse(
            UUID id,
            UUID paymentIntentId,
            BigDecimal amount,
            String status,
            Instant createdAt,
            String reasonCode) {
    }
}
