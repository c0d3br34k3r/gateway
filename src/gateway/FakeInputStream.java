package gateway;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class FakeInputStream extends InputStream {

	private final byte fillByte;
	private int remaining;

	public FakeInputStream(byte fillByte, int count) {
		this.fillByte = fillByte;
		this.remaining = count;
	}

	@Override
	public int read() throws IOException {
		if (remaining <= 0) {
			return -1;
		}
		remaining--;
		return fillByte;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (remaining <= 0) {
			return -1;
		}
		int actualLen = Math.min(remaining, len);
		Arrays.fill(b, off, off + actualLen, fillByte);
		remaining -= actualLen;
		return actualLen;
	}

	@Override
	public int available() throws IOException {
		return remaining;
	}

	@Override
	public void close() {}

}
