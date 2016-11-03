package websocket;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.common.io.ByteStreams;

import server.CharsetOutputStream;

public abstract class Websocket implements Runnable {

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

	private final InputStream in;
	private final OutputStream out;

	private CharsetOutputStream currentMessage = new CharsetOutputStream();
	boolean inProgress; // = false
	boolean messageIsText;

	protected Websocket(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	protected Websocket(Socket socket) throws IOException {
		this(socket.getInputStream(), socket.getOutputStream());
	}

	@Override public final void run() {
		for (;;) {
			try {
				handleNextMessage();
			} catch (IOException e) {
				onError(e);
				break;
			}
		}
	}

	private void handleNextMessage() throws IOException {
		int finOpcode = in.read();
		if (finOpcode == -1) {
			onClose(1005, "");
		}
		boolean fin = (finOpcode & FIN_BIT) == FIN_BIT;
		int opcode = finOpcode & OPCODE_MASK;
		int payloadSize = readLength();
		byte[] masks = readBytes(NUM_MASKS);
		byte[] payload = readBytes(payloadSize);
		for (int i = 0; i < payloadSize; i++) {
			payload[i] ^= masks[i % NUM_MASKS];
		}
		if (!inProgress && opcode == CONTINUATION) {
			throw new IOException("unstarted continuation");
		}
		if (fin) {
			handleFinished(opcode, payload);
		} else {
			handleUnfinished(opcode, payload);
		}
	}

	private void handleFinished(int opcode, byte[] payload) throws IOException {
		switch (opcode) {
			case CONTINUATION:
				handleLastContinuation(payload);
				break;
			case TEXT:
				onMessage(new String(payload, StandardCharsets.UTF_8));
				break;
			case BINARY:
				onMessage(payload);
				break;
			case PING:
				handlePing(payload);
				break;
			case CLOSE:
				handleClose(payload);
				break;
			default:
				throw new IOException("bad opcode: " + opcode);
		}
		inProgress = false;
	}

	private void handleUnfinished(int opcode, byte[] payload) throws IOException {
		switch (opcode) {
			case TEXT:
				beginMessage(true);
				break;
			case BINARY:
				beginMessage(false);
				break;
			case CONTINUATION:
				break;
			default:
				throw new IOException("opcode " + opcode + " must have fin set");
		}
		inProgress = true;
		currentMessage.write(payload);
	}

	private void beginMessage(boolean isText) throws IOException {
		if (inProgress) {
			throw new IOException();
		}
		this.messageIsText = isText;
	}

	private void handleLastContinuation(byte[] payload) throws IOException {
		currentMessage.write(payload);
		if (messageIsText) {
			onMessage(currentMessage.toString());
		} else {
			onMessage(currentMessage.toByteArray());
		}
		currentMessage.reset();
	}

	private void handleClose(byte[] payload) {
		int code;
		String message;
		if (payload.length >= 2) {
			code = ((payload[0] & 0xFF) << Byte.SIZE) | (payload[1] & 0xFF);
			message = new String(payload, 2, payload.length - 2, StandardCharsets.UTF_8);
		} else {
			code = 1005;
			message = "";
		}
		onClose(code, message);
	}

	private void handlePing(byte[] payload) throws IOException {
		sendMessage(PONG, payload);
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

	// Write methods

	public final void send(String message) throws IOException {
		sendMessage(TEXT, message.getBytes(UTF_8));
	}

	public final void send(byte[] message) throws IOException {
		sendMessage(BINARY, message);
	}

	public final void sendClose() throws IOException {
		sendMessage(CLOSE, new byte[0]);
	}

	public final void sendClose(int code) throws IOException {
		sendClose(code, "");
	}

	public final void sendClose(int code, String message) throws IOException {
		byte[] messageBytes = message.getBytes(UTF_8);
		byte[] payloadBytes = new byte[2 + messageBytes.length];
		payloadBytes[0] = (byte) ((code & 0xFF00) >> Byte.SIZE);
		payloadBytes[1] = (byte) (code & 0xFF);
		System.arraycopy(messageBytes, 0, payloadBytes, 2, messageBytes.length);
		sendMessage(CLOSE, payloadBytes);
	}

	private void sendMessage(int opcode, byte[] message) throws IOException {
		sendFrame(true, opcode, message);
	}

	private void sendFrame(boolean fin, int opcode, byte[] message) throws IOException {
		sendFrame(fin, opcode, message, 0, message.length);
	}

	private synchronized void sendFrame(boolean fin, int opcode, byte[] message, int off, int len)
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

	// Listnener methods

	protected void onMessage(String text) {}

	protected void onMessage(byte[] bytes) {}

	protected void onClose(int code, String message) {}

	protected void onError(IOException e) {}

}
