package com.paymentplatform.embeddedpayments.shared.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private UUID entityId;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private Instant happenedAt;

    protected AuditEvent() {
    }

    public AuditEvent(UUID id,
                      String eventType,
                      String entityType,
                      UUID entityId,
                      String origin,
                      String actor,
                      Instant happenedAt) {
        this.id = id;
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.origin = origin;
        this.actor = actor;
        this.happenedAt = happenedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getOrigin() {
        return origin;
    }

    public String getActor() {
        return actor;
    }

    public Instant getHappenedAt() {
        return happenedAt;
    }
}

