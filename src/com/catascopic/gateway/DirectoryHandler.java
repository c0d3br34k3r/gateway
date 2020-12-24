package com.catascopic.gateway;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class DirectoryHandler implements Handler {

	private Map<String, Handler> paths;
	private Handler index;

	@Override
	public HttpResponse get(Iterator<String> path, HttpRequest request)
			throws IOException {
		if (!path.hasNext()) {
			return index.get(path, request);
		}
		String part = path.next();
		
		return prefer(paths, _404Handler.INSTANCE, "index", "")
				.get(null, request);
	}

	@SafeVarargs
	private static <K, V> V prefer(Map<K, V> map, V defaultValue, K... keys) {
		for (K k : keys) {
			V v = map.get(k);
			if (v != null) {
				return v;
			}
		}
		return defaultValue;
	}

}
