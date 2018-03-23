package com.catascopic.gateway.websocket;

import java.io.IOException;

public class WebsocketException extends IOException {

	private static final long serialVersionUID = 1L;

	public WebsocketException() {
		super();
	}

	public WebsocketException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebsocketException(String message) {
		super(message);
	}

	public WebsocketException(Throwable cause) {
		super(cause);
	}

}
