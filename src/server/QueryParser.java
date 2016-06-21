package server;

import java.util.Map;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

public class QueryParser {

	public static Map<String, String> toMap(String query) {
		final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		parse(query, new MapBuilder() {

			@Override public void put(String key, String value) {
				builder.put(key, value);
			}
		});
		return builder.build();
	}

	public static ListMultimap<String, String> toMultimap(String query) {
		final ImmutableListMultimap.Builder<String, String> builder =
				ImmutableListMultimap.builder();
		parse(query, new MapBuilder() {

			@Override public void put(String key, String value) {
				builder.put(key, value);
			}
		});
		return builder.build();
	}

	public static void parse(String query, MapBuilder builder) {
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
				case ';':
					builder.put(key.toString(), value.toString());
					key = new StringBuilder();
					value = new StringBuilder();
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
		if (key.length() != 0) {
			builder.put(key.toString(), value.toString());
		}
	}

	private interface MapBuilder {
		void put(String key, String value);
	}

}
