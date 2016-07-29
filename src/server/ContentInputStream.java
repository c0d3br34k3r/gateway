package server;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;

public class ContentInputStream extends InputStream {

	private long remaining;
	private InputStream in;

	ContentInputStream(InputStream in, long limit) {
		this.in = in;
		this.remaining = limit;
	}

	@Override public int available() throws IOException {
		return (int) Math.min(in.available(), remaining);
	}

	@Override public int read() throws IOException {
		if (remaining == 0) {
			return -1;
		}
		int result = in.read();
		if (result != -1) {
			--remaining;
		}
		return result;
	}

	@Override public int read(byte[] b, int off, int len) throws IOException {
		if (remaining == 0) {
			return -1;
		}
		int result = in.read(b, off, (int) Math.min(len, remaining));
		if (result != -1) {
			remaining -= result;
		}
		return result;
	}

	@Override public long skip(long n) throws IOException {
		long skipped = in.skip(Math.min(n, remaining));
		remaining -= skipped;
		return skipped;
	}

	@Override public void close() throws IOException {
		ByteStreams.skipFully(in, remaining);
		remaining = 0;
	}

}
