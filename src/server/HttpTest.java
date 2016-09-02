package server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

public class HttpTest extends HttpServer {

	public HttpTest() throws IOException {
		super(8080);
	}

	public static void main(String[] args) throws IOException {
		new HttpTest().start();
	}

	@Override protected void handle(HttpRequest request, HttpResponse response) throws IOException {
		switch (request.method()) {
			case GET:
				get(request, response);
				break;
			case POST:
				post(request, response);
				break;
			default:
				throw new IllegalArgumentException();
		}
	}

	private void get(HttpRequest request, HttpResponse response) throws IOException {
		HttpStatus status;
		switch (request.requestUri()) {
			case "/index.html":
				status = HttpStatus._200_OK;
				response.setContent(new File("index.html"));
				response.addCookie(new Cookie("SID", "1234"));
				break;
			case "/post-it.png":
				status = HttpStatus._304_NOT_MODIFIED;
				response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=315360000");
				// response.setHeader(HttpHeaders.EXPIRES, "Mon, 06 Apr 2026
				// 19:13:02 GMT");
				// response.setHeader(HttpHeaders.LAST_MODIFIED, "Sun, 14 Jul
				// 2013 13:05:23 GMT");
				response.setHeader(HttpHeaders.ETAG, "64d79db1fee1513ead9b6b48064b10ee");
				// response.setContent(new File("post-it.png"));
				break;
			default:
				status = HttpStatus._404_NOT_FOUND;
				response.setContent(new File("404.html"));
				break;
		}
		response.setStatus(status);

		response.send();
	}

	private void post(HttpRequest request, HttpResponse response) throws IOException {
		InputStream content = request.content();
		String message = new String(ByteStreams.toByteArray(content), StandardCharsets.UTF_8);
		String reverse = new StringBuilder(message).reverse().toString();
		response.setStatus(HttpStatus._200_OK);
		response.setContent(reverse);
		response.send();
	}

}
