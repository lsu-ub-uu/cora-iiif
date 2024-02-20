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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binary.BinaryException;
import se.uu.ub.cora.binary.iiif.IiifImageParameters;
import se.uu.ub.cora.binary.iiif.IiifImageResponse;
import se.uu.ub.cora.httphandler.spies.HttpHandlerFactorySpy;
import se.uu.ub.cora.httphandler.spies.HttpHandlerSpy;
import se.uu.ub.cora.iiif.IiifImageAdapterImp;

public class IiiFImageAdapterTest {

	private static final String IIIF_SERVER_URL = "someIiifServerUrl";
	private static final String FORMAT = "someFormat";
	private static final String QUALITY = "someQuality";
	private static final String ROTATION = "someRotation";
	private static final String SIZE = "someSize";
	private static final String REGION = "someRegion";
	private static final String IDENTIFIER = "someIdentifier";
	private static final String DATA_DIVIDER = "someDataDivider";
	IiifImageAdapterImp adapter;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private IiifImageParameters iiifImageParameters;
	private HttpHandlerSpy httpHandler;

	@BeforeMethod
	private void beforeMethod() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		httpHandler = new HttpHandlerSpy();

		httpHandlerFactory.MRV.setDefaultReturnValuesSupplier("factor", () -> httpHandler);

		iiifImageParameters = new IiifImageParameters(DATA_DIVIDER, IDENTIFIER, REGION, SIZE,
				ROTATION, QUALITY, FORMAT);

		adapter = new IiifImageAdapterImp(IIIF_SERVER_URL, httpHandlerFactory);

	}

	@Test
	public void testRequestImage_OK() throws Exception {

		IiifImageResponse response = adapter.requestImage(iiifImageParameters);

		assertBuildUrl();
		assertHeaders();

		httpHandler.MCR.assertParameters("getResponseCode", 0);
		httpHandler.MCR.assertParameters("getResponseHeaders", 0);
		httpHandler.MCR.assertParameters("getResponseBinary", 0);

		httpHandler.MCR.assertReturn("getResponseCode", 0, response.status());
		httpHandler.MCR.assertReturn("getResponseHeaders", 0, response.headers());
		httpHandler.MCR.assertReturn("getResponseBinary", 0, response.image().get());
		assertTrue(response.errorMessage().isEmpty());
	}

	private void assertHeaders() {
		httpHandler.MCR.assertParameters("setRequestMethod", 0, "GET");
		httpHandler.MCR.assertParameters("setRequestProperty", 0, "Accept",
				"image/avif,image/webp,*/*");
	}

	private void assertBuildUrl() {
		String expectedUrl = IIIF_SERVER_URL + "/" + DATA_DIVIDER + "/" + IDENTIFIER + "/" + REGION
				+ "/" + SIZE + "/" + ROTATION + "/" + QUALITY + "." + FORMAT;

		String iiifRequestUrl = (String) httpHandlerFactory.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("factor", 0, "url");

		assertEquals(iiifRequestUrl, expectedUrl);
	}

	@Test
	public void testRequestImage_ResponseStatusNotFound() throws Exception {
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 404);

		IiifImageResponse response = adapter.requestImage(iiifImageParameters);

		String errorMessage = "Image with id: " + IDENTIFIER + ", could not be found.";
		assertIiifResponse(response, 404, errorMessage);
	}

	private void assertIiifResponse(IiifImageResponse response, int status, String errorMessage) {
		assertEquals(response.status(), status);
		httpHandler.MCR.assertReturn("getResponseHeaders", 0, response.headers());
		assertTrue(response.image().isEmpty());
		assertEquals(response.errorMessage().get(), errorMessage);
	}

	@Test
	public void testRequestImage_ResponseStatusNotOk() throws Exception {
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 418);

		IiifImageResponse response = adapter.requestImage(iiifImageParameters);

		String errorMessage = "Image with id: " + IDENTIFIER + ", could not be retrieved";
		assertIiifResponse(response, 418, errorMessage);
	}

	@Test
	public void testRequestImage_ResponseStatusNotInternalErro() throws Exception {
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 500);

		IiifImageResponse response = adapter.requestImage(iiifImageParameters);

		String errorMessage = "Image with id: " + IDENTIFIER + ", could not be retrieved";
		assertIiifResponse(response, 500, errorMessage);
	}

	@Test
	public void testRequestImage_UnexpectedException() throws Exception {
		RuntimeException runtimeException = new RuntimeException();
		httpHandler.MRV.setAlwaysThrowException("getResponseCode", runtimeException);

		try {
			adapter.requestImage(iiifImageParameters);
			fail("It should throw an exception");
		} catch (Exception e) {
			assertTrue(e instanceof BinaryException);
			assertEquals(e.getMessage(),
					"Error while requesting an image from server with id: " + IDENTIFIER);
			assertEquals(e.getCause(), runtimeException);
		}
	}
}
