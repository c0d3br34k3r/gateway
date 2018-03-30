package com.catascopic.gateway.websocket;

public abstract class WebsocketListener {

	protected void onOpen(Websocket websocket) {}

	protected void onBinary(byte[] bytes) {}

	protected void onText(String text) {}

	protected void onClose(int code, String message) {}

	protected void onError(Throwable error) {}

	protected void onPing(byte[] payload) {}

	protected void onPong(byte[] payload) {}

}
