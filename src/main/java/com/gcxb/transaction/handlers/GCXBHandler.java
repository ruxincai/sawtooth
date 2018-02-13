/*
 * Copyright 2018 Toronto Tiger Inc.
 * ALL RIGHTS RESERVED.
 */

package com.gcxb.transaction.handlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcxb.transaction.CBORUtils;
import com.gcxb.transaction.TransactionType;
import com.gcxb.transaction.model.GCXBMetaInfo;
import com.gcxb.transaction.model.TransactionMetaInfo;
import com.google.protobuf.ByteString;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sawtooth.sdk.processor.State;
import sawtooth.sdk.processor.TransactionHandler;
import sawtooth.sdk.processor.Utils;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;
import sawtooth.sdk.protobuf.TpProcessRequest;

public class GCXBHandler implements TransactionHandler {

/** Transaction Family Name. */
private static final String FAMILY_NAME = "gcxb";
/** Transaction Family Version. */
private static final String VERSION = "1.0.0";
public static final String UTF_8 = "UTF-8";
/** The first 6 chars of hashing version of family name as GCXB namespace. */
private String namespace;
/** Jackson object mapper to read/write cbor object. */
private ObjectMapper objectMapper = new ObjectMapper();

public GCXBHandler() {
	try {
		namespace = Utils.hash512(transactionFamilyName().getBytes(UTF_8)).substring(0, 6);
	}
	catch (UnsupportedEncodingException usee) {
		namespace = StringUtils.EMPTY;
	}
}

@Override
public String transactionFamilyName() {
	return FAMILY_NAME;
}

@Override
public String getVersion() {
	return VERSION;
}

@Override
public Collection<String> getNameSpaces() {
	return Collections.singletonList(namespace);
}

/**
 * The apply method will be invoked by Transaction Processor's process method.
 *
 * @param tpProcessRequest the transaction process request
 * @param state the client state with request context information.
 * @throws InvalidTransactionException the invalid transaction exception for handling transaction
 * related invalid state.
 * @throws InternalError the internal error for catching any internal processing errors.
 */
@Override
public void apply(TpProcessRequest tpProcessRequest, State state)
		throws InvalidTransactionException, InternalError {
	assert tpProcessRequest != null : "The tpProcessRequest must no be null.";
	assert state != null : "The state should not be null either.";

	//Account management includes different aspects,
	// the least should include: Create, Update, Delete
	//Check the request to process accordingly.
	GCXBMetaInfo metaInfo = (GCXBMetaInfo) getTransactionMetaInfo(tpProcessRequest);

	try {
		Collection<String> addresses = Collections.emptyList();
		switch (metaInfo.getTransactionType()) {
			case EXCHANGE:
				//process exchange transaction.
				//TODO: validate the transaction???
				String address = getAddress(TransactionType.EXCHANGE.name());

				Map<String, ByteString> possibleAddressValues = state.getState(
						Collections.singletonList(address));
				byte[] stateValueRep = possibleAddressValues.get(address).toByteArray();
				Map<String, ?> stateValue;
				if (stateValueRep.length > 0) {

					//noinspection unchecked
					stateValue = (Map<String, ?>) decodeState(stateValueRep, objectMapper,
							GCXBMetaInfo.class);
					if (stateValue.containsKey(address)) {
						throw new InvalidTransactionException(
								"Address is already in state, Address: " + address + " Value: " +
										stateValue.get(address).toString());
					}
				}
				addresses = state.setState(Collections.singletonList(
						encodeState(address, objectMapper, metaInfo)));
				break;
			case OTHER:
				return;
		}
		if (addresses.isEmpty()) {
			throw new InternalError("State error!");
		}
	}
	catch (IOException ioe) {
		throw new InternalError("IOException " + ioe.toString());
	}
}

/**
 * Helper function to generate new card address.
 */
private String getAddress(@NotNull String transactionType) throws InternalError {
	try {
		return namespace + Utils.hash512(transactionType.getBytes("UTF-8")).substring(0, 64);
	}
	catch (UnsupportedEncodingException e) {
		throw new InternalError("Internal Error: " + e.toString());
	}
}

/**
 * Helper function to decode State retrieved from the address.
 */
@NotNull
private static <T> T decodeState(@NotNull byte[] bytes, @NotNull ObjectMapper objectMapper,
		@NotNull Class<T> classType) throws IOException {
	return objectMapper.readValue(CBORUtils.cborParser(bytes), classType);
}

/**
 * Helper function to encode State written to the address.
 */
@NotNull
private static <T> Map.Entry<String, ByteString> encodeState(@NotNull String address,
		@NotNull ObjectMapper objectMapper, @Nullable T value) throws IOException {
	ByteArrayOutputStream boas = new ByteArrayOutputStream();
	objectMapper.writeValue(CBORUtils.cborGenerator(boas), value);
	return new AbstractMap.SimpleEntry<>(address, ByteString.copyFrom(boas.toByteArray()));
}

/**
 * Get Transaction Meta Info
 *
 * @param transactionRequest the request.
 * @return Transaction Meta Info object.
 * @throws InvalidTransactionException the InvalidTransactionException with detailed message.
 */
private static TransactionMetaInfo getTransactionMetaInfo(
		@NotNull TpProcessRequest transactionRequest) throws InvalidTransactionException {
	String payload = transactionRequest.getPayload().toStringUtf8();
	ArrayList<String> payloadList =
			new ArrayList<>(Arrays.asList(payload.split(",")));
	if (payloadList.size() > 3) {
		throw new InvalidTransactionException("Invalid payload serialization");
	}
	while (payloadList.size() < 3) {
		payloadList.add("");
	}
	String[] infoArray = (String[]) payloadList.toArray();
	String transactionType = infoArray[0];
	String buyerAddress = infoArray[1];
	String sellerAddress = infoArray[2];
	String cardType = infoArray[3];
	String cardId = infoArray[4];
	String amountStr = infoArray[5];

	if (StringUtils.isEmpty(transactionType)) {
		throw new InvalidTransactionException(
				"Missing required payload field - \"transactionType\"");
	}
	if (StringUtils.isEmpty(buyerAddress)) {
		throw new InvalidTransactionException("Missing required payload field - \"buyerAddress\"");
	}
	if (StringUtils.isEmpty(sellerAddress)) {
		throw new InvalidTransactionException("Missing required payload field - \"sellerAddress\"");
	}
	if (StringUtils.isEmpty(cardType)) {
		throw new InvalidTransactionException("Missing required payload field - \"cardType\"");
	}
	if (StringUtils.isEmpty(cardId)) {
		throw new InvalidTransactionException("Missing required payload field - \"cardId\"");
	}
	if (StringUtils.isEmpty(amountStr)) {
		throw new InvalidTransactionException("Missing required payload field - \"amount\"");
	}
	double amount;
	try {
		amount = Double.parseDouble(amountStr);
	}
	catch (NumberFormatException nfe) {
		throw new InvalidTransactionException("Failed to parse \"amount\" object");
	}

	return new GCXBMetaInfo(TransactionType.valueOf(transactionType), buyerAddress,
			sellerAddress, cardType, cardId, amount);
}

}