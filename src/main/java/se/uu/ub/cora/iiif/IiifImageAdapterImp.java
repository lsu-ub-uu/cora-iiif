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
import se.uu.ub.cora.binary.iiif.IiifImageAdapter;
import se.uu.ub.cora.binary.iiif.IiifImageParameters;
import se.uu.ub.cora.binary.iiif.IiifImageResponse;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class IiifImageAdapterImp implements IiifImageAdapter {

	private HttpHandlerFactory httpHandlerFactory;
	private String iiifServerUrl;

	public IiifImageAdapterImp(String iiifServerUrl, HttpHandlerFactory httpHandlerFactory) {
		this.iiifServerUrl = iiifServerUrl;
		this.httpHandlerFactory = httpHandlerFactory;
	}

	@Override
	public IiifImageResponse requestImage(IiifImageParameters iiifImageParameters) {
		try {
			return tryToRequestImage(iiifImageParameters);
		} catch (Exception e) {
			throw BinaryException
					.withMessageAndException("Error while requesting an image from server with id: "
							+ iiifImageParameters.identifier(), e);
		}
	}

	private IiifImageResponse tryToRequestImage(IiifImageParameters iiifImageParameters) {
		HttpHandler httpHandler = setUpRequest(iiifImageParameters);
		int responseCode = call(httpHandler);
		return requestResponse(iiifImageParameters, httpHandler, responseCode);
	}

	private HttpHandler setUpRequest(IiifImageParameters iiifImageParameters) {
		String requestUrl = buildRequestUrl(iiifImageParameters);
		HttpHandler httpHandler = httpHandlerFactory.factor(requestUrl);
		httpHandler.setRequestMethod("GET");
		// TODO: Set ALL headers from external call
		httpHandler.setRequestProperty("Accept", "image/avif,image/webp,*/*");
		return httpHandler;
	}

	private String buildRequestUrl(IiifImageParameters iiifImageParameters) {
		return iiifServerUrl + "/" + iiifImageParameters.dataDivider() + "/"
				+ iiifImageParameters.identifier() + "/" + iiifImageParameters.region() + "/"
				+ iiifImageParameters.size() + "/" + iiifImageParameters.rotation() + "/"
				+ iiifImageParameters.quality() + "." + iiifImageParameters.format();
	}

	private int call(HttpHandler httpHandler) {
		return httpHandler.getResponseCode();
	}

	private IiifImageResponse requestResponse(IiifImageParameters iiifImageParameters,
			HttpHandler httpHandler, int responseCode) {
		if (responseNotOk(responseCode)) {
			return returnNotOk(iiifImageParameters, httpHandler, responseCode);
		}
		return returnOk(httpHandler, responseCode);
	}

	private boolean responseNotOk(int responseCode) {
		return responseCode != 200;
	}

	private IiifImageResponse returnNotOk(IiifImageParameters iiifImageParameters,
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

	private IiifImageResponse returnNotFound(IiifImageParameters iiifImageParameters,
			int responseCode, Map<String, Object> responseHeaders) {
		var errorCode = Optional
				.of("Image with id: " + iiifImageParameters.identifier() + ", could not be found.");
		return new IiifImageResponse(responseCode, responseHeaders, Optional.empty(), errorCode);
	}

	private IiifImageResponse returnAnyError(IiifImageParameters iiifImageParameters,
			int responseCode, Map<String, Object> responseHeaders) {
		var errorCode = Optional.of(
				"Image with id: " + iiifImageParameters.identifier() + ", could not be retrieved");
		return new IiifImageResponse(responseCode, responseHeaders, Optional.empty(), errorCode);
	}

	private IiifImageResponse returnOk(HttpHandler httpHandler, int responseCode) {
		return new IiifImageResponse(responseCode, httpHandler.getResponseHeaders(),
				Optional.of(httpHandler.getResponseBinary()), Optional.empty());
	}

	@Override
	public IiifImageResponse requestInformation(String dataDivider, String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	String onlyForTestGetIiifServerUrl() {
		return iiifServerUrl;
	}

	HttpHandlerFactory onlyForTestGetHttpHandlerFactory() {
		return httpHandlerFactory;
	}
}