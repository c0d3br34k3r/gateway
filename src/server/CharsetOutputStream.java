package server;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class CharsetOutputStream extends ByteArrayOutputStream {

	public CharsetOutputStream() {
		super();
	}

	public CharsetOutputStream(int size) {
		super(size);
	}

	String toString(Charset charset) {
		return new String(buf, 0, count, charset);
	}

}
