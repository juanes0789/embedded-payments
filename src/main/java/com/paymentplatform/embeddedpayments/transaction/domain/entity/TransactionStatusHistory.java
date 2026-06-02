package com.paymentplatform.embeddedpayments.transaction.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_status_history")
public class TransactionStatusHistory {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID transactionId;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status", nullable = false)
    private String newStatus;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "reason_code")
    private String reasonCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransactionStatusHistory() {
    }

    public TransactionStatusHistory(UUID id, UUID transactionId, String previousStatus, String newStatus, String changedBy, String reasonCode, Instant createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.reasonCode = reasonCode;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
