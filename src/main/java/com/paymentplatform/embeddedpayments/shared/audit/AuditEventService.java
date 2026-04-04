package com.paymentplatform.embeddedpayments.shared.audit;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    public AuditEventService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void registerMerchantCreated(UUID merchantId, String origin, String actor) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                "merchant_registered",
                "merchant",
                merchantId,
                origin,
                actor,
                Instant.now()
        );
        auditEventRepository.save(event);
    }
}

