package com.paymentplatform.embeddedpayments.transaction.application;

import com.paymentplatform.embeddedpayments.customer.domain.entity.Customer;
import com.paymentplatform.embeddedpayments.customer.domain.repository.CustomerRepository;
import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
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
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;

    public ListTransactionsUseCase(TransactionRepository transactionRepository,
                                   PaymentRepository paymentRepository,
                                   CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
    }

    public Page<TransactionDto> execute(UUID merchantId, String status, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        
        Page<PaymentTransaction> transactionsPage;
        
        // Mapear de estado del frontend ('COMPLETED') a estado del backend ('SUCCEEDED')
        String backendStatus;
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

        return transactionsPage.map(t -> {
            Customer customer = resolveCustomer(t);

            return new TransactionDto(
                t.getId(),
                merchantId,
                t.getAmount(),
                t.getCurrency() != null ? t.getCurrency() : "USD",
                t.getStatus().equalsIgnoreCase("SUCCEEDED") ? "COMPLETED" : t.getStatus(),
                customer != null ? customer.getEmail() : null,
                customer != null ? customer.getName() : null,
                t.getCreatedAt(),
                t.getCreatedAt() // updatedAt
            );
        });
    }

    private Customer resolveCustomer(PaymentTransaction transaction) {
        UUID customerId = transaction.getCustomerId();

        if (customerId == null) {
            PaymentIntent intent = paymentRepository.findById(transaction.getPaymentIntentId()).orElse(null);
            if (intent != null) {
                customerId = intent.getCustomerId();
            }
        }

        if (customerId == null) {
            return null;
        }

        return customerRepository.findById(customerId).orElse(null);
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
