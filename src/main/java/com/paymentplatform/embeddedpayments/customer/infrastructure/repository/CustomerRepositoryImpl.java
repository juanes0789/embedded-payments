package com.paymentplatform.embeddedpayments.customer.infrastructure.repository;

import com.paymentplatform.embeddedpayments.customer.domain.entity.Customer;
import com.paymentplatform.embeddedpayments.customer.domain.repository.CustomerRepository;
import com.paymentplatform.embeddedpayments.customer.infrastructure.persistence.CustomerJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;

    public CustomerRepositoryImpl(CustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        return jpaRepository.save(customer);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Customer> findByMerchantIdAndEmail(UUID merchantId, String email) {
        return jpaRepository.findByMerchantIdAndEmail(merchantId, email);
    }
}

