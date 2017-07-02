package gateway.websocket;

import java.io.IOException;

public class WebsocketProtocolException extends IOException {

	private static final long serialVersionUID = 1L;

	public WebsocketProtocolException() {
		super();
	}

	public WebsocketProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebsocketProtocolException(String message) {
		super(message);
	}

	public WebsocketProtocolException(Throwable cause) {
		super(cause);
	}

}
