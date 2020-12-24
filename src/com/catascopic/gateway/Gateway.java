package com.catascopic.gateway;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.stream.JsonReader;

public class Gateway {

	private static final String DEFAULT_EXTENSION = ".html";

	private Map<String, String> aliases;
	private Predicate<String> hidden;
	private Handler defaultHandler;

	private Gateway(Path dir) throws IOException {

		for (Path path : Files.newDirectoryStream(dir)) {
			if (Files.isRegularFile(path)) {

			} else if (Files.isDirectory(path)) {

			}
		}
	}

	private void handleDirectory(Path dir, Iterator<String> path)
			throws IOException {
		
		String part = path.next();
		String alias = aliases.get(part);
		if (alias == null) {
			// only non-aliased files can be hidden
			if (hidden.apply(part)) {
				defaultHandle();
				return;
			}
		} else {
			part = alias;
		}

		Path local = dir.resolve(part);
		if (path.hasNext()) {
			if (Files.isDirectory(local)) {
				handleDirectory(local, path);
				return;
			}
		} else {
			if (Files.isRegularFile(local)) {
				handleFile(local);
				return;
			}
			local = dir.resolve(part + DEFAULT_EXTENSION);
			if (Files.isRegularFile(local)) {
				handleFile(local);
				return;
			}
		}
		defaultHandle();
	}

	private void handleFile(Path localPath) {
		// TODO Auto-generated method stub

	}

	private void defaultHandle() {
		// TODO Auto-generated method stub

	}

	private static Predicate<String> readHidden(Path path) throws IOException {
		List<Predicate<String>> filters = new ArrayList<>();
		for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
			if (!line.isEmpty()) {
				filters.add(FileMatcher.of(line.trim()));
			}
		}
		return Predicates.and(filters);
	}

	private static Map<String, String> readAlias(Path aliasPath)
			throws IOException {
		try (Reader reader = Files.newBufferedReader(aliasPath,
				StandardCharsets.UTF_8)) {
			return readMap(reader);
		}
	}

	private static Map<String, String> readMap(Reader reader)
			throws IOException {
		JsonReader json = new JsonReader(reader);
		Builder<String, String> builder = ImmutableMap.builder();
		json.beginObject();
		while (json.hasNext()) {
			builder.put(json.nextName(), json.nextString());
		}
		json.endObject();
		return builder.build();
	}

}
