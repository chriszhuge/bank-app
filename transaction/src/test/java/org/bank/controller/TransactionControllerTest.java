package org.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bank.common.enums.Currency;
import org.bank.common.enums.TransactionChannel;
import org.bank.common.enums.TransactionStatus;
import org.bank.common.enums.TransactionType;
import org.bank.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = new Transaction();
        sampleTransaction.setUserName("张三");
        sampleTransaction.setAccountNumber("62220202020000123");
        sampleTransaction.setAmount(new BigDecimal("100.00"));
        sampleTransaction.setCurrency(Currency.CNY);
        sampleTransaction.setStatus(TransactionStatus.SUCCESS);
        sampleTransaction.setType(TransactionType.DEPOSIT);
        sampleTransaction.setChannel(TransactionChannel.COUNTER);
        sampleTransaction.setDescription("测试交易");
    }

    @Test
    void testCreateTransaction_success() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userName").value("张三"));
    }

    @Test
    void testCreateTransaction_invalidAmount() throws Exception {
        sampleTransaction.setAmount(new BigDecimal("0.001")); // too small

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002));
    }

    @Test
    void testGetTransactions_success() throws Exception {
        mockMvc.perform(get("/transactions?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testUpdateTransaction_success() throws Exception {
        // First create one
        String response = mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID id = UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());

        // Modify username
        sampleTransaction.setUserName("李四");

        mockMvc.perform(put("/transactions/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("李四"));
    }

    @Test
    void testUpdateTransaction_notFound() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(put("/transactions/" + fakeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10001));
    }

    @Test
    void testDeleteTransaction_success() throws Exception {
        // First create one
        String response = mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID id = UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());

        mockMvc.perform(delete("/transactions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testDeleteTransaction_invalidId() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(delete("/transactions/" + fakeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10001));
    }

    @Test
    void testCreateTransaction_missingUserName() throws Exception {
        sampleTransaction.setUserName(null); // 必填字段为空

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002));
    }

    @Test
    void testCreateTransaction_zeroAmount() throws Exception {
        sampleTransaction.setAmount(BigDecimal.ZERO); // 金额非法

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002));
    }

    @Test
    void testGetTransactions_invalidPage() throws Exception {
        mockMvc.perform(get("/transactions?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002));
    }

    @Test
    void testCreateTransaction_futureCreatedAt() throws Exception {
        sampleTransaction.setCreatedAt(java.time.LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10002));
    }

    @Test
    void testCreateTransaction_invalidCurrency() throws Exception {
        String json = objectMapper.writeValueAsString(sampleTransaction);
        json = json.replace("\"CNY\"", "\"BITCOIN\""); // 非法枚举

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void testUpdateTransaction_missingId() throws Exception {
        mockMvc.perform(put("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isMethodNotAllowed()); // 405
    }

    @Test
    void testDeleteTransaction_withoutUUID() throws Exception {
        mockMvc.perform(delete("/transactions/invalid-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

}