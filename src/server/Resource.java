package server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;

public abstract class Resource {

	protected abstract void get(String requestUri,
			Map<String, String> headers,
			BufferedOutputStream out)
					throws IOException;

	protected abstract void post(String requestUri,
			Map<String, String> headers,
			byte[] data,
			BufferedOutputStream out)
					throws IOException;

}
