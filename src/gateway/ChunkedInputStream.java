package gateway;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;

class ChunkedInputStream extends InputStream {

	private InputStream in;
	private long remaining;
	private boolean end; // = false

	public ChunkedInputStream(InputStream in) throws IOException {
		this.in = in;
		this.remaining = Integer.parseInt(readLine(), 16);
	}

	@Override
	public int read() throws IOException {
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

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (len == 0) {
			return 0;
		}
		if (end) {
			return -1;
		}
		if (remaining == 0) {
			nextChunk();
			return read(b, off, len);
		}
		int read = in.read(b, off, (int) Math.min(len, remaining));
		remaining -= read;
		return read;
	}

	@Override
	public long skip(long n) throws IOException {
		long skipped = in.skip(Math.min(n, remaining));
		remaining -= skipped;
		return skipped;
	}

	@Override
	public int available() throws IOException {
		return (int) Math.min(in.available(), remaining);
	}

	@Override
	public void close() throws IOException {
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
