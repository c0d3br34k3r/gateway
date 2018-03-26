package com.catascopic.gateway.websocket;

public interface WebsocketListener {

	void onOpen(Websocket websocket);

	void onBinary(byte[] bytes);

	void onText(String text);

	void onClose(int code, String message);

	void onError(Throwable error);

	void onPing(byte[] payload);

	void onPong(byte[] payload);

}
