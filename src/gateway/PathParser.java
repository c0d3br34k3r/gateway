package gateway;

import java.util.ArrayList;
import java.util.List;

public class PathParser {

	public static List<String> parse(String query) {
		List<String> parts = new ArrayList<>();
		StringBuilder builder = new StringBuilder();

		int i = 0;
		while (i < query.length()) {
			char c = query.charAt(i++);
			switch (c) {
				case '/':
					parts.add(builder.toString());
					builder.setLength(0);
					break;
				case '%':
					builder.append((char) Integer.parseInt(query.substring(i, i + 2), 16));
					i += 2;
					break;
				default:
					builder.append(c);
			}
		}
		if (builder.length() > 0) {
			parts.add(builder.toString());
		}
		return parts;
	}

}
