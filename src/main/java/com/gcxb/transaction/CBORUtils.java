/*
 * Copyright 2018 Toronto Tiger Inc.
 * ALL RIGHTS RESERVED.
 */

package com.gcxb.transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;

public class CBORUtils {

private CBORUtils() {
}

public static CBORParser cborParser(ByteArrayOutputStream bytes) throws IOException {
	return cborParser(bytes.toByteArray());
}

public static CBORParser cborParser(byte[] input) throws IOException {
	return cborParser(cborFactory(), input);
}

public static CBORParser cborParser(InputStream in) throws IOException {
	CBORFactory f = cborFactory();
	return cborParser(f, in);
}

public static CBORParser cborParser(CBORFactory f, byte[] input) throws IOException {
	return f.createParser(input);
}

public static CBORParser cborParser(CBORFactory f, InputStream in) throws IOException {
	return f.createParser(in);
}

public static ObjectMapper cborMapper() {
	return new ObjectMapper(cborFactory());
}

public static CBORFactory cborFactory() {
	return new CBORFactory();
}

public static byte[] cborDoc(String json) throws IOException {
	return cborDoc(cborFactory(), json);
}

public static byte[] cborDoc(CBORFactory cborF, String json) throws IOException {
	JsonFactory jf = new JsonFactory();
	JsonParser jp = jf.createParser(json);
	ByteArrayOutputStream out = new ByteArrayOutputStream(json.length());
	JsonGenerator dest = cborF.createGenerator(out);

	while (jp.nextToken() != null) {
		dest.copyCurrentEvent(jp);
	}
	jp.close();
	dest.close();
	return out.toByteArray();
}

public static CBORGenerator cborGenerator(ByteArrayOutputStream result) throws IOException {
	return cborGenerator(cborFactory(), result);
}

public static CBORGenerator cborGenerator(CBORFactory f,
		ByteArrayOutputStream result)
		throws IOException {
	return f.createGenerator(result, null);
}

}
