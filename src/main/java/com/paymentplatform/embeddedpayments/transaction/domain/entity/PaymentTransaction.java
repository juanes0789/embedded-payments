package com.paymentplatform.embeddedpayments.transaction.domain.entity;

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

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(name = "reason_code")
    private String reasonCode;

    @Column(nullable = false, length = 10)
    private String currency;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(UUID id, UUID paymentIntentId, BigDecimal amount, String status, Instant createdAt) {
        this(id, paymentIntentId, amount, "USD", status, createdAt, null);
    }

    public PaymentTransaction(UUID id, UUID paymentIntentId, BigDecimal amount, String status, Instant createdAt, String reasonCode) {
        this(id, paymentIntentId, amount, "USD", status, createdAt, reasonCode);
    }

    public PaymentTransaction(UUID id, UUID paymentIntentId, BigDecimal amount, String currency, String status, Instant createdAt) {
        this(id, paymentIntentId, amount, currency, status, createdAt, null);
    }

    public PaymentTransaction(UUID id, UUID paymentIntentId, BigDecimal amount, String currency, String status, Instant createdAt, String reasonCode) {
        this.id = id;
        this.paymentIntentId = paymentIntentId;
        this.amount = amount;
        this.currency = currency != null ? currency : "USD";
        this.status = status;
        this.createdAt = createdAt;
        this.reasonCode = reasonCode;
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

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public String getCurrency() {
        return currency;
    }
}

