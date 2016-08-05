package server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.io.ByteStreams;

/**
 * Reads HTTP content from an InputStream.
 */
public class HttpInput {

	private InputStream in;

	public HttpInput(InputStream in) {
		this.in = in;
	}

	public String readLine() throws IOException {
		@SuppressWarnings("resource")
		CharsetOutputStream lineBuilder = new CharsetOutputStream();
		for (;;) {
			int b = in.read();
			if (b == '\r') {
				if (in.read() != '\n') {
					throw new IOException();
				}
				return lineBuilder.toString(StandardCharsets.US_ASCII);
			} else if (b == -1) {
				throw new IOException();
			}
			lineBuilder.write(b);
		}
	}

	public Map<String, String> readHeaders() throws IOException {
		Map<String, String> headers = new LinkedHashMap<>();
		readHeaders(headers);
		return headers;
	}

	public void readHeaders(Map<String, String> headers) throws IOException {
		String line;
		for (;;) {
			line = readLine();
			if (line.isEmpty()) {
				return;
			}
			int separator = line.indexOf(':');
			if (separator == -1) {
				throw new IOException("Malformed header: " + line);
			}
			headers.put(line.substring(0, separator).trim(),
					line.substring(separator + 1).trim());
		}
	}

	public InputStream streamContent(int count) {
		return new ContentInputStream(in, count);
	}

	public InputStream streamChunked() throws IOException {
		return new ChunkedInputStream();
	}

	private class ChunkedInputStream extends InputStream {

		private long remaining;
		private boolean end = false;

		public ChunkedInputStream() throws IOException {
			this.remaining = Integer.parseInt(readLine(), 16);
		}

		@Override public int read() throws IOException {
			if (end) {
				return -1;
			}
			if (remaining == 0) {
				nextChunk();
				return read();
			}
			remaining--;
			return in.read();
		}

		@Override public int read(byte[] b, int off, int len) throws IOException {
			if (end) {
				return -1;
			}
			if (len == 0) {
				return 0;
			}
			if (remaining == 0) {
				nextChunk();
				return read(b, off, len);
			}
			int read = in.read(b, off, (int) Math.min(len, remaining));
			remaining -= read;
			return read;
		}

		@Override public long skip(long n) throws IOException {
			long skipped = in.skip(Math.min(n, remaining));
			remaining -= skipped;
			return skipped;
		}

		@Override public int available() throws IOException {
			return (int) Math.min(in.available(), remaining);
		}

		@Override public void close() throws IOException {
			while (!end) {
				ByteStreams.skipFully(in, remaining);
				nextChunk();
			}
		}

		private void nextChunk() throws IOException {
			readCrlf();
			remaining = Integer.parseInt(readLine(), 16);
			if (remaining == 0) {
				readCrlf();
				end = true;
			}
		}

		private void readCrlf() throws IOException {
			int cr = in.read();
			int lf = in.read();
			if (cr != '\r' || lf != '\n') {
				throw new IOException();
			}
		}
	}

}
