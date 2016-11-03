package websocket;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import com.google.common.io.ByteStreams;

public class OldWebsocket implements Closeable {

	private final InputStream in;
	private final OutputStream out;

	public OldWebsocket(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	public OldWebsocket(Socket socket) throws IOException {
		this(socket.getInputStream(), socket.getOutputStream());
	}

	// Opcodes
	public static final int CONTINUATION = 0x0;
	public static final int TEXT = 0x1;
	public static final int BINARY = 0x2;
	public static final int CLOSE = 0x8;
	public static final int PING = 0x9;
	public static final int PONG = 0xA;

	private static final int FIN_BIT = 0x80;
	private static final int OPCODE_MASK = 0xF;
	private static final int LENGTH_MASK = 0x7F;

	private static final int NUM_MASKS = 4;

	// In case of small length, the code is the length.
	private static final int MID_LENGTH_CODE = 126;
	private static final int LARGE_LENGTH_CODE = 127;

	// No extra bytes used for small message
	private static final int MID_LENGTH_BYTES = 2;
	private static final int LARGE_LENGTH_BYTES = 8;

	private static final int SMALL_MESSAGE_MAX_SIZE = 125;
	private static final int MID_MESSAGE_MAX_SIZE = 65535;
	// No real max for large messages

	public Message read() throws IOException {
		int finOpcode = in.read();
		if (finOpcode == -1) {
			return null;
		}
		// most messages will only be 1 frame
		boolean fin = (finOpcode & FIN_BIT) == FIN_BIT;
		int firstOpcode = finOpcode & OPCODE_MASK;
		int payloadSize = readLength();
		int count = payloadSize;
		byte[] buf = new byte[fin ? payloadSize : payloadSize * 4];
		readPayload(buf, 0, payloadSize);

		while (!fin) {
			finOpcode = in.read();
			if (finOpcode == -1) {
				throw new IOException();
			}
			fin = (finOpcode & FIN_BIT) == FIN_BIT;
			if ((finOpcode & OPCODE_MASK) != CONTINUATION) {
				throw new IOException();
			}
			payloadSize = readLength();

			// resize buffer if necessary
			int minCapacity = count + payloadSize;
			if (minCapacity > buf.length) {
				buf = Arrays.copyOf(buf, fin
						? minCapacity
						: Math.max(buf.length * 2, minCapacity));
			}
			readPayload(buf, count, payloadSize);
			count += payloadSize;
		}
		return new Message(buf, count, firstOpcode);
	}

	private void readPayload(byte[] buf, int off, int len) throws IOException {
		byte[] masks = readBytes(NUM_MASKS);
		ByteStreams.readFully(in, buf, off, len);
		for (int i = 0; i < len; i++) {
			buf[off + i] ^= masks[i % NUM_MASKS];
		}
	}

	private int readLength() throws IOException {
		int lengthCode = in.read() & LENGTH_MASK;
		int bytes;
		switch (lengthCode) {
			case MID_LENGTH_CODE:
				bytes = MID_LENGTH_BYTES;
				break;
			case LARGE_LENGTH_CODE:
				bytes = LARGE_LENGTH_BYTES;
				break;
			default:
				return lengthCode;
		}
		return (int) readLong(bytes);
	}

	private long readLong(int bytes) throws IOException {
		long result = 0;
		for (int i = 0; i < bytes; i++) {
			result |= in.read() << (Byte.SIZE * (bytes - i - 1));
		}
		return result;
	}

	private byte[] readBytes(int count) throws IOException {
		byte[] buf = new byte[count];
		ByteStreams.readFully(in, buf);
		return buf;
	}

	public void send(String message) throws IOException {
		sendSingleFrame(TEXT, message.getBytes(UTF_8));
	}

	public void send(byte[] message) throws IOException {
		sendSingleFrame(BINARY, message);
	}

	public void close(int code) throws IOException {
		close(code, "");
	}

	public void closeWithoutCode() throws IOException {
		sendSingleFrame(CLOSE, new byte[0]);
	}

	public void close(byte[] bytes) throws IOException {
		sendSingleFrame(CLOSE, bytes);
	}

	public void close(int code, String message) throws IOException {
		byte[] messageBytes = message.getBytes(UTF_8);
		byte[] closeBytes = new byte[2 + messageBytes.length];
		closeBytes[0] = (byte) ((code & 0xFF00) >> Byte.SIZE);
		closeBytes[1] = (byte) (code & 0xFF);
		System.arraycopy(messageBytes, 0, closeBytes, 2, messageBytes.length);
		sendSingleFrame(CLOSE, closeBytes);
	}

	public void ping(byte[] bytes) throws IOException {
		sendSingleFrame(PING, bytes);
	}

	public void pong(byte[] bytes) throws IOException {
		sendSingleFrame(PONG, bytes);
	}

	private void sendSingleFrame(int opcode, byte[] message) throws IOException {
		sendFrame(true, opcode, message);
	}

	public void sendFrame(boolean fin, int opcode, byte[] message) throws IOException {
		sendFrame(fin, opcode, message, 0, message.length);
	}

	public void sendFrame(boolean fin, int opcode, byte[] message, int off, int len)
			throws IOException {
		out.write((fin ? FIN_BIT : 0) | opcode);
		int lengthCode;
		int lengthBytes;
		if (message.length <= SMALL_MESSAGE_MAX_SIZE) {
			lengthCode = message.length;
			lengthBytes = 0;
		} else if (message.length <= MID_MESSAGE_MAX_SIZE) {
			lengthCode = MID_LENGTH_CODE;
			lengthBytes = MID_LENGTH_BYTES;
		} else {
			lengthCode = LARGE_LENGTH_CODE;
			lengthBytes = LARGE_LENGTH_BYTES;
		}
		out.write(lengthCode);
		writeAsBytes(len, lengthBytes);
		out.write(message, off, len);
		out.flush();
	}

	private void writeAsBytes(long bytes, int byteCount) throws IOException {
		for (int i = 0; i < byteCount; i++) {
			out.write((int) (bytes >> (Byte.SIZE * (byteCount - i - 1))));
		}
	}

	@Override public void close() throws IOException {
		in.close();
		out.close();
	}

	public static class Message {

		private final byte[] data;
		private final int count;
		private final int type;

		private Message(byte[] data, int count, int type) {
			this.data = data;
			this.count = count;
			this.type = type;
		}

		public int type() {
			return type;
		}

		public int length() {
			return count;
		}

		public String text() {
			return new String(data, 0, count, UTF_8);
		}

		public byte[] binary() {
			return Arrays.copyOf(data, count);
		}

		public int closeCode() {
			if (count == 0) {
				return -1;
			}
			return ((data[0] & 0xFF) << Byte.SIZE) | (data[1] & 0xFF);
		}

		public String closeMessage() {
			if (count <= 2) {
				return null;
			}
			return new String(data, 2, count - 2, UTF_8);
		}
	}

}
