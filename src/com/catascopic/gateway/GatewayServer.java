package com.catascopic.gateway;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

public class GatewayServer {

	private static final String HTML_EXTENSION = ".html";
	private static final String GATEWAY_EXTENSION = ".gateway";

	private void handleDirectory(Path dir, Iterator<String> path) {
		String part = path.next();
		// part = MoreObjects.firstNonNull(aliases.get(part), part);

		// if (hidden.apply(part)) {
		// defaultHandle();
		// return;
		// }

		Path local = dir.resolve(part);

		if (path.hasNext() && Files.isDirectory(local)) {
			handleDirectory(local, path);
			return;
		}

		if (!path.hasNext()) {
			if (Files.isRegularFile(local)) {
				handleFile(local);
				return;
			}
			Path html = dir.resolve(part + HTML_EXTENSION);
			if (Files.isRegularFile(html)) {
				handleFile(html);
				return;
			}
		}

		Path gateway = dir.resolve(part + GATEWAY_EXTENSION);
		if (Files.isRegularFile(gateway)) {
			handleGateway(gateway, Lists.newArrayList(path));
			return;
		}

		defaultHandle();
	}

	private void handleFile(Path localPath) {
		// TODO Auto-generated method stub

	}

	private void handleGateway(Path localPath, List<String> remaining) {
		// TODO Auto-generated method stub

	}

	private void defaultHandle() {
		// TODO Auto-generated method stub

	}

}
