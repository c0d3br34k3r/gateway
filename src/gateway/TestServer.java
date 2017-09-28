package gateway;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestServer {

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(8080);

		Socket accept = serverSocket.accept();
		for (;;) {
			for (;;) {
				HttpRequest request = HttpRequest.read(accept.getInputStream());
				HttpResponse response = new HttpResponse();
				Path file = Paths.get("." + request.path());
				new RegularFileHandler().get(file, request, response);
				System.out.println(request);
				if (request.hasContent()) {
					System.out.println(request.readContent());
				}
				System.out.println(response);
				
				response.send(accept.getOutputStream());
			}
		}
	}

}
