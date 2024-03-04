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

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;

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
	public IiifAdapterResponse callIiifServer(IiifParameters iiifParameters) {
		try {
			return tryToRequestImage(iiifParameters);
		} catch (Exception e) {
			throw errorWhileCallingIiifServer(e, iiifParameters);
		}
	}

	private IiifAdapterResponse tryToRequestImage(IiifParameters iiifParameters) {
		HttpHandler httpHandler = setUpRequest(iiifParameters);
		int responseCode = call(httpHandler);
		return requestResponse(httpHandler, responseCode);
	}

	private HttpHandler setUpRequest(IiifParameters iiifParameters) {
		String requestUrl = buildRequestUrl(iiifParameters);
		HttpHandler httpHandler = httpHandlerFactory.factor(requestUrl);
		httpHandler.setRequestMethod(iiifParameters.method());
		setHeaders(httpHandler, iiifParameters.headersMap());
		return httpHandler;
	}

	private void setHeaders(HttpHandler httpHandler, Map<String, String> headersMap) {
		for (Entry<String, String> header : headersMap.entrySet()) {
			httpHandler.setRequestProperty(header.getKey(), header.getValue());
		}
	}

	private String buildRequestUrl(IiifParameters iiifImageParameters) {
		return iiifServerUrl + iiifImageParameters.uri();
	}

	private int call(HttpHandler httpHandler) {
		return httpHandler.getResponseCode();
	}

	private IiifAdapterResponse requestResponse(HttpHandler httpHandler, int responseCode) {
		if (responseNotFound(responseCode)) {
			return returnNotFound(responseCode, httpHandler.getResponseHeaders());
		}
		return returnResponse(httpHandler, responseCode);
	}

	private boolean responseNotFound(int responseCode) {
		return responseCode == 404;
	}

	private IiifAdapterResponse returnNotFound(int responseCode, Map<String, String> map) {
		String errorMessage = "Requested identifier could not be found.";
		return new IiifAdapterResponse(responseCode, map,
				new ByteArrayInputStream(errorMessage.getBytes()));
	}

	private IiifAdapterResponse returnResponse(HttpHandler httpHandler, int responseCode) {
		return new IiifAdapterResponse(responseCode, httpHandler.getResponseHeaders(),
				httpHandler.getResponseBinary());
	}

	private BinaryException errorWhileCallingIiifServer(Exception e,
			IiifParameters iiifParameters) {
		String errorMessage = "Error while calling iiifServer using method: {0}, and URI: {1}";
		return BinaryException.withMessageAndException(
				MessageFormat.format(errorMessage, iiifParameters.method(), iiifParameters.uri()),
				e);
	}

	String onlyForTestGetIiifServerUrl() {
		return iiifServerUrl;
	}

	HttpHandlerFactory onlyForTestGetHttpHandlerFactory() {
		return httpHandlerFactory;
	}
}