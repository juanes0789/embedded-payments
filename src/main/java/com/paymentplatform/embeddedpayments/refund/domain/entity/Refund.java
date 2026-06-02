package com.paymentplatform.embeddedpayments.refund.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID transactionId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Refund() {
    }

    public Refund(UUID id, UUID transactionId, BigDecimal amount, String reason, String status) {
        this(id, transactionId, amount, reason, status, Instant.now(), Instant.now());
    }

    public Refund(UUID id,
                  UUID transactionId,
                  BigDecimal amount,
                  String reason,
                  String status,
                  Instant createdAt,
                  Instant updatedAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
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
}

