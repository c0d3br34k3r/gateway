package gateway;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.joda.time.DateTime;

import com.google.common.net.HttpHeaders;

public class TestServer {

	public static void main(String[] args) throws IOException {
		DateTime modified = DateTime.now().minusDays(7);
		ServerSocket serverSocket = new ServerSocket(8080);

		Socket accept = serverSocket.accept();

		for (;;) {
			for (;;) {
				HttpRequest request = HttpRequest.read(accept.getInputStream());
				DateTime ifModifiedSince =
						request.getHeaderAsDateTime(HttpHeaders.IF_MODIFIED_SINCE);
				System.out.println(request + "\n" + ifModifiedSince);

				HttpResponse response = new HttpResponse();
				Path file = Paths.get("." + request.path());
				DateTime lastModifiedTime;
				if (!Files.exists(file)) {
					new _404Handler().get(null, request, response);
					lastModifiedTime = null;
				} else {
					lastModifiedTime = HttpDateTimeFormat.getLastModifiedTime(file);
					System.out.println(lastModifiedTime.isAfter(ifModifiedSince));
					if (ifModifiedSince != null && !lastModifiedTime.isAfter(ifModifiedSince)) {
						response.setStatus(HttpStatus._304_NOT_MODIFIED);
					} else {
						response.setStatus(HttpStatus._200_OK)
								.setContent(file)
								.setLastModified(lastModifiedTime);
					}
				}
				System.out.println(response + "\n" + lastModifiedTime);
				response.send(accept.getOutputStream());
			}
		}
	}

}
