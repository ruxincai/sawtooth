package com.gcxb.transaction;

import org.jetbrains.annotations.NotNull;

public class TransactionMetaInfo {

    private TransactionType transactionType;

    public TransactionMetaInfo(@NotNull TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setTransactionType(@NotNull TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @NotNull
    public TransactionType getTransactionType() {
        return transactionType;
    }
}
