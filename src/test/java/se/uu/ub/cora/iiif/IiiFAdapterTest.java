/*
 * Copyright 2024 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.iiif;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binary.BinaryException;
import se.uu.ub.cora.binary.iiif.IiifAdapterResponse;
import se.uu.ub.cora.binary.iiif.IiifParameters;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.spies.HttpHandlerFactorySpy;
import se.uu.ub.cora.httphandler.spies.HttpHandlerSpy;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class IiiFAdapterTest {

	private static final String SOME_METHOD = "someMethod";
	private static final String IIIF_SERVER_URL = "someIiifServerUrl/";
	private static final String SOME_URI = "someUri";
	IiifAdapterImp adapter;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private IiifParameters iiifImageParameters;
	private HttpHandlerSpy httpHandler;
	private Map<String, String> headersMap;

	@BeforeMethod
	private void beforeMethod() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		httpHandler = new HttpHandlerSpy();

		httpHandlerFactory.MRV.setDefaultReturnValuesSupplier("factor", () -> httpHandler);

		headersMap = new LinkedHashMap<>();

		iiifImageParameters = new IiifParameters(SOME_URI, SOME_METHOD, headersMap);

		adapter = new IiifAdapterImp(IIIF_SERVER_URL, httpHandlerFactory);

	}

	@Test
	public void testRequestImage_ResponseStatusNotFound() throws Exception {
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 404);

		IiifAdapterResponse response = adapter.callIiifServer(iiifImageParameters);

		String errorMessage = "Requested identifier could not be found.";

		assertEquals(response.status(), 404);
		assertBody(response.body(), errorMessage);
	}

	@Test
	public void testRequestImage_ResponseStatusNotFound_UseUtf8Encoding() throws Exception {
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 404);
		IiifAdapterImpOnlyForTest adapter = new IiifAdapterImpOnlyForTest(IIIF_SERVER_URL,
				httpHandlerFactory);

		adapter.callIiifServer(iiifImageParameters);

		adapter.MCR.assertParameter("getBytesUsingEncoding", 0, "encoding", "UTF-8");
	}

	@Test
	public void testRequestImage_ResponseStatusNotFound_UnsupportedEncoding() throws Exception {
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 404);
		IiifAdapterImpOnlyForTest adapter = new IiifAdapterImpOnlyForTest(IIIF_SERVER_URL,
				httpHandlerFactory);
		adapter.throwUnsupportedEncodingException = Optional
				.of(new UnsupportedEncodingException("someUnsupportedEncodingException"));

		try {
			adapter.callIiifServer(iiifImageParameters);
			fail("It should Throw an exception");
		} catch (Exception e) {
			assertTrue(e instanceof BinaryException);
			assertEquals(e.getCause().getMessage(),
					"Unsupported encoding someUnsupportedEncodingException");
		}
	}

	class IiifAdapterImpOnlyForTest extends IiifAdapterImp {
		public MethodCallRecorder MCR = new MethodCallRecorder();
		public MethodReturnValues MRV = new MethodReturnValues();
		public Optional<UnsupportedEncodingException> throwUnsupportedEncodingException = Optional
				.empty();

		public IiifAdapterImpOnlyForTest(String iiifServerUrl,
				HttpHandlerFactory httpHandlerFactory) {
			super(iiifServerUrl, httpHandlerFactory);

			MCR.useMRV(MRV);
			MRV.setDefaultReturnValuesSupplier("getBytesUsingEncoding", () -> new byte[] {});
		}

		@Override
		byte[] getBytesUsingEncoding(String errorMessage, String encoding)
				throws UnsupportedEncodingException {
			if (throwUnsupportedEncodingException.isPresent()) {
				throw throwUnsupportedEncodingException.get();
			}
			return (byte[]) MCR.addCallAndReturnFromMRV("errorMessage", errorMessage, "encoding",
					encoding);
		}
	}

	private void assertBody(InputStream inputStream, String expected) throws IOException {
		String actual = convertInputStreamToString(inputStream);
		assertEquals(actual, expected);
	}

	private String convertInputStreamToString(InputStream inputStream) throws IOException {
		try (Reader reader = new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			return tryToConvertInputStreamToString(reader);
		}
	}

	private String tryToConvertInputStreamToString(Reader reader) throws IOException {
		int item = 0;
		StringBuilder textBuilder = new StringBuilder();
		while ((item = reader.read()) != -1) {
			textBuilder.append((char) item);
		}
		return textBuilder.toString();
	}

	@Test
	public void testRequestImage_UnexpectedException() throws Exception {
		RuntimeException runtimeException = new RuntimeException();
		httpHandler.MRV.setAlwaysThrowException("getResponseCode", runtimeException);

		try {
			adapter.callIiifServer(iiifImageParameters);
			fail("It should throw an exception");
		} catch (Exception e) {
			assertTrue(e instanceof BinaryException);
			assertEquals(e.getMessage(), "Error while calling iiifServer using method: "
					+ SOME_METHOD + ", and URI: " + SOME_URI);
			assertEquals(e.getCause(), runtimeException);
		}
	}

	@Test
	public void testRequestImage_OK() throws Exception {
		headersMap.put("someHeader", "someValue1, someValue2");
		headersMap.put("someOtherHeader", "someOtherValue1");

		IiifAdapterResponse response = adapter.callIiifServer(iiifImageParameters);

		assertMethod();
		assertHeaders();
		assertUri();

		httpHandler.MCR.assertReturn("getResponseCode", 0, response.status());
		httpHandler.MCR.assertReturn("getResponseHeaders", 0, response.headers());
		httpHandler.MCR.assertReturn("getResponseBinary", 0, response.body());
	}

	private void assertMethod() {
		httpHandler.MCR.assertParameters("setRequestMethod", 0, SOME_METHOD);
	}

	private void assertHeaders() {
		httpHandler.MCR.assertParameters("setRequestProperty", 0, "someHeader",
				"someValue1, someValue2");
		httpHandler.MCR.assertParameters("setRequestProperty", 1, "someOtherHeader",
				"someOtherValue1");
	}

	private void assertUri() {
		String expectedUrl = IIIF_SERVER_URL + SOME_URI;

		String iiifRequestUrl = (String) httpHandlerFactory.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("factor", 0, "url");

		assertEquals(iiifRequestUrl, expectedUrl);
	}
}
