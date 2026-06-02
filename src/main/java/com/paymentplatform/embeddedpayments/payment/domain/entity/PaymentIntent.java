package com.paymentplatform.embeddedpayments.payment.domain.entity;

import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_intents")
public class PaymentIntent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(nullable = true)
    private UUID customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected PaymentIntent() {
    }

    public PaymentIntent(UUID id, UUID merchantId, BigDecimal amount, String currency, String status) {
        this(id, merchantId, null, amount, currency, status, null, Instant.now(), Instant.now());
    }

    public PaymentIntent(UUID id,
                         UUID merchantId,
                         UUID customerId,
                         BigDecimal amount,
                         String currency,
                         String status,
                         String description,
                         Instant createdAt,
                         Instant updatedAt) {
        this.id = id;
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markProcessing() {
        changeStatus("PROCESSING");
    }

    public void markSucceeded() {
        changeStatus("SUCCEEDED");
    }

    public void markFailed() {
        changeStatus("FAILED");
    }

    public void cancel() {
        changeStatus("CANCELED");
    }

    public boolean isCancelable() {
        return "CREATED".equalsIgnoreCase(status) || "PROCESSING".equalsIgnoreCase(status);
    }

    public boolean belongsTo(UUID merchantId) {
        return merchantId != null && merchantId.equals(this.merchantId);
    }

    private void changeStatus(String targetStatus) {
        if (!isValidTransition(this.status, targetStatus)) {
            throw new DomainException("Invalid payment intent transition from " + this.status + " to " + targetStatus);
        }

        this.status = targetStatus;
        this.updatedAt = Instant.now();
    }

    private boolean isValidTransition(String currentStatus, String targetStatus) {
        return switch (currentStatus == null ? "" : currentStatus.toUpperCase()) {
            case "CREATED" -> "PROCESSING".equals(targetStatus) || "SUCCEEDED".equals(targetStatus)
                    || "FAILED".equals(targetStatus) || "CANCELED".equals(targetStatus);
            case "PROCESSING" -> "SUCCEEDED".equals(targetStatus) || "FAILED".equals(targetStatus)
                    || "CANCELED".equals(targetStatus);
            case "SUCCEEDED", "FAILED", "CANCELED" -> false;
            default -> false;
        };
    }
}

