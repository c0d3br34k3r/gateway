package com.catascopic.gateway;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Test {

	public static void main(String[] args) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.put("whattttt".getBytes());
	}

}
