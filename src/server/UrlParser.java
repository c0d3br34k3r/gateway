package server;

import java.util.ArrayList;
import java.util.List;

public final class UrlParser {

	private UrlParser() {}

	static List<String> parse(String path) {
		List<String> parts = new ArrayList<>();
		StringBuilder segment = new StringBuilder();
		int i = 0;
		while (i < path.length()) {
			char c = path.charAt(i++);
			switch (c) {
				case '%':
					segment.append((char) Integer.parseInt(path.substring(i, i + 2), 16));
					i += 2;
					break;
				case '/':
					parts.add(segment.toString());
					segment = new StringBuilder();
				default:
					segment.append(c);
			}
		}
		parts.add(segment.toString());
		return parts;
	}

}
