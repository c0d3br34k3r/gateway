package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import websocket.Websocket;

public class HttpInput {

	private BufferedInputStream in;

	public HttpInput(BufferedInputStream in) {
		this.in = in;
	}

	public HttpInput(InputStream in) {
		this.in = new BufferedInputStream(in);
	}

	public String readLine() throws IOException {
		@SuppressWarnings("resource")
		CharsetOutputStream lineBuilder = new CharsetOutputStream();
		for (;;) {
			int b = in.read();
			if (b == '\r') {
				in.mark(1);
				int b2 = in.read();
				if (b2 == '\n') {
					String line = lineBuilder.toString(StandardCharsets.US_ASCII);
					lineBuilder.reset();
					return line;
				}
				in.reset();
			} else if (b == -1) {
				return null;
			}
			lineBuilder.write(b);
		}
	}

	public byte[] readChunked() throws IOException {
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		int length;
		for(;;) {
			length = Integer.parseInt(readLine(), 16);
			if (length == 0) {
				return content.toByteArray();
			}
			content.write(readContent(length));
			if (!readLine().isEmpty()) {
				throw new IOException();
			}
		}
	}

	public byte[] readContent(int count) throws IOException {
		byte[] content = new byte[count];
		int pos = 0;
		do {
			int read = in.read(content, pos, count - pos);
			if (read == -1) {
				throw new IOException();
			}
			pos += read;
		} while (pos < count);
		return content;
	}

	public Websocket websocket(BufferedOutputStream out) {
		return new Websocket(in, out);
	}

}
