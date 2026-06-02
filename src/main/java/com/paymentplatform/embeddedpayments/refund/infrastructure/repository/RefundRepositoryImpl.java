package com.paymentplatform.embeddedpayments.refund.infrastructure.repository;

import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import com.paymentplatform.embeddedpayments.refund.domain.repository.RefundRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class RefundRepositoryImpl implements RefundRepository {

    private final RefundJpaRepository jpaRepository;

    public RefundRepositoryImpl(RefundJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Refund save(Refund refund) {
        return jpaRepository.save(refund);
    }

    @Override
    public Optional<Refund> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Refund> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Refund> findByTransactionId(UUID transactionId) {
        return jpaRepository.findByTransactionId(transactionId);
    }
}

