package com.paymentplatform.embeddedpayments.transaction.application;

import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ListTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    public ListTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Page<TransactionDto> execute(UUID merchantId, String status, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        
        Page<PaymentTransaction> transactionsPage;
        
        // Mapear de estado del frontend ('COMPLETED') a estado del backend ('SUCCEEDED')
        String backendStatus = null;
        if (status != null && !status.isBlank()) {
            if (status.equalsIgnoreCase("COMPLETED")) {
                backendStatus = "SUCCEEDED";
            } else {
                backendStatus = status.toUpperCase();
            }
            transactionsPage = transactionRepository.findByMerchantIdAndStatus(merchantId, backendStatus, pageRequest);
        } else {
            transactionsPage = transactionRepository.findByMerchantId(merchantId, pageRequest);
        }

        return transactionsPage.map(t -> new TransactionDto(
                t.getId(),
                merchantId,
                t.getAmount(),
                t.getCurrency() != null ? t.getCurrency() : "USD",
                t.getStatus().equalsIgnoreCase("SUCCEEDED") ? "COMPLETED" : t.getStatus(),
                "customer@example.com", // Por defecto
                "John Doe", // Por defecto
                t.getCreatedAt(),
                t.getCreatedAt() // updatedAt
        ));
    }

    public record TransactionDto(
            UUID id,
            UUID merchantId,
            BigDecimal amount,
            String currency,
            String status,
            String customerEmail,
            String customerName,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
