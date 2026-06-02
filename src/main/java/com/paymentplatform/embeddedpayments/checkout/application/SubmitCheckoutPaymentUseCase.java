package com.paymentplatform.embeddedpayments.checkout.application;

import com.paymentplatform.embeddedpayments.customer.domain.entity.Customer;
import com.paymentplatform.embeddedpayments.customer.domain.repository.CustomerRepository;
import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.payment.domain.services.PaymentProcessor;
import com.paymentplatform.embeddedpayments.payment.domain.services.ProcessorResult;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmitCheckoutPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentProcessor paymentProcessor;

    public SubmitCheckoutPaymentUseCase(PaymentRepository paymentRepository,
                                       CustomerRepository customerRepository,
                                       TransactionRepository transactionRepository,
                                       PaymentProcessor paymentProcessor) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.paymentProcessor = paymentProcessor;
    }

    public record Result(String transactionId, String status, String processorReference) {
    }

    @Transactional
    public Result execute(UUID paymentIntentId, String customerEmail, String customerName) {
        // 1. Fetch payment intent
        PaymentIntent intent = paymentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "PAYMENT_INTENT_NOT_FOUND",
                        "Payment intent not found",
                        List.of("paymentIntentId: " + paymentIntentId)
                ));

        // 2. Verify intent is in processable state
        if (!"CREATED".equalsIgnoreCase(intent.getStatus()) && !"PROCESSING".equalsIgnoreCase(intent.getStatus())) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "PAYMENT_INTENT_NOT_PROCESSABLE",
                    "Payment intent cannot be processed in its current state",
                    List.of("paymentIntentId: " + paymentIntentId, "status: " + intent.getStatus())
            );
        }

        // 3. Get or create customer
        Customer customer = customerRepository.findByMerchantIdAndEmail(intent.getMerchantId(), customerEmail)
                .orElseGet(() -> customerRepository.save(
                        new Customer(UUID.randomUUID(), intent.getMerchantId(), customerEmail, customerName)
                ));

        UUID customerId = customer.getId();

        // 4. Check if transaction already exists
        PaymentTransaction existingTransaction = transactionRepository.findByPaymentIntentId(paymentIntentId)
                .orElse(null);
        if (existingTransaction != null && !existingTransaction.isTerminal()) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "TRANSACTION_ALREADY_EXISTS",
                    "A transaction is already being processed for this payment intent",
                    List.of("transactionId: " + existingTransaction.getId())
            );
        }

        // 5. Mark intent as PROCESSING
        if ("CREATED".equalsIgnoreCase(intent.getStatus())) {
            intent.markProcessing();
            paymentRepository.save(intent);
        }

        // 6. Process payment
        ProcessorResult result = paymentProcessor.process(
                paymentIntentId,
                customerId,
                intent.getAmount(),
                intent.getCurrency()
        );

        // 7. Create transaction
        PaymentTransaction transaction = new PaymentTransaction(
                UUID.randomUUID(),
                paymentIntentId,
                intent.getAmount(),
                intent.getCurrency(),
                "PENDING"
        );

        // 8. Update statuses based on processor result
        if ("SUCCEEDED".equalsIgnoreCase(result.status())) {
            transaction.markSucceeded();
            intent.markSucceeded();
        } else {
            transaction.markFailed();
            intent.markFailed();
        }

        // 9. Persist updates
        transactionRepository.save(transaction);
        paymentRepository.save(intent);

        return new Result(
                transaction.getId().toString(),
                transaction.getStatus(),
                result.processorReference()
        );
    }
}

