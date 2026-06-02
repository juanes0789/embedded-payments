package com.paymentplatform.embeddedpayments.customer.infrastructure.persistence;

import com.paymentplatform.embeddedpayments.customer.domain.entity.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerJpaRepository extends JpaRepository<Customer, UUID> {
    @Query("SELECT c FROM Customer c WHERE c.merchantId = :merchantId AND c.email = :email")
    Optional<Customer> findByMerchantIdAndEmail(@Param("merchantId") UUID merchantId, @Param("email") String email);
}

