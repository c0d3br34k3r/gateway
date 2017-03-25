package websocket;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.ByteStreams;

public abstract class AbstractWebsocket {

	/**
	 * Override this method to return the OutputStream.
	 * 
	 * @return the OutputStream for writing messages
	 * @throws IOException if an I/O error occurs while getting the OutputStream
	 */
	protected abstract OutputStream getOutputStream() throws IOException;

	/**
	 * Override this method to return the InputStream.
	 * 
	 * @return the InputStream for reading messages.
	 * @throws IOException if an I/O error occurs while getting the InputStream
	 */
	protected abstract InputStream getInputStream() throws IOException;

	/**
	 * Handles a single frame.
	 * 
	 * @param fin the fin bit
	 * @param opcode the opcode
	 * @param payload the payload bytes
	 * @throws WebsocketProtocolException 
	 * @throws IOException 
	 */
	protected abstract void handleFrame(boolean fin, int opcode, byte[] payload) throws IOException;

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

	/**
	 * Reads a frame from the InputStream, and handles it appropriately,
	 * depending on the opcode and the fin bit.
	 * <ul>
	 * <li>If the frame is a text, binary, or continuation frame with the fin
	 * bit set, {@link #onMessage(String)} or {@link #onMessage(byte[])} is
	 * called appropriately with the message contents.
	 * <li>If the frame is a text, binary, or continuation frame with the fin
	 * bit not set, the payload is written to the message buffer for later use.
	 * <li>If the frame is a close frame, then this Websocket sends a close
	 * frame in response, and calls {@link #onClose(int, String)} with the
	 * received close code and message.
	 * <li>If the frame is a ping frame, then this Websocket sends a pong frame
	 * in response.
	 * <li>If the frame is a pong frame, nothing happens.
	 * </ul>
	 * 
	 * @throws IOException if an I/O error occurs
	 * @throws WebsocketProtocolException if any of the following occur:
	 *         <ul>
	 *         <li>An unknown opcode is received
	 *         <li>A continuation frame is received and no message is in
	 *         progress
	 *         <li>A text or binary frame is received and a message is already
	 *         in progress
	 *         <li>A ping, pong, or close frame without the fin bit set is
	 *         received
	 *         </ul>
	 */
	protected final void handleNextFrame() throws IOException {
		InputStream in = getInputStream();
		int finOpcode = in.read();
		if (finOpcode == -1) {
			throw new IOException();
		}
		boolean fin = (finOpcode & FIN_BIT) == FIN_BIT;
		int opcode = finOpcode & OPCODE_MASK;
		int payloadSize = readLength(in);
		byte[] masks = readBytes(in, NUM_MASKS);
		byte[] payload = readBytes(in, payloadSize);
		for (int i = 0; i < payloadSize; i++) {
			payload[i] ^= masks[i % NUM_MASKS];
		}
		handleFrame(fin, opcode, payload);
	}

	private static int readLength(InputStream in) throws IOException {
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
		return (int) readLong(in, bytes);
	}

	private static long readLong(InputStream in, int bytes) throws IOException {
		long result = 0;
		for (int i = 0; i < bytes; i++) {
			result |= in.read() << (Byte.SIZE * (bytes - i - 1));
		}
		return result;
	}

	private static byte[] readBytes(InputStream in, int count) throws IOException {
		byte[] buf = new byte[count];
		ByteStreams.readFully(in, buf);
		return buf;
	}

	// Write methods

	/**
	 * Sends a single-frame text message with the given text.
	 * 
	 * @param message the text content of the message
	 * @throws IOException if an I/O error occurs while sending the message
	 * @throws WebsocketProtocolException if a close frame has been sent or
	 *         received
	 */
	public final void send(String message) throws IOException {
		sendMessage(TEXT, message.getBytes(UTF_8));
	}

	/**
	 * Sends a single-frame binary message with the given data.
	 * 
	 * @param message the binary content of the message
	 * @throws IOException if an I/O error occurs while sending the message
	 * @throws WebsocketProtocolException if a close frame has been sent or
	 *         received
	 */
	public final void send(byte[] message) throws IOException {
		sendMessage(BINARY, message);
	}

	/**
	 * Sends a close message with no close code and no close message.
	 * 
	 * @throws IOException if an I/O error occurs while sending the close
	 *         message
	 */
	public final void sendClose() throws IOException {
		sendClose(new byte[0]);
	}

	/**
	 * Sends a close message with the given close code and no close message.
	 * 
	 * @param code the close code
	 * @throws IOException if an I/O error occurs while sending the close
	 *         message
	 * @throws WebsocketProtocolException if a close frame has been sent or
	 *         received
	 */
	public final void sendClose(int code) throws IOException {
		sendClose(code, "");
	}

	/**
	 * Sends a close message with the given close code and close message.
	 * 
	 * @param code the close code
	 * @param message the close message
	 * @throws IOException if an I/O error occurs while sending the close
	 *         message
	 * @throws WebsocketProtocolException if a close frame has been sent or
	 *         received
	 */
	public final void sendClose(int code, String message) throws IOException {
		byte[] messageBytes = message.getBytes(UTF_8);
		byte[] payloadBytes = new byte[2 + messageBytes.length];
		payloadBytes[0] = (byte) ((code & 0xFF00) >> Byte.SIZE);
		payloadBytes[1] = (byte) (code & 0xFF);
		System.arraycopy(messageBytes, 0, payloadBytes, 2, messageBytes.length);
		sendClose(payloadBytes);
	}

	protected final void sendClose(byte[] message) throws IOException {
		sendMessage(CLOSE, message);
	}

	protected final void sendMessage(int opcode, byte[] message) throws IOException {
		sendFrame(true, opcode, message);
	}

	protected final void sendFrame(boolean fin, int opcode, byte[] message) throws IOException {
		sendFrame(fin, opcode, message, 0, message.length);
	}

	protected final void sendFrame(boolean fin, int opcode, byte[] message, int off, int len)
			throws IOException {
		OutputStream out = getOutputStream();
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

	private void writeAsBytes(OutputStream out, long bytes, int byteCount) throws IOException {
		for (int i = 0; i < byteCount; i++) {
			out.write((int) (bytes >> (Byte.SIZE * (byteCount - i - 1))));
		}
	}

}
