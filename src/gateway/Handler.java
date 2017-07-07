package gateway;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class Handler {

	public abstract void get(Path dir,
			List<String> subpath,
			HttpRequest request,
			HttpResponse response) throws IOException;

}
