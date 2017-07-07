package gateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class Handler2 {

	private HttpReader in;
	private Path root;

	void handle() throws IOException {
		Path current = root;
		HttpRequest request = HttpRequest.create(in);
		Iterator<String> path = PathParser.parse(request.path()).iterator();
		while (path.hasNext()) {
			Path next = current.resolve(path.next());
			if (Files.exists(next)) {
				if (Files.isDirectory(next)) {
					standardHandleFile(request, null);
				}
			} else {

			}
		}
	}

	private void handle(Path dir, Iterator<String> path, HttpRequest request) {
		if (!path.hasNext()) {
			handleDir(dir, request);
		}
		Path next = dir.resolve(path.next());
		if (Files.exists(next)) {
			if (Files.isDirectory(next)) {
				handle(next, path, request);
			} else {
				standardHandleFile(request, next);
			}
		} else {
			
		}
	}

	private void handleDir(Path dir, HttpRequest request) {
		// TODO Auto-generated method stub
		
	}

	private void standardHandleFile(HttpRequest request, Path file) {
		// TODO Auto-generated method stub

	}

}
