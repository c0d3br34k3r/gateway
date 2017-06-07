package server;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CharsetOutputStream extends OutputStream {

	protected byte buf[];
	protected int count;

	public CharsetOutputStream() {
		this(32);
	}

	public CharsetOutputStream(int size) {
		buf = new byte[size];
	}

	private void ensureCapacity(int minCapacity) {
		if (minCapacity - buf.length > 0) {
			grow(minCapacity);
		}
	}

	private void grow(int minCapacity) {
		int oldCapacity = buf.length;
		int newCapacity = oldCapacity << 1;
		if (newCapacity - minCapacity < 0) {
			newCapacity = minCapacity;
		}
		if (newCapacity < 0) {
			if (minCapacity < 0) {
				throw new OutOfMemoryError();
			}
			newCapacity = Integer.MAX_VALUE;
		}
		buf = Arrays.copyOf(buf, newCapacity);
	}

	@Override public void write(int b) {
		ensureCapacity(count + 1);
		buf[count] = (byte) b;
		count += 1;
	}

	@Override public void write(byte b[], int off, int len) {
		ensureCapacity(count + len);
		System.arraycopy(b, off, buf, count, len);
		count += len;
	}

	public void reset() {
		count = 0;
	}

	public byte[] toByteArray() {
		return Arrays.copyOf(buf, count);
	}

	public int size() {
		return count;
	}

	public String toString(Charset charset) {
		return new String(buf, 0, count, charset);
	}

	@Override public String toString() {
		return toString(StandardCharsets.UTF_8);
	}

	@Override public void close() {}

}
