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

import java.util.Map;
import java.util.Optional;

import se.uu.ub.cora.binary.BinaryException;
import se.uu.ub.cora.binary.iiif.IiifAdapter;
import se.uu.ub.cora.binary.iiif.IiifAdapterResponse;
import se.uu.ub.cora.binary.iiif.IiifParameters;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class IiifAdapterImp implements IiifAdapter {

	private HttpHandlerFactory httpHandlerFactory;
	private String iiifServerUrl;

	public IiifAdapterImp(String iiifServerUrl, HttpHandlerFactory httpHandlerFactory) {
		this.iiifServerUrl = iiifServerUrl;
		this.httpHandlerFactory = httpHandlerFactory;
	}

	@Override
	public IiifAdapterResponse callIiifServer(IiifParameters iiifImageParameters) {
		try {
			return tryToRequestImage(iiifImageParameters);
		} catch (Exception e) {
			throw BinaryException
					.withMessageAndException("Error while requesting an image from server with id: "
							+ iiifImageParameters.uri(), e);
		}
	}

	private IiifAdapterResponse tryToRequestImage(IiifParameters iiifImageParameters) {
		HttpHandler httpHandler = setUpRequest(iiifImageParameters);
		int responseCode = call(httpHandler);
		return requestResponse(iiifImageParameters, httpHandler, responseCode);
	}

	private HttpHandler setUpRequest(IiifParameters iiifImageParameters) {
		String requestUrl = buildRequestUrl(iiifImageParameters);
		HttpHandler httpHandler = httpHandlerFactory.factor(requestUrl);
		httpHandler.setRequestMethod("GET");
		// TODO: Set ALL headers from external call
		httpHandler.setRequestProperty("Accept", "image/avif,image/webp,*/*");
		return httpHandler;
	}

	private String buildRequestUrl(IiifParameters iiifImageParameters) {
		return iiifServerUrl + "/" + iiifImageParameters.uri();
	}

	private int call(HttpHandler httpHandler) {
		return httpHandler.getResponseCode();
	}

	private IiifAdapterResponse requestResponse(IiifParameters iiifImageParameters,
			HttpHandler httpHandler, int responseCode) {
		if (responseNotOk(responseCode)) {
			return returnNotOk(iiifImageParameters, httpHandler, responseCode);
		}
		return returnOk(httpHandler, responseCode);
	}

	private boolean responseNotOk(int responseCode) {
		return responseCode != 200;
	}

	private IiifAdapterResponse returnNotOk(IiifParameters iiifImageParameters,
			HttpHandler httpHandler, int responseCode) {
		Map<String, Object> responseHeaders = httpHandler.getResponseHeaders();
		if (responseNotFound(responseCode)) {
			return returnNotFound(iiifImageParameters, responseCode, responseHeaders);
		}
		return returnAnyError(iiifImageParameters, responseCode, responseHeaders);
	}

	private boolean responseNotFound(int responseCode) {
		return responseCode == 404;
	}

	private IiifAdapterResponse returnNotFound(IiifParameters iiifImageParameters, int responseCode,
			Map<String, Object> responseHeaders) {
		var errorCode = Optional
				.of("Image with path: " + iiifImageParameters.uri() + ", could not be found.");
		return new IiifAdapterResponse(responseCode, responseHeaders, Optional.empty(), errorCode);
	}

	private IiifAdapterResponse returnAnyError(IiifParameters iiifImageParameters, int responseCode,
			Map<String, Object> responseHeaders) {
		var errorCode = Optional.of(
				"Image with id: " + iiifImageParameters.identifier() + ", could not be retrieved");
		return new IiifAdapterResponse(responseCode, responseHeaders, Optional.empty(), errorCode);
	}

	private IiifAdapterResponse returnOk(HttpHandler httpHandler, int responseCode) {
		return new IiifAdapterResponse(responseCode, httpHandler.getResponseHeaders(),
				httpHandler.getResponseBinary());
	}

	// @Override
	// public IiifImageResponse requestInformation(String dataDivider, String identifier) {
	// // TODO Auto-generated method stub
	// return null;
	// }

	String onlyForTestGetIiifServerUrl() {
		return iiifServerUrl;
	}

	HttpHandlerFactory onlyForTestGetHttpHandlerFactory() {
		return httpHandlerFactory;
	}
}
