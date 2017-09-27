package gateway;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

public final class QueryParser {

	private QueryParser() {}

	public static Map<String, String> toMap(String query) {
		// So we don't crash on duplicate keys
		final Map<String, String> builder = new LinkedHashMap<>();
		parse(query, new MapBuilder() {

			@Override
			public void put(String key, String value) {
				builder.put(key, value);
			}
		});
		return Collections.unmodifiableMap(builder);
	}

	public static ListMultimap<String, String> toMultimap(String query) {
		final ImmutableListMultimap.Builder<String, String> builder =
				ImmutableListMultimap.builder();
		parse(query, new MapBuilder() {

			@Override
			public void put(String key, String value) {
				builder.put(key, value);
			}
		});
		return builder.build();
	}

	private static void parse(String query, MapBuilder builder) {
		StringBuilder key = new StringBuilder();
		StringBuilder value = new StringBuilder();

		StringBuilder current = key;
		int i = 0;
		while (i < query.length()) {
			char c = query.charAt(i);
			switch (c) {
				case '=':
					current = value;
					break;
				case '&':
					builder.put(key.toString(), value.toString());
					key.setLength(0);
					value.setLength(0);
					current = key;
					break;
				case '+':
					current.append(' ');
					break;
				case '%':
					current.append((char) Integer.parseInt(query.substring(i + 1, i + 3), 16));
					i += 2;
					break;
				default:
					current.append(c);
			}
			i++;
		}
		builder.put(key.toString(), value.toString());
	}

	private interface MapBuilder {
		void put(String key, String value);
	}

}
