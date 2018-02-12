package com.gcxb.transaction;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import sawtooth.sdk.processor.State;
import sawtooth.sdk.processor.TransactionHandler;
import sawtooth.sdk.processor.Utils;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;
import sawtooth.sdk.protobuf.TpProcessRequest;
import sawtooth.sdk.protobuf.TransactionHeader;

public class GCXBHandler implements TransactionHandler {

/**
 * Transaction Family Name.
 */
private static final String FAMILY_NAME = "gcxb";
/**
 * Transaction Family Version.
 */
private static final String VERSION = "1.0.0";
/**
 * The first 6 chars of hashing version of family name as GCXB namespace.
 */
private String namespace;

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
	TransactionHeader header = tpProcessRequest.getHeader();

	switch (metaInfo.getTransactionType()) {
		case EXCHANGE:
			//process exchange transaction.
			//TODO: validate the transaction???
			String buyerAddress = metaInfo.getBuyerAddress();
			String stateEntry = state.getState(Collections.singletonList(buyerAddress))
					.get(buyerAddress).toStringUtf8();

			//state.setState(header.getSignerPublicKey())
			break;
		case OTHER:
			break;
	}

}

/**
 * Helper function to generate new card address.
 */
private String getBuyerAddress(@NotNull String buyerName) throws InternalError {
	try {
		return namespace + Utils.hash512(buyerName.getBytes("UTF-8")).substring(0, 64);
	}
	catch (UnsupportedEncodingException e) {
		throw new InternalError("Internal Error: " + e.toString());
	}
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
	String amountStr = infoArray[4];

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

	return new GCXBHandler.GCXBMetaInfo(TransactionType.valueOf(transactionType), buyerAddress,
			sellerAddress, cardType, amount);
}

private static class GCXBMetaInfo extends TransactionMetaInfo {

	private String buyerAddress;
	private String sellerAddress;
	private String cardType;
	private double amount;

	public GCXBMetaInfo(@NotNull TransactionType transactionType, @NotNull String buyerAddress,
			@NotNull String sellerAddress, String cardType, double amount) {
		super(transactionType);
		this.buyerAddress = buyerAddress;
		this.sellerAddress = sellerAddress;
		this.cardType = cardType;
		this.amount = amount;
	}

	public String getBuyerAddress() {
		return buyerAddress;
	}

	public String getSellerAddress() {
		return sellerAddress;
	}

	public String getCardType() {
		return cardType;
	}

	public double getAmount() {
		return amount;
	}
}
}