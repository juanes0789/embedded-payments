package com.paymentplatform.embeddedpayments.merchant.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO genérico para respuestas de actualización de comercio
 */
public record MerchantUpdateResponse(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("name")
        String name,

        @JsonProperty("email")
        String email,

        @JsonProperty("status")
        String status,

        @JsonProperty("updated_at")
        Instant updatedAt,

        @JsonProperty("message")
        String message
) {
}

