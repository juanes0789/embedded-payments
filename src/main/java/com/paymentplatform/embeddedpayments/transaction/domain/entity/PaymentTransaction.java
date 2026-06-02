package com.paymentplatform.embeddedpayments.transaction.domain.entity;

import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class PaymentTransaction {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID paymentIntentId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(UUID id, UUID paymentIntentId, BigDecimal amount, String currency, String status) {
        this(id, paymentIntentId, amount, currency, status, Instant.now(), Instant.now());
    }

    public PaymentTransaction(UUID id,
                              UUID paymentIntentId,
                              BigDecimal amount,
                              String currency,
                              String status,
                              Instant createdAt,
                              Instant updatedAt) {
        this.id = id;
        this.paymentIntentId = paymentIntentId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentIntentId() {
        return paymentIntentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
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

    public void markSucceeded() {
        changeStatus("SUCCEEDED");
    }

    public void markFailed() {
        changeStatus("FAILED");
    }

    public void cancel() {
        changeStatus("CANCELED");
    }

    public void markRefunded() {
        changeStatus("REFUNDED");
    }

    public void requestRefund() {
        changeStatus("REFUND_PENDING");
    }

    public void cancelRefundRequest() {
        changeStatus("SUCCEEDED");
    }

    public boolean isTerminal() {
        return "SUCCEEDED".equalsIgnoreCase(status)
                || "FAILED".equalsIgnoreCase(status)
                || "CANCELED".equalsIgnoreCase(status)
                || "REFUNDED".equalsIgnoreCase(status);
    }

    private void changeStatus(String targetStatus) {
        if (!isValidTransition(this.status, targetStatus)) {
            throw new DomainException("Invalid transaction transition from " + this.status + " to " + targetStatus);
        }

        this.status = targetStatus;
        this.updatedAt = Instant.now();
    }

    private boolean isValidTransition(String currentStatus, String targetStatus) {
        return switch (currentStatus == null ? "" : currentStatus.toUpperCase()) {
            case "PENDING" -> "SUCCEEDED".equals(targetStatus) || "FAILED".equals(targetStatus)
                    || "CANCELED".equals(targetStatus) || "REFUNDED".equals(targetStatus);
            case "SUCCEEDED" -> "REFUNDED".equals(targetStatus) || "CANCELED".equals(targetStatus) || "REFUND_PENDING".equals(targetStatus);
            case "REFUND_PENDING" -> "REFUNDED".equals(targetStatus) || "SUCCEEDED".equals(targetStatus) || "CANCELED".equals(targetStatus);
            case "FAILED", "CANCELED", "REFUNDED" -> false;
            default -> false;
        };
    }
}

