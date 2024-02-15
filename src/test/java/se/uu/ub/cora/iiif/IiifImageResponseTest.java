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

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;

import org.testng.annotations.Test;

public class IiifImageResponseTest {

	@Test
	public void testIiifImageResponse() throws Exception {
		ByteArrayInputStream image = new ByteArrayInputStream("someImage".getBytes());
		Map<String, Object> headers = Map.of("someKey", "someValue");

		IiifImageResponse record = new IiifImageResponse(200, headers, Optional.of(image),
				Optional.of("someError"));

		assertEquals(record.status(), 200);
		assertEquals(record.headers(), headers);
		assertEquals(record.image().get(), image);
		assertEquals(record.errorMessage().get(), "someError");
	}

}
