package org.bank.controller;

import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.bank.common.response.ResponseData;
import org.bank.model.Transaction;
import org.bank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "交易管理", description = "提供交易的增删改查接口")
@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @Operation(summary = "创建交易", description = "创建一条新的交易记录")
    @PostMapping
    public ResponseData<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
        Transaction created = transactionService.createTransaction(transaction);
        return ResponseData.data(created);
    }

    @Operation(summary = "查询所有交易", description = "分页查询交易列表")
    @GetMapping
    public ResponseData<List<Transaction>> getAllTransactions(
            @Parameter(description = "页码，从1开始", example = "1") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页条数", example = "10") @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseData.data(transactionService.getTransactions(page, size));
    }

    @Operation(summary = "更新交易", description = "根据 ID 修改已有交易记录")
    @PutMapping("/{id}")
    public ResponseData<Transaction> updateTransaction(
            @Parameter(description = "交易ID", required = true) @PathVariable("id") UUID id,
            @Valid @RequestBody Transaction transaction) {
        Transaction updated = transactionService.updateTransaction(id, transaction);
        return ResponseData.data(updated);
    }

    @Operation(summary = "删除交易", description = "根据 ID 删除指定交易")
    @DeleteMapping("/{id}")
    public ResponseData<Boolean> deleteTransaction(
            @Parameter(description = "交易ID", required = true) @PathVariable("id") UUID id) {
        transactionService.deleteTransaction(id);
        return ResponseData.data(Boolean.TRUE);
    }
}
