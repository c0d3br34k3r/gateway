package com.catascopic.gateway;

import java.io.IOException;
import java.io.Reader;

public class HttpRequestParser {

	HttpRequestParser(Reader reader) {
		this.method = readUntil(reader, ' ');
		this.requestUri = parseRequestUri(reader);
		this.version = readToLineEnd(reader);
	}

	String readUntil(Reader reader, char end) throws IOException {
		StringBuilder builder = new StringBuilder();
		for (;;) {
			int ch = reader.read();
			if (ch == end) {
				return builder.toString();
			}
			builder.append((char) ch);
		}
	}

	String readToLineEnd(Reader reader) throws IOException {
		StringBuilder builder = new StringBuilder();
		for (;;) {
			int ch = reader.read();
			switch (ch) {
			case -1:
				throw new IOException();
			case '\r':
				if (reader.read() != '\n') {
					throw new IOException();
				}
				return builder.toString();
			default:
				builder.append((char) ch);
			}
		}
	}

}
