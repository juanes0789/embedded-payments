package com.paymentplatform.embeddedpayments.transaction.api;

import com.paymentplatform.embeddedpayments.transaction.application.AuthorizeTransactionUseCase;
import com.paymentplatform.embeddedpayments.transaction.application.CreateTransactionUseCase;
import com.paymentplatform.embeddedpayments.auth.application.GetCurrentMerchantUseCase;
import com.paymentplatform.embeddedpayments.transaction.application.GetTransactionUseCase;
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
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final CreateTransactionUseCase createTransactionUseCase;
    private final AuthorizeTransactionUseCase authorizeTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;
    private final GetTransactionUseCase getTransactionUseCase;
    private final GetCurrentMerchantUseCase getCurrentMerchantUseCase;
    private final AuthenticationService authenticationService;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                 AuthorizeTransactionUseCase authorizeTransactionUseCase,
                                 ListTransactionsUseCase listTransactionsUseCase,
                                 GetTransactionUseCase getTransactionUseCase,
                                 GetCurrentMerchantUseCase getCurrentMerchantUseCase,
                                 AuthenticationService authenticationService) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.authorizeTransactionUseCase = authorizeTransactionUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
        this.getTransactionUseCase = getTransactionUseCase;
        this.getCurrentMerchantUseCase = getCurrentMerchantUseCase;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        UUID merchantId = resolveMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        PaymentTransaction transaction = createTransactionUseCase.execute(merchantId, request.paymentIntentId(), request.amount());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TransactionResponse(
                        transaction.getId(),
                        merchantId,
                        transaction.getPaymentIntentId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getStatus(),
                        transaction.getCreatedAt(),
                        transaction.getUpdatedAt()
                ));
    }

    @PostMapping("/{paymentIntentId}/authorize")
    public ResponseEntity<TransactionResponse> authorize(@PathVariable UUID paymentIntentId) {
        UUID merchantId = resolveMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        PaymentTransaction transaction = authorizeTransactionUseCase.execute(merchantId, paymentIntentId);
        return ResponseEntity.ok(new TransactionResponse(
                transaction.getId(),
                merchantId,
                transaction.getPaymentIntentId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        ));
    }

    @GetMapping
    public ResponseEntity<PaginatedTransactionResponse> list(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Entered list transactions endpoint: page={} pageSize={}", page, pageSize);
        UUID merchantId = resolveMerchantId();
        log.info("Resolved merchantId={}", merchantId);
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        log.debug("Calling ListTransactionsUseCase.execute for merchantId={}", merchantId);
        List<com.paymentplatform.embeddedpayments.transaction.application.ListTransactionsUseCase.TransactionView> views;
        try {
            views = listTransactionsUseCase.execute(merchantId);
        } catch (Exception ex) {
            log.error("Error while listing transactions for merchantId={}: {}", merchantId, ex.getMessage(), ex);
            throw ex;
        }
        log.info("ListTransactionsUseCase returned {} items", views == null ? 0 : views.size());

        List<TransactionResponse> transactions = views.stream()
                .map(view -> new TransactionResponse(
                        view.id(),
                        view.merchantId(),
                        view.paymentIntentId(),
                        view.amount(),
                        view.currency(),
                        view.status(),
                        view.createdAt(),
                        view.updatedAt()
                ))
                .toList();

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((safePage - 1) * safePageSize, transactions.size());
        int toIndex = Math.min(fromIndex + safePageSize, transactions.size());

        return ResponseEntity.ok(new PaginatedTransactionResponse(
                transactions.subList(fromIndex, toIndex),
                transactions.size(),
                safePage,
                safePageSize
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable UUID id) {
        UUID merchantId = resolveMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        var view = getTransactionUseCase.execute(merchantId, id);
        return ResponseEntity.ok(new TransactionResponse(
                view.id(),
                view.merchantId(),
                view.paymentIntentId(),
                view.amount(),
                view.currency(),
                view.status(),
                view.createdAt(),
                view.updatedAt()
        ));
    }

    public record CreateTransactionRequest(@NotNull UUID paymentIntentId,
                                           @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
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

    public record PaginatedTransactionResponse(List<TransactionResponse> items,
                                               int total,
                                               int page,
                                               int pageSize) {
    }

    private UUID resolveMerchantId() {
        UUID apiKeyMerchantId = authenticationService.getCurrentMerchantId();
        if (apiKeyMerchantId != null) {
            return apiKeyMerchantId;
        }

        UUID currentUserId = authenticationService.getCurrentUserId();
        String role = authenticationService.getCurrentUserRole();
        if (currentUserId == null || role == null) {
            return null;
        }

        return getCurrentMerchantUseCase.execute(currentUserId, role, null).merchantId();
    }
}
