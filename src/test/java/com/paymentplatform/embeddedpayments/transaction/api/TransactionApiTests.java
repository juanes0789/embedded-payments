package com.paymentplatform.embeddedpayments.transaction.api;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionStatusHistoryRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionStatusHistoryRepository transactionStatusHistoryRepository;

    @Autowired
    private com.paymentplatform.embeddedpayments.auth.domain.repository.MerchantApiKeyRepository merchantApiKeyRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void shouldCreateTransactionAndRecordAuditTimeline() throws Exception {
        UUID merchantId = UUID.randomUUID();
        
        // Crear y guardar API Key para la autenticación
        UUID keyId = UUID.randomUUID();
        String rawApiKey = "epk_" + keyId + "_dummysecret";
        String hashedApiKey = passwordEncoder.encode(rawApiKey);
        com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey apiKey = 
                new com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey(
                        keyId,
                        merchantId,
                        hashedApiKey,
                        "ACTIVE",
                        java.time.Instant.now()
                );
        merchantApiKeyRepository.save(apiKey);

        // Crear un payment intent de prueba
        PaymentIntent intent = new PaymentIntent(
                UUID.randomUUID(),
                merchantId,
                new BigDecimal("150.00"),
                "EUR",
                "CREATED"
        );
        paymentRepository.save(intent);

        // Intentar registrar la transacción de pago
        String request = String.format("""
                {
                  "paymentIntentId":"%s",
                  "amount":150.00,
                  "status":"SUCCEEDED"
                }
                """, intent.getId());

        mockMvc.perform(post("/api/v1/transactions")
                        .header("X-API-Key", rawApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Validar que se guardó en la base de datos el historial (Timeline HU 4.1)
        java.util.List<PaymentTransaction> transactions = transactionRepository.findByMerchantId(merchantId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(transactions).isNotEmpty();
        
        UUID transId = transactions.get(0).getId();
        var history = transactionStatusHistoryRepository.findByTransactionIdOrderByCreatedAtAsc(transId);
        assertThat(history).isNotEmpty();
        assertThat(history.get(0).getNewStatus()).isEqualTo("SUCCEEDED");
        assertThat(history.get(0).getChangedBy()).isEqualTo("API_CLIENT");
    }

    @Test
    void shouldRecordFailedTransactionWithReasonCode() throws Exception {
        UUID merchantId = UUID.randomUUID();
        
        // Crear y guardar API Key para la autenticación
        UUID keyId = UUID.randomUUID();
        String rawApiKey = "epk_" + keyId + "_dummysecret";
        String hashedApiKey = passwordEncoder.encode(rawApiKey);
        com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey apiKey = 
                new com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey(
                        keyId,
                        merchantId,
                        hashedApiKey,
                        "ACTIVE",
                        java.time.Instant.now()
                );
        merchantApiKeyRepository.save(apiKey);

        PaymentIntent intent = new PaymentIntent(
                UUID.randomUUID(),
                merchantId,
                new BigDecimal("80.00"),
                "USD",
                "CREATED"
        );
        paymentRepository.save(intent);

        // Intentar registrar la transacción fallida (HU 4.2)
        String request = String.format("""
                {
                  "paymentIntentId":"%s",
                  "amount":80.00,
                  "status":"FAILED",
                  "reasonCode":"INSUFFICIENT_FUNDS"
                }
                """, intent.getId());

        mockMvc.perform(post("/api/v1/transactions")
                        .header("X-API-Key", rawApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.reasonCode").value("INSUFFICIENT_FUNDS"));

        // Validar persistencia en base de datos
        java.util.List<PaymentTransaction> transactions = transactionRepository.findByMerchantId(merchantId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(transactions).isNotEmpty();
        assertThat(transactions.get(0).getReasonCode()).isEqualTo("INSUFFICIENT_FUNDS");

        // Validar que el historial tenga la transición con el motivo del rechazo (HU 4.1 & 4.2)
        var history = transactionStatusHistoryRepository.findByTransactionIdOrderByCreatedAtAsc(transactions.get(0).getId());
        assertThat(history).isNotEmpty();
        assertThat(history.get(0).getReasonCode()).isEqualTo("INSUFFICIENT_FUNDS");
    }
}
