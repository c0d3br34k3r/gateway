package server;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.common.net.UrlEscapers;

public class ContentTest {

	public static void main(String[] args) throws IOException {
		
		System.out.println(UrlEscapers.urlFormParameterEscaper().escape("a"));
		String lines =
				"line1\r\nline2\r\nline3\r\n\r\n4\r\nWiki\r\n5\r\npedia\r\nE\r\n in\r\n\r\nchunks.\r\n0\r\n\r\n";
		BufferedInputStream stream = new BufferedInputStream(new ByteArrayInputStream(lines.getBytes()));

		HttpInput http = new HttpInput(stream);

		for (;;) {
			String line = http.readLine();
			if (line.isEmpty()) {
				System.out.println("END");
				break;
			}
			System.out.println("LINE: " + line);
		}

		System.out.println();
		System.out.println(new String(http.readChunked(), StandardCharsets.UTF_8));
		System.out.println(stream.read());
	}

}