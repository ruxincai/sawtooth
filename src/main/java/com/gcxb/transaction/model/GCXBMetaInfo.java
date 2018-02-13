/*
 * Copyright 2018 Toronto Tiger Inc.
 * ALL RIGHTS RESERVED.
 */

package com.gcxb.transaction.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gcxb.transaction.TransactionType;
import org.jetbrains.annotations.NotNull;

@JsonSerialize
public class GCXBMetaInfo extends TransactionMetaInfo {

private static final long serialVersionUID = 2L;

@JsonProperty
private String buyerAddress;
@JsonProperty
private String sellerAddress;
@JsonProperty
private String cardType;
@JsonProperty
private String cardId;
@JsonProperty
private double amount;

public GCXBMetaInfo(@NotNull TransactionType transactionType, @NotNull String buyerAddress,
		@NotNull String sellerAddress, @NotNull String cardType, @NotNull String cardId,
		@NotNull Double amount) {
	super(transactionType);
	this.buyerAddress = buyerAddress;
	this.sellerAddress = sellerAddress;
	this.cardType = cardType;
	this.cardId = cardId;
	this.amount = amount;
}

@JsonGetter
public String getBuyerAddress() {
	return buyerAddress;
}

@JsonGetter
public String getSellerAddress() {
	return sellerAddress;
}

@JsonGetter
public String getCardType() {
	return cardType;
}

@JsonGetter
public String getCardId() {
	return cardId;
}

@JsonGetter
public double getAmount() {
	return amount;
}

@JsonSetter
public void setBuyerAddress(String buyerAddress) {
	this.buyerAddress = buyerAddress;
}

@JsonSetter
public void setSellerAddress(String sellerAddress) {
	this.sellerAddress = sellerAddress;
}

@JsonSetter
public void setCardType(String cardType) {
	this.cardType = cardType;
}

@JsonSetter
public void setCardId(String cardId) {
	this.cardId = cardId;
}

@JsonSetter
public void setAmount(double amount) {
	this.amount = amount;
}
}
