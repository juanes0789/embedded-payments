package com.paymentplatform.embeddedpayments.customer.domain.repository;

import com.paymentplatform.embeddedpayments.customer.domain.entity.Customer;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    Optional<Customer> findByMerchantIdAndEmail(UUID merchantId, String email);
}

