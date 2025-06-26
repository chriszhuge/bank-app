package org.bank.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.bank.common.enums.Currency;
import org.bank.common.enums.TransactionChannel;
import org.bank.common.enums.TransactionStatus;
import org.bank.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Transaction {

    private UUID id;

    @NotNull
    private TransactionType type;       // 存款、取款、转账、支付

    @NotNull
    private TransactionStatus status;   // 成功、失败、处理中

    @NotNull
    @DecimalMin(value = "0.01", message = "交易金额不能小于0.01")
    private BigDecimal amount;

    @NotNull
    private Currency currency;          // CNY/USD/EUR/JPY

    @NotBlank
    private String accountNumber;       // 账户或卡号

    @NotBlank
    private String userName;            // 用户名

    @NotNull
    private TransactionChannel channel; // 柜面、ATM、网银、手机银行

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String description;

}