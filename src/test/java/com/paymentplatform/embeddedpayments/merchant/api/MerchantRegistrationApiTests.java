package com.paymentplatform.embeddedpayments.merchant.api;

import com.paymentplatform.embeddedpayments.shared.audit.AuditEventRepository;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MerchantRegistrationApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    void shouldRegisterMerchantAsPotentialCommerce() throws Exception {
        String request = """
                {
                  "name":"Zara",
                  "email":"zara+self@test.com"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andReturn();

        UUID merchantId = extractMerchantId(result.getResponse().getContentAsString());

        assertThat(auditEventRepository.existsByEventTypeAndEntityTypeAndEntityId(
                "merchant_registered",
                "merchant",
                merchantId
        )).isTrue();
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    void shouldRegisterMerchantAsAdministrator() throws Exception {
        String request = """
                {
                  "name":"Bershka",
                  "email":"bershka+admin@test.com"
                }
                """;

        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldReturnBadRequestForInvalidPayload() throws Exception {
        String request = """
                {
                  "name":"",
                  "email":"invalid-email"
                }
                """;

        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void shouldReturnConflictForDuplicateMerchant() throws Exception {
        String request = """
                {
                  "name":"Massimo Dutti",
                  "email":"massimo+dup@test.com"
                }
                """;

        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("MERCHANT_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    private UUID extractMerchantId(String responseBody) {
        Matcher matcher = Pattern.compile("\\\"id\\\":\\\"([0-9a-fA-F-]{36})\\\"").matcher(responseBody);
        assertThat(matcher.find()).isTrue();
        return UUID.fromString(matcher.group(1));
    }
}



