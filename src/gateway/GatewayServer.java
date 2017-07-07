package gateway;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

public class GatewayServer {

	private static final List<String> EXTENSIONS = ImmutableList.of(".gateway", ".html");

	private void handleDirectory(Path dir, Iterator<String> path) {
		String part = path.next();
		part = MoreObjects.firstNonNull(aliases.get(part), part);

		if (hidden.apply(part)) {
			defaultHandle();
			return;
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

			Path gateway = dir.resolve(part + ".gateway");
			if (Files.isRegularFile(gateway)) {
				handleGateway(gateway);
				return;
			}

			Path html = dir.resolve(part + ".html");
			if (Files.isRegularFile(html)) {
				handleFile(html);
				return;
			}
		}

		defaultHandle();
	}

	private void handleFile(Path localPath) {
		// TODO Auto-generated method stub

	}
	
	private void handleGateway(Path localPath) {
		// TODO Auto-generated method stub

	}

	private void defaultHandle() {
		// TODO Auto-generated method stub

	}

}
