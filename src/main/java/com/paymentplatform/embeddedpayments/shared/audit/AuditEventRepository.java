package com.paymentplatform.embeddedpayments.shared.audit;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    boolean existsByEventTypeAndEntityTypeAndEntityId(String eventType, String entityType, UUID entityId);
}

