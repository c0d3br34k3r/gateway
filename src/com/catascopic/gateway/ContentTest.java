package com.catascopic.gateway;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.google.common.io.ByteStreams;

public class ContentTest {

	public static void main(String[] args) throws IOException {
		String lines =
				"line1\r\nline2\r\nline3\r\n\r\n4\r\nWiki\r\n5\r\npedia\r\nE\r\n in\r\n\r\nchunks.\r\n0\r\n\r\n";
		BufferedInputStream stream = new BufferedInputStream(new ByteArrayInputStream(lines.getBytes()));

		HttpReader http = new HttpReader(stream);

		for (;;) {
			String line = http.readLine();
			if (line.isEmpty()) {
				System.out.println("END");
				break;
			}
			System.out.println("LINE: " + line);
		}

		System.out.println(new String(ByteStreams.toByteArray(http.streamChunked())));
		System.out.println(stream.read());
	}

}
