package gateway.websocket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import gateway.CharsetOutputStream;

public abstract class EventWebsocket extends AbstractWebsocket {

	private CharsetOutputStream currentMessage = new CharsetOutputStream();
	private boolean inProgress; // = false
	private boolean messageIsText;

	private boolean receivedClose; // = false
	private boolean sentClose; // = false

	/**
	 * This method is called when a message containing text is received.
	 * 
	 * @param text the text content of the message
	 */
	protected void onMessage(String text) {}

	/**
	 * This method is called when a message containing binary data is received.
	 * 
	 * @param bytes the binary content of the message
	 */
	protected void onMessage(byte[] bytes) {}

	/**
	 * This method is called when a close message is received.
	 * 
	 * @param code the close code, or 1005 if no close code was present
	 * @param message the close message, or the empty String if no close message
	 *        was present
	 */
	protected void onClose(int code, String message) {}

	@Override protected final void handleFrame(boolean fin, int opcode, byte[] payload)
			throws IOException {
		if (!inProgress && opcode == CONTINUATION) {
			throw new WebsocketProtocolException("unstarted continuation");
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
				handleFinalContinuation(payload);
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
			case PONG:
				// do nothing
				break;
			case CLOSE:
				handleClose(payload);
				break;
			default:
				throw new WebsocketProtocolException("unknown opcode: " + opcode);
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
				throw new WebsocketProtocolException(
						"opcode " + opcode + " must have fin set or is unknown");
		}
		inProgress = true;
		currentMessage.write(payload);
	}

	private void beginMessage(boolean isText) throws IOException {
		if (inProgress) {
			throw new WebsocketProtocolException("message in progress");
		}
		this.messageIsText = isText;
	}

	private void handleFinalContinuation(byte[] payload) throws IOException {
		currentMessage.write(payload);
		if (messageIsText) {
			onMessage(currentMessage.toString());
		} else {
			onMessage(currentMessage.toByteArray());
		}
		currentMessage.reset();
	}

	private void handleClose(byte[] payload) throws IOException {
		receivedClose = true;
		int code;
		String message;
		if (payload.length >= 2) {
			code = ((payload[0] & 0xFF) << Byte.SIZE) | (payload[1] & 0xFF);
			message = new String(payload, 2, payload.length - 2, StandardCharsets.UTF_8);
		} else {
			code = 1005;
			message = "";
		}

		synchronized (this) {
			if (!sentClose) {
				if (code == 1005) {
					sendClose();
				} else {
					sendClose(code);
				}
			}
		}

		onClose(code, message);
	}

	private void handlePing(byte[] payload) throws IOException {
		sendMessage(PONG, payload);
	}

}
