package org.bank.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.bank.common.enums.CodeEnum;
import org.bank.common.enums.TransactionChannel;
import org.bank.common.enums.TransactionStatus;
import org.bank.common.enums.TransactionType;
import org.bank.common.enums.Currency;
import org.bank.common.exception.TransactionException;
import org.bank.model.Transaction;
import org.bank.service.TransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final Map<UUID, Transaction> transactionMap = new ConcurrentHashMap<>();

    // 缓存排序列表
    private volatile List<Transaction> cachedSortedList = null;

    @RateLimiter(name = "transactionService")
    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallback")
    @Override
    public Transaction createTransaction(Transaction transaction) {

        validateTransaction(transaction);

        if (transaction.getId() == null) {
            transaction.setId(UUID.randomUUID());
        }
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(java.time.LocalDateTime.now());
        }
        if (transaction.getUpdatedAt() == null) {
            transaction.setUpdatedAt(java.time.LocalDateTime.now());
        }
        transactionMap.put(transaction.getId(), transaction);
        cachedSortedList = null; // 写操作后清空缓存
        log.debug("Created txn: id={}, amount={}", transaction.getId(), transaction.getAmount());
        return transaction;
    }

    @RateLimiter(name = "transactionService")
    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallback")
    @Override
    public List<Transaction> getTransactions(int page, int size) {
        log.debug("Fetch transactions page={}, size={}", page, size);
        if(page <= 0 || size <= 0){
            throw new TransactionException(CodeEnum.ILLEGAL_PARA,"分页参数错误");
        }
        int skip = Math.max((page - 1) * size, 0);

        // 缓存排序结果避免每次都排序
        List<Transaction> sortedList = cachedSortedList;
        if (sortedList == null) {
            synchronized (this) {
                if (cachedSortedList == null) {
                    cachedSortedList = transactionMap.values().stream()
                            .sorted(Comparator.comparing(Transaction::getUpdatedAt).reversed())
                            .collect(Collectors.toList());
                }
                sortedList = cachedSortedList;
            }
        }

        return sortedList.stream()
                .skip(skip)
                .limit(size)
                .toList();
    }

    @RateLimiter(name = "transactionService")
    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallback")
    @Override
    public Transaction updateTransaction(UUID id, Transaction transaction) {
        validateTransaction(transaction);
        Transaction old = transactionMap.get(id);
        if (old == null) {
            throw new TransactionException(CodeEnum.TRANSACTION_NOT_EXIST);
        }
        transaction.setId(id);
        transaction.setUpdatedAt(java.time.LocalDateTime.now());
        transactionMap.put(id, transaction);
        cachedSortedList = null; // 修改也清除缓存
        log.debug("Updated txn: id={}, amount={}", id, transaction.getAmount());
        return transaction;
    }

    @RateLimiter(name = "transactionService")
    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallback")
    @Override
    public Boolean deleteTransaction(UUID id) {
        Transaction removed = transactionMap.remove(id);
        if (removed == null) {
            throw new TransactionException(CodeEnum.TRANSACTION_NOT_EXIST);
        }
        cachedSortedList = null; // 删除也清除缓存
        log.debug("Deleted txn: id={}", id);
        return true;
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA,"交易数据不能为空");
        }
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA,"交易金额必须大于或等于 0.01");
        }
        if (transaction.getUserName() == null || transaction.getUserName().trim().isEmpty()) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA,"用户名不能为空");
        }
        if (transaction.getAccountNumber() == null || transaction.getAccountNumber().trim().isEmpty()) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA,"账号不能为空");
        }
        if (transaction.getType() == null) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA,"交易类型不能为空");
        }
        if (transaction.getStatus() == null) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA,"交易状态不能为空");
        }
        if (transaction.getCurrency() == null) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA,"币种不能为空");
        }
        if (transaction.getChannel() == null) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA,"交易渠道不能为空");
        }
        // ✅ 枚举值是否合法（防止非法序列化或手动构造）
        if (!EnumSet.allOf(TransactionType.class).contains(transaction.getType())) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA, "非法的交易类型");
        }

        if (!EnumSet.allOf(TransactionStatus.class).contains(transaction.getStatus())) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA, "非法的交易状态");
        }

        if (!EnumSet.allOf(Currency.class).contains(transaction.getCurrency())) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA, "非法的币种类型");
        }

        if (!EnumSet.allOf(TransactionChannel.class).contains(transaction.getChannel())) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA, "非法的交易渠道");
        }

        // ✅ 时间不能为未来
        LocalDateTime now = LocalDateTime.now();
        if (transaction.getCreatedAt() != null && transaction.getCreatedAt().isAfter(now)) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA, "创建时间不能晚于当前时间");
        }

        if (transaction.getUpdatedAt() != null && transaction.getUpdatedAt().isAfter(now)) {
            throw new TransactionException(CodeEnum.ILLEGAL_PARA, "修改时间不能晚于当前时间");
        }
    }

    public Transaction fallback(Transaction transaction, Throwable t) {
        if (t instanceof TransactionException) {
            // 业务异常，直接向上传递，不执行降级逻辑
            throw (TransactionException) t;
        }
        log.warn("create fallback triggered: {}", t.toString());
        throw new TransactionException(CodeEnum.SERVICE_DEGRADED);
    }

    public Transaction fallback(UUID id, Transaction transaction, Throwable t) {
        if (t instanceof TransactionException) {
            // 业务异常，直接向上传递，不执行降级逻辑
            throw (TransactionException) t;
        }
        log.warn("update fallback triggered: {}", t.toString());
        throw new TransactionException(CodeEnum.SERVICE_DEGRADED);
    }

    public Boolean fallback(UUID id, Throwable t) {
        if (t instanceof TransactionException) {
            // 业务异常，直接向上传递，不执行降级逻辑
            throw (TransactionException) t;
        }
        log.warn("delete fallback triggered: {}", t.toString());
        throw new TransactionException(CodeEnum.SERVICE_DEGRADED);
    }

    public List<Transaction> fallback(int page, int size, Throwable t) {
        if (t instanceof TransactionException) {
            // 业务异常，直接向上传递，不执行降级逻辑
            throw (TransactionException) t;
        }
        log.warn("get fallback triggered: {}", t.toString());
        throw  new TransactionException(CodeEnum.SERVICE_DEGRADED);
    }
}