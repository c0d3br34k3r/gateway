package gateway;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class _404Handler extends Handler {

	@Override
	public void get(Path dir, List<String> subpath, HttpRequest request, HttpResponse response) throws IOException {
		response.setContent(Paths.get("404.html"));
	}

}
