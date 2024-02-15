package se.uu.ub.cora.iiif;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class IiiFImageExceptionTest {

	@Test
	public void testInit() {
		String message = "message";
		IiiFImageException exception = IiiFImageException.withMessage(message);
		assertEquals(exception.getMessage(), "message");
	}

	@Test
	public void testWithMessageAndException() throws Exception {
		Exception e = new Exception("some message");
		IiiFImageException exception = IiiFImageException.withMessageAndException("second message",
				e);
		assertEquals(exception.getMessage(), "second message");
		assertEquals(exception.getCause().getMessage(), "some message");

	}
}
