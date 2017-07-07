package gateway;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Gateway {

	private static final String DEFAULT_EXTENSION = ".html";

	private Map<String, String> aliases;
	private Predicate<String> hidden;
	private Handler defaultHandler;

	private static final LoadingCache<Path, Gateway> CACHE =
			CacheBuilder.newBuilder().build(new CacheLoader<Path, Gateway>() {

				@Override
				public Gateway load(Path dir) throws Exception {

					Predicate<String> hidden;
					Path hiddenPath = Paths.get(".gatewayhidden");
					if (Files.isRegularFile(hiddenPath)) {
						hidden = readHidden(hiddenPath);
					} else {
						hidden = Predicates.alwaysFalse();
					}

					Map<String, String> aliases;
					Path aliasPath = Paths.get(".gatewayalias");
					if (Files.isRegularFile(aliasPath)) {
						aliases = readAlias(aliasPath);
					} else {
						aliases = Collections.emptyMap();
					}

					Handler defaultHandler;
					Path handlerPath = Paths.get(".gateway");
					if (Files.isRegularFile(aliasPath)) {
						@SuppressWarnings("unchecked")
						Class<? extends Handler> handlerClass =
								(Class<? extends Handler>) Class.forName(new String(
										Files.readAllBytes(handlerPath), StandardCharsets.UTF_8));
						defaultHandler = handlerClass
								.newInstance();
					} else {
						defaultHandler = new _404Handler();
					}
					return new Gateway(hidden, aliases, defaultHandler);
				}

			});

	private Gateway(Predicate<String> hidden, Map<String, String> aliases,
			Handler defaultHandler) {
		this.hidden = hidden;
		this.aliases = aliases;
		this.defaultHandler = defaultHandler;
	}

	private void handleDirectory(Path dir, Iterator<String> path) {
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

	private static Map<String, String> readAlias(Path aliasPath) throws IOException {
		try (Reader reader = Files.newBufferedReader(aliasPath, StandardCharsets.UTF_8)) {
			return new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
		}
	}

}
