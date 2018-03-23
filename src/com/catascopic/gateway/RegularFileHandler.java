package com.catascopic.gateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.joda.time.DateTime;

import com.google.common.net.HttpHeaders;

public class RegularFileHandler extends Handler {

	@Override
	public void get(Path file, HttpRequest request, HttpResponse response) throws IOException {
		if (!Files.exists(file)) {
			new _404Handler().get(file, request, response);
			return;
		}
		DateTime modifiedTime = HttpDateTimeFormat.getLastModifiedTime(file);
		DateTime ifModifiedSince = request.getHeaderAsDateTime(HttpHeaders.IF_MODIFIED_SINCE);
		if (ifModifiedSince != null && !modifiedTime.isAfter(ifModifiedSince)) {
			response.setStatus(HttpStatus._304_NOT_MODIFIED);
			return;
		}
		response.setStatus(HttpStatus._200_OK).setContentAndLastModified(file);
	}

}
