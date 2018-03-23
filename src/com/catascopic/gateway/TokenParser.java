package com.catascopic.gateway;

public class TokenParser {

	private String queue;
	private int pos = 0;

	public TokenParser(String queue) {
		this.queue = queue;
	}

	public String getNext(char separator) {
		int index = queue.indexOf(separator, pos);
		if (index != -1) {
			String part = queue.substring(pos, index);
			pos = index + 1;
			return part;
		} else {
			return remainder();
		}
	}

	public String getNext(String separator) {
		int index = queue.indexOf(separator, pos);
		if (index != -1) {
			String part = queue.substring(pos, index);
			pos = index + separator.length();
			return part;
		} else {
			return remainder();
		}
	}

	public String remainder() {
		final String remainder = queue.substring(pos, queue.length());
		pos = queue.length();
		return remainder;
	}

	public boolean hasNext() {
		return pos < queue.length();
	}

}
