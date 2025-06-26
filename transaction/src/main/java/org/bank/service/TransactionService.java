package org.bank.service;

import org.bank.model.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(Transaction transaction);

    List<Transaction> getTransactions(int page, int size);

    Transaction updateTransaction(UUID id, Transaction transaction);

    Boolean deleteTransaction(UUID id);
}
