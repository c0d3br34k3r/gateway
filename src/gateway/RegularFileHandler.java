package gateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.joda.time.DateTime;

import com.google.common.net.HttpHeaders;

public class RegularFileHandler extends Handler {

	@Override
	public void get(Path dir, List<String> subpath, HttpRequest request, HttpResponse response)
			throws IOException {
		Path file = dir.resolve(subpath.get(subpath.size() - 1));
		if (!Files.exists(file)) {
			response.setStatus(StandardHttpStatus._404_NOT_FOUND);
			return;
		}
		DateTime modifiedTime = new DateTime(Files.getLastModifiedTime(file).toMillis());
		String ifModifiedSince = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
		if (ifModifiedSince != null
				&& !modifiedTime.isAfter(HttpDateTimeFormat.parse(ifModifiedSince))) {
			response.setStatus(StandardHttpStatus._304_NOT_MODIFIED);
			return;
		}
		response.setStatus(StandardHttpStatus._200_OK)
				.setLastModified(modifiedTime)
				.setContent(file);
	}

}
