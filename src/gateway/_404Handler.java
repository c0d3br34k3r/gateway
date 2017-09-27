package gateway;

import java.io.IOException;
import java.nio.file.Path;

import com.google.common.net.MediaType;

public class _404Handler extends Handler {

	@Override
	public void get(Path dir, HttpRequest request, HttpResponse response) throws IOException {
		response.setStatus(HttpStatus._404_NOT_FOUND)
				.setContent(HttpStatus._404_NOT_FOUND.toString())
				.setContentType(MediaType.PLAIN_TEXT_UTF_8);
	}

}
