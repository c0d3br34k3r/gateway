package gateway;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Stopwatch;

public class TimeTest {

	public static void main(String[] args) throws IOException {
		Stopwatch stopwatch = Stopwatch.createStarted();
		for (int i = 0; i < 10000; i++) {
			try (InputStream in = new FileInputStream("lines.txt")) {
				HttpReader http = new HttpReader(in);
				String line;
				do {
					line = http.readLine();
				} while (!line.isEmpty());
			}
		}
		System.out.println(stopwatch);
	}

}
