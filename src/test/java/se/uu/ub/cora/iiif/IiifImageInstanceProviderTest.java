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

import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.binary.iiif.IiifAdapter;
import se.uu.ub.cora.binary.iiif.IiifInstanceProvider;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.iiif.IiifAdapterImp;
import se.uu.ub.cora.iiif.IiifImageInstanceProviderImp;
import se.uu.ub.cora.initialize.SettingsProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.logger.spies.LoggerFactorySpy;

public class IiifImageInstanceProviderTest {
	private IiifInstanceProvider provider;

	private LoggerFactorySpy loggerFactory;

	@BeforeMethod
	private void beforeMethod() {
		setExternalProviders();

		provider = new IiifImageInstanceProviderImp();
	}

	private void setExternalProviders() {
		loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);
		SettingsProvider.setSettings(Map.of("imageServerUrl", "someUrl"));
	}

	@Test
	public void testInstanceOf() throws Exception {
		assertTrue(provider instanceof IiifInstanceProvider);
	}

	@Test
	public void testGetOrderToSelectImplementionsBy() throws Exception {
		assertEquals(provider.getOrderToSelectImplementionsBy(), 0);
	}

	@Test
	public void getIiifImageAdapter() throws Exception {

		IiifAdapterImp iiifImageAdapter = (IiifAdapterImp) provider.getIiifAdapter();

		assertTrue(iiifImageAdapter instanceof IiifAdapter);
		assertTrue(
				iiifImageAdapter.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactory);
		assertEquals(iiifImageAdapter.onlyForTestGetIiifServerUrl(), "someUrl");
	}

}
