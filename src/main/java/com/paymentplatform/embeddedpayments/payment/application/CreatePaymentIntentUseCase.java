package com.paymentplatform.embeddedpayments.payment.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.payment.domain.services.PaymentDomainService;
import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CreatePaymentIntentUseCase {

    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final PaymentDomainService paymentDomainService;

    public CreatePaymentIntentUseCase(PaymentRepository paymentRepository,
                                      MerchantRepository merchantRepository,
                                      PaymentDomainService paymentDomainService) {
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
        this.paymentDomainService = paymentDomainService;
    }

    public PaymentIntent execute(UUID merchantId, BigDecimal amount, String currency) {
        return execute(merchantId, amount, currency, null);
    }

    public PaymentIntent execute(UUID merchantId, BigDecimal amount, String currency, String description) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "MERCHANT_NOT_FOUND",
                        "Merchant not found",
                        List.of("merchantId: " + merchantId)
                ));

        if (!merchant.isActive()) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "MERCHANT_INACTIVE",
                    "Merchant must be active to receive payments",
                    List.of("merchantId: " + merchantId, "status: " + merchant.getStatus())
            );
        }

        PaymentIntent intent = paymentDomainService.createPaymentIntent(merchantId, amount, currency, description);
        return paymentRepository.save(intent);
    }
}

