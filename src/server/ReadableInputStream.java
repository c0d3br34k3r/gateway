package server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.common.io.ByteStreams;

public abstract class ReadableInputStream extends InputStream {

	public byte[] getBytes() throws IOException {
		return ByteStreams.toByteArray(this);
	}

	public String getText(Charset charset) throws IOException {
		return new String(getBytes(), charset);
	}
	
	public String getText() throws IOException {
		return new String(getBytes(), StandardCharsets.UTF_8);
	}

}
