package com.catascopic.gateway;

import java.util.Iterator;

import com.google.common.net.MediaType;

public enum _404Handler implements Handler {

	INSTANCE;

	@Override
	public HttpResponse get(Iterator<String> path, HttpRequest request) {
		return new HttpResponse().setStatus(HttpStatus._404_NOT_FOUND)
				.setContent("404 Not Found")
				.setContentType(MediaType.PLAIN_TEXT_UTF_8);
	}

}
