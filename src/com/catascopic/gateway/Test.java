package com.catascopic.gateway;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Test {

	public static void main(String[] args) throws IOException {
		System.out.println(HttpDateTimeFormat.hashMd5(new ByteArrayInputStream("ab4".getBytes())));
	}

}
