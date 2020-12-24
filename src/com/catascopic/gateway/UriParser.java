package com.catascopic.gateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

public final class UriParser {
	private UriParser() {}

	public static List<String> parse(String query) {
		List<String> parts = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		int i = 0;
		while (i < query.length()) {
			char c = query.charAt(i);
			switch (c) {
			case '/':
				parts.add(builder.toString());
				builder = new StringBuilder();
				i++;
				break;
			case '%':
				builder.append((char) Integer.parseInt(
						query.substring(i + 1, i + 3), 16));
				i += 3;
				break;
			default:
				builder.append(c);
				i++;
			}
		}
		if (builder.length() > 0) {
			parts.add(builder.toString());
		}
		return parts;
	}

	public static Map<String, String> toMap(String query) {
		// So we don't crash on duplicate keys
		final Map<String, String> map = new LinkedHashMap<>();
		parse(query, new MapBuilder() {

			@Override
			public void put(String key, String value) {
				// TODO: we can do this more cleanly with java 8
				String prev = map.put(key, value);
				if (prev != null) {
					// whoops, put it back
					map.put(key, prev);
				}
			}
		});
		return Collections.unmodifiableMap(map);
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
				key = new StringBuilder();
				value = new StringBuilder();
				current = key;
				break;
			case '+':
				current.append(' ');
				break;
			case '%':
				current.append((char) Integer.parseInt(
						query.substring(i + 1, i + 3), 16));
				i += 2;
				break;
			default:
				current.append(c);
				i++;
			}
		}
		builder.put(key.toString(), value.toString());
	}

	private interface MapBuilder {

		void put(String key, String value);
	}

}
