/*
 * Copyright 2018 Toronto Tiger Inc.
 * ALL RIGHTS RESERVED.
 */

package com.gcxb.transaction;

import org.jetbrains.annotations.NotNull;

public enum TransactionType {
	EXCHANGE("exchange"),
	OTHER("OTHER");

private String name;

TransactionType(@NotNull String name) {
	this.name = name;
}

public String getName() {
	return name;
}
}

