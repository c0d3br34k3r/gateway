package server;

import java.io.IOException;

public abstract class Connection {

	protected void init(HttpRequest request) {}

	protected void onGet(HttpRequest request, HttpResponse response) throws IOException {
		response.setStatus(StandardHttpStatus._400_BAD_REQUEST).send();
	}
	
	protected void onPost(HttpRequest request, HttpResponse response) throws IOException {
		response.setStatus(StandardHttpStatus._400_BAD_REQUEST).send();
	}
	
	protected void close() {}

}
