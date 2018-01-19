package gateway;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.ByteStreams;

public class PathParser {

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
				builder.append((char) Integer.parseInt(query.substring(i + 1, i + 3), 16));
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

	public static List<String> parse(InputStream in) throws IOException {
		List<String> parts = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		for (;;) {
			int ch = in.read();
			switch (ch) {
			case -1:
				throw new EOFException();
			case '/':
				parts.add(builder.toString());
				builder = new StringBuilder();
				break;
			case '%':
				String code = new String(new char[] { readChar(in), readChar(in) });
				builder.append((char) Integer.parseInt(code, 16));
				break;
			case '?':

			case ' ':
				if (builder.length() > 0) {
					parts.add(builder.toString());
				}
				return parts;
			default:
				builder.append((char) ch);
			}
		}
	}

	private static char readChar(InputStream in) throws IOException {
		int b = in.read();
		if (b == -1) {
			throw new EOFException();
		}
		return (char) b;
	}

}
