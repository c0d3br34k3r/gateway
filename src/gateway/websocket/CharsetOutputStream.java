package gateway.websocket;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class CharsetOutputStream extends ByteArrayOutputStream {

	CharsetOutputStream() {
		this(32);
	}

	CharsetOutputStream(int size) {
		super(size);
	}

	String toString(Charset charset) {
		return new String(buf, 0, count, charset);
	}

	@Override
	public String toString() {
		return toString(StandardCharsets.UTF_8);
	}

	@Override
	public void close() {}

}
