package com.catascopic.gateway.websocket;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import com.google.common.io.ByteStreams;

abstract class Websocket implements Runnable, Closeable {

	private final Socket socket;
	private final WebsocketListener listener;

	Websocket(Socket socket, WebsocketListener listener) {
		this.socket = socket;
		this.listener = listener;
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

	private static final int MASKS_LENGTH = 4;

	// In case of small length, the code is the length.
	private static final int MID_LENGTH_CODE = 126;
	private static final int LARGE_LENGTH_CODE = 127;

	// No extra bytes used for small message
	private static final int MID_LENGTH_BYTES = 2;
	private static final int LARGE_LENGTH_BYTES = 8;

	private static final int SMALL_MESSAGE_MAX_SIZE = 125;
	private static final int MID_MESSAGE_MAX_SIZE = 65535;
	// No real max for large messages

	private static final int NO_STATUS_CODE = 1005;

	private static final int DEFAULT_BUFFER_SIZE = 0x1000;

	private CharsetOutputStream currentMessage = new CharsetOutputStream();
	private boolean inProgress; // = false
	private int messageType;

	private boolean closed; // = false;
	private boolean sentClose; // false;

	private Object writeLock = new Object();

	@Override
	public void run() {
		try (Websocket closeOnError = this) {
			while (!closed) {
				handleNextMessage();
			}
		} catch (WebsocketProtocolException wpe) {
			try {
				sendClose(1002, wpe.getMessage());
			} catch (IOException sendClose) {
				wpe.addSuppressed(sendClose);
			}
			try {
				socket.close();
			} catch (IOException closeSocket) {
				wpe.addSuppressed(closeSocket);
			}
			closed = true;
			listener.onError(wpe);
		} catch (IOException ioe) {
			try {
				socket.close();
			} catch (IOException closeSocket) {
				ioe.addSuppressed(closeSocket);
			}
			closed = true;
			listener.onError(ioe);
		}
	}

	private final void handleNextMessage() throws IOException {
		InputStream in = socket.getInputStream();
		int finOpcode = in.read();
		if (finOpcode == -1) {
			// TODO: non-clean close
			handleConnectionClose();
			return;
		}
		boolean fin = (finOpcode & FIN_BIT) == FIN_BIT;
		int opcode = finOpcode & OPCODE_MASK;
		int payloadSize = readLength(in);
		byte[] masks = readBytes(in, MASKS_LENGTH);
		byte[] payload = readBytes(in, payloadSize);
		for (int i = 0; i < payloadSize; i++) {
			payload[i] ^= masks[i % MASKS_LENGTH];
		}
		if (!inProgress && opcode == CONTINUATION) {
			throw new WebsocketProtocolException("unstarted continuation");
		}
		if (fin) {
			handleFinished(opcode, payload);
		} else {
			handleUnfinished(opcode, payload);
		}
	}

	private void handleUnfinished(int opcode, byte[] payload)
			throws IOException {
		switch (opcode) {
		case TEXT:
		case BINARY:
			beginMessage(opcode);
			break;
		case CONTINUATION:
			break;
		default:
			throw new WebsocketProtocolException("bad opcode: " + opcode);
		}
		inProgress = true;
		currentMessage.write(payload);
	}

	private void handleFinished(int opcode, byte[] payload) throws IOException {
		switch (opcode) {
		case CONTINUATION:
			handleFinalContinuation(payload);
			break;
		case TEXT:
			listener.onText(new String(payload, UTF_8));
			break;
		case BINARY:
			listener.onBinary(payload);
			break;
		case PING:
			handlePing(payload);
			break;
		case PONG:
			listener.onPong(payload);
			break;
		case CLOSE:
			handleClose(payload);
			break;
		default:
			throw new WebsocketProtocolException("bad opcode: " + opcode);
		}
		inProgress = false;
	}

	private void beginMessage(int type) throws IOException {
		if (inProgress) {
			throw new WebsocketProtocolException("message in progress");
		}
		this.messageType = type;
	}

	private void handleFinalContinuation(byte[] payload) throws IOException {
		currentMessage.write(payload);
		if (messageType == TEXT) {
			listener.onText(currentMessage.toString());
		} else {
			listener.onBinary(currentMessage.toByteArray());
		}
		currentMessage.reset();
	}

	private void handleClose(byte[] payload) throws IOException {
		try {
			if (!sentClose) {
				sendClose(payload);
			}
			// TODO: clean close
			socket.close();
		} finally {
			int code;
			String message;
			if (payload.length >= 2) {
				code = ((payload[0] & 0xFF) << Byte.SIZE) | (payload[1] & 0xFF);
				message = new String(payload, 2, payload.length - 2, UTF_8);
			} else {
				code = NO_STATUS_CODE;
				message = "";
			}
			listener.onClose(code, message);
		}
	}

	private void handleConnectionClose() throws IOException {
		closed = true;
		socket.close();
		listener.onClose(1006, "");
	}

	private void handlePing(byte[] payload) throws IOException {
		sendMessage(PONG, payload);
	}

	private static int readLength(InputStream in) throws IOException {
		int b = in.read();
		if (b == -1) {
			throw new EOFException();
		}
		int lengthCode = b & LENGTH_MASK;
		switch (lengthCode) {
		case MID_LENGTH_CODE:
			return readInt(in, MID_LENGTH_BYTES);
		case LARGE_LENGTH_CODE:
			return readInt(in, LARGE_LENGTH_BYTES);
		default:
			return lengthCode;
		}
	}

	private static int readInt(InputStream in, int byteCount)
			throws IOException {
		int result = 0;
		for (int i = 0; i < byteCount; i++) {
			int b = in.read();
			if (b == -1) {
				throw new EOFException();
			}
			result |= b << (Byte.SIZE * (byteCount - i - 1));
		}
		return result;
	}

	private static byte[] readBytes(InputStream in, int count)
			throws IOException {
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

	public final void send(InputStream in) throws IOException {
		PushbackInputStream pushback = new PushbackInputStream(in, 1);
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		boolean fin = sendFrame(pushback, buffer, BINARY);
		while (!fin) {
			fin = sendFrame(pushback, buffer, CONTINUATION);
		}
	}

	private boolean sendFrame(PushbackInputStream pushback,
			byte[] buffer, int opcode) throws IOException {
		int read = pushback.read(buffer);
		int next = pushback.read();
		boolean fin = (next == -1);
		if (!fin) {
			pushback.unread(next);
		}
		sendFrame(fin, opcode, buffer, 0, read);
		return fin;
	}

	public final void sendClose() throws IOException {
		sendClose(new byte[0]);
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
		sendClose(payloadBytes);
	}

	public final void sendPing(byte[] message) throws IOException {
		sendFrame(true, PING, message);
	}

	private void sendClose(byte[] message) throws IOException {
		sendMessage(CLOSE, message);
	}

	private void sendMessage(int opcode, byte[] message) throws IOException {
		sendFrame(true, opcode, message);
	}

	private void sendFrame(boolean fin, int opcode, byte[] message)
			throws IOException {
		sendFrame(fin, opcode, message, 0, message.length);
	}

	private void sendFrame(
			boolean fin, int opcode, byte[] message, int off, int len)
			throws IOException {
		synchronized (writeLock) {
			OutputStream out = socket.getOutputStream();
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
			writeAsBytes(out, len, lengthBytes);
			out.write(message, off, len);
			out.flush();
		}
	}

	private static void writeAsBytes(OutputStream out, long bytes,
			int byteCount)
			throws IOException {
		for (int i = 0; i < byteCount; i++) {
			out.write((int) (bytes >> (Byte.SIZE * (byteCount - i - 1))));
		}
	}

	@Override
	public final void close() throws IOException {
		socket.close();
	}

}
