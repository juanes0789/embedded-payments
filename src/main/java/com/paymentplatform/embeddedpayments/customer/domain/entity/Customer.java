package com.paymentplatform.embeddedpayments.customer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Customer() {
    }

    public Customer(UUID id, UUID merchantId, String email, String name) {
        this(id, merchantId, email, name, Instant.now());
    }

    public Customer(UUID id, UUID merchantId, String email, String name, Instant createdAt) {
        this.id = id;
        this.merchantId = merchantId;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

