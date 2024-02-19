package se.uu.ub.cora.iiif;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;

public class IiiFImageAdapterRealTest {

	// private static final String HTTP_LOCALHOST_39080_IIIF = "http://localhost:39080/iiif";
	private static final String HTTP_LOCALHOST_39080_IIIF = "http://iiifserver:80/iiif";

	@Test(enabled = false)
	public void testName() throws Exception {

		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();

		IiifImageAdapterImp iiiFImageAdapter = new IiifImageAdapterImp(HTTP_LOCALHOST_39080_IIIF,
				httpHandlerFactory);

		IiifImageParameters parameters = new IiifImageParameters("systemOne",
				"binary:binary:10143787675430", "full", "263,", "0", "default", "jpg");

		iiiFImageAdapter.requestImage(parameters);
	}

	@Test(enabled = false)
	public void testHttpHandler() throws Exception {
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();

		// String url = "http://systemone-iipimageserver:80/iiif/";
		// String url =
		// "http://systemone-fedora:8080/fcrepo/rest/systemOne/resource/binary:binary:20670231624729-master";
		String url = "http://systemone-iipimageserver:80/iiif/systemOne/binary:binary:29147131575073/full/263,/0/default.jpg";
		// String url = "https://www.svt.se";

		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");

		int responseCode = httpHandler.getResponseCode();

		assertEquals(responseCode, 200);
	}
	// OBS add-requires java.net.http in module info to run test down ther. Not needed for
	// IIIfAdapter
	// to run

	// Call to IIPserver works and the file is transfered when using only HTTP_1_1
	// @Test(enabled = false)
	// public void testUsingHTTP_1_1() throws Exception {
	// HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
	// .connectTimeout(Duration.ofSeconds(10)).build();
	//
	// HttpRequest request = HttpRequest.newBuilder().uri(new URI(
	// "http://systemone-iipimageserver:80/iiif/systemOne/binary:binary:29147131575073/full/263,/0/default.jpg"))
	// .GET().build();
	//
	// HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
	//
	// System.out.println("Response Code: " + response.statusCode());
	// System.out.println("Response Body: " + response.body());
	// }

	// Call to IIPserver hangs when swithcing protocol from HTTP_1_1 to HTTP_2
	// @Test(enabled = false)
	// public void testUsingHTTP_2() throws Exception {
	// HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
	// .connectTimeout(Duration.ofSeconds(10)).build();
	//
	// HttpRequest request = HttpRequest.newBuilder().uri(new URI(
	// "http://systemone-iipimageserver:80/iiif/systemOne/binary:binary:29147131575073/full/263,/0/default.jpg"))
	// .GET().build();
	//
	// HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
	//
	// System.out.println("Response Code: " + response.statusCode());
	// System.out.println("Response Body: " + response.body());
	// }
}