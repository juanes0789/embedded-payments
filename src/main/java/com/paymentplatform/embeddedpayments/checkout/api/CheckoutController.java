package com.paymentplatform.embeddedpayments.checkout.api;

import com.paymentplatform.embeddedpayments.checkout.application.SubmitCheckoutPaymentUseCase;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final SubmitCheckoutPaymentUseCase submitCheckoutPaymentUseCase;

    public CheckoutController(PaymentRepository paymentRepository,
                             MerchantRepository merchantRepository,
                             SubmitCheckoutPaymentUseCase submitCheckoutPaymentUseCase) {
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
        this.submitCheckoutPaymentUseCase = submitCheckoutPaymentUseCase;
    }

    /**
     * GET /checkout/intents/{id}
     * Retrieve public payment intent information for checkout display.
     * No authentication required.
     */
    @GetMapping("/intents/{id}")
    public ResponseEntity<PaymentIntentPublicResponse> getIntent(@PathVariable UUID id) {
        PaymentIntent intent = paymentRepository.findById(id)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent not found",
                        List.of("intentId: " + id)
                ));

        if ("CANCELED".equalsIgnoreCase(intent.getStatus()) || "FAILED".equalsIgnoreCase(intent.getStatus())) {
            throw new DomainException(
                    HttpStatus.GONE,
                    "PAYMENT_INTENT_INVALID",
                    "This payment intent is no longer available",
                    List.of("status: " + intent.getStatus())
            );
        }

        String merchantName = merchantRepository.findById(intent.getMerchantId())
                .map(merchant -> merchant.getName())
                .orElse("Merchant");

        return ResponseEntity.ok(new PaymentIntentPublicResponse(
                intent.getId(),
                merchantName,
                intent.getAmount(),
                intent.getCurrency(),
                intent.getStatus(),
                intent.getDescription(),
                intent.getCreatedAt(),
                intent.getUpdatedAt()
        ));
    }

    /**
     * POST /checkout/submit
     * Submit customer payment information and process the transaction.
     * No authentication required, but checkoutId must be a valid payment intent.
     */
    @PostMapping("/submit")
    public ResponseEntity<CheckoutSubmitResponse> submitPayment(@Valid @RequestBody CheckoutSubmitRequest request) {
        try {
            SubmitCheckoutPaymentUseCase.Result result = submitCheckoutPaymentUseCase.execute(
                    request.checkoutId(),
                    request.customerEmail(),
                    request.customerName()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CheckoutSubmitResponse(
                            result.transactionId(),
                            result.status(),
                            result.processorReference()
                    ));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PAYMENT_PROCESSING_ERROR",
                    "An error occurred while processing the payment",
                    List.of(e.getMessage())
            );
        }
    }

    public record PaymentIntentPublicResponse(
            UUID id,
            String merchantName,
            BigDecimal amount,
            String currency,
            String status,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record CheckoutSubmitRequest(
            @NotNull(message = "checkoutId is required") UUID checkoutId,
            @NotBlank(message = "customerEmail is required") @Email String customerEmail,
            @NotBlank(message = "customerName is required") String customerName
    ) {
    }

    public record CheckoutSubmitResponse(
            String transactionId,
            String status,
            String processorReference
    ) {
    }
}

