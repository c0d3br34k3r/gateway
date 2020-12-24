package com.catascopic.gateway;

import java.io.IOException;
import java.util.Iterator;

public interface Handler {

	HttpResponse get(Iterator<String> path, HttpRequest request)
			throws IOException;

}
