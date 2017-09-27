package gateway;

import java.io.IOException;
import java.nio.file.Path;

public abstract class Handler {

	public abstract void get(Path file, HttpRequest request, HttpResponse response) throws IOException;

}
