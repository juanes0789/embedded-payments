package com.paymentplatform.embeddedpayments.merchant.api;

import com.paymentplatform.embeddedpayments.merchant.application.RegisterMerchantUseCase;
import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final RegisterMerchantUseCase registerMerchantUseCase;

    public MerchantController(RegisterMerchantUseCase registerMerchantUseCase) {
        this.registerMerchantUseCase = registerMerchantUseCase;
    }

    @PostMapping
    public ResponseEntity<MerchantResponse> register(@Valid @RequestBody RegisterMerchantRequest request) {
        Merchant merchant = registerMerchantUseCase.execute(request.name(), request.email());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MerchantResponse(
                        merchant.getId(),
                        merchant.getName(),
                        merchant.getEmail(),
                        merchant.getStatus()
                ));
    }

    public record RegisterMerchantRequest(@NotBlank String name, @NotBlank @Email String email) {
    }

    public record MerchantResponse(UUID id, String name, String email, String status) {
    }
}

