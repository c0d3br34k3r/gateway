package server;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharsetOutputStream extends ByteArrayOutputStream {

	public CharsetOutputStream() {
		super();
	}

	public CharsetOutputStream(int size) {
		super(size);
	}

	public String toString(Charset charset) {
		return new String(buf, 0, count, charset);
	}

	@Override public String toString() {
		return new String(buf, 0, count, StandardCharsets.UTF_8);
	}

}
