/*
 * Copyright 2018 Toronto Tiger Inc.
 * ALL RIGHTS RESERVED.
 */

package com.gcxb.transaction.model;

import java.io.Serializable;

import com.gcxb.transaction.TransactionType;
import org.jetbrains.annotations.NotNull;

public class TransactionMetaInfo implements Serializable {

private static final long serialVersionUID  = 1L;

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
