
package se.uu.ub.cora.iiif;

import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import se.uu.ub.cora.binary.iiif.IiifParameters;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;

public class IiiFImageAdapterRealTest {

	// private static final String HTTP_LOCALHOST_39080_IIIF = "http://localhost:39080/iiif";
	private static final String HTTP_LOCALHOST_39080_IIIF = "http://iiifserver:80/iiif";

	@Test(enabled = false)
	public void testName() throws Exception {

		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();

		IiifAdapterImp iiiFImageAdapter = new IiifAdapterImp(HTTP_LOCALHOST_39080_IIIF,
				httpHandlerFactory);

		Map<String, String> headers = new HashMap<>();
		IiifParameters parameters = new IiifParameters("systemOne/binary:binary:10143787675430",
				"GET", headers);

		headers.put("Accept-Encoding", "gzip, deflate, br");
		//
		iiiFImageAdapter.callIiifServer(parameters);
	}

	@Test(enabled = false)
	public void testHttpHandler() throws Exception {
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();

		// String url = "http://systemone-iipimageserver:80/iiif/";
		// String url =
		// "http://systemone-fedora:8080/fcrepo/rest/systemOne/resource/binary:binary:20670231624729-master";
		// String url =
		// "http://systemone-iipimageserver:80/iiif/systemOne/binary:binary:29147131575073/full/263,/0/default.jpg";
		String url = "http://localhost:8080/systemone/iiif/binary:binary:10143787675430/12288,8192,4096,4096/263,/0/default.jpg";
		// String url = "https://www.svt.se";

		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");
		httpHandler.setRequestProperty("headerName", "h1, h2, h3");

		int responseCode = httpHandler.getResponseCode();

		assertEquals(responseCode, 200);
	}
	// OBS add-requires java.net.http in module info to run test down ther. Not needed for
	// IIIfAdapter
	// to run

	// Call to IIPserver works and the file is transfered when using only HTTP_1_1
	@Test(enabled = false)
	public void testUsingHTTP_1_1() throws Exception {
		HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(Duration.ofSeconds(10)).build();

		// "http://systemone-iipimageserver:80/iiif/systemOne/binary:binary:29147131575073/full/263,/0/default.jpg"))
		String url = "http://systemone-iipimageserver:80/iiif/systemOne/binary:binary:10143787675430/12288,8192,4096,4096/263,/0/default.jpg";
		java.net.http.HttpRequest.Builder requetsBuilder = HttpRequest.newBuilder();
		requetsBuilder.headers("headerName", "h1", "headerName", "h2", "headerName", "h3");
		HttpRequest request = requetsBuilder.uri(new URI(url)).GET().build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		System.out.println("Response Code: " + response.statusCode());
		System.out.println("Response Body: " + response.body());
	}

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