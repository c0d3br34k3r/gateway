package server;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.ByteSource;
import com.google.common.net.HttpHeaders;

public class HttpTest extends HttpServer {

	public HttpTest() throws IOException {
		super(8080);
	}

	private static final Path ROOT;

	static {
		try {
			ROOT = Paths.get("./server").toRealPath();
		} catch (IOException e) {
			throw new IllegalArgumentException();
		}
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
				break;
			case "/post-it.png":
				status = HttpStatus._304_NOT_MODIFIED;
				response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=315360000");
//				response.setHeader(HttpHeaders.EXPIRES, "Mon, 06 Apr 2026 19:13:02 GMT");
//				response.setHeader(HttpHeaders.LAST_MODIFIED, "Sun, 14 Jul 2013 13:05:23 GMT");
				response.setHeader(HttpHeaders.ETAG, "\"64d79db1fee1513ead9b6b48064b10ee\"");
//				response.setContent(new File("post-it.png"));
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
		ByteSource content = request.content();
		String message = new String(content.read(), StandardCharsets.UTF_8);
		String reverse = new StringBuilder(message).reverse().toString();
		response.setStatus(HttpStatus._200_OK);
		response.setContent(reverse);
		response.send();
	}

	private static boolean isLegalResource(File file) throws IOException {
		return file.toPath().toRealPath().startsWith(ROOT);
	}

}
