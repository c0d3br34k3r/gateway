package server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) throws IOException {
		new Thread(new Runnable() {

			@Override public void run() {
				ServerSocket server;
				try {
					server = new ServerSocket(3637);
				} catch (IOException e) {
					return;
				}
				for(;;) {
					try {
						final Socket client = server.accept();
						new Thread(new Runnable() {

							@Override public void run() {
								try {
									client.close();
								} catch (IOException e) {
									
								}
							}
							
						}).start();
					} catch (IOException e) {
						// continue
					}
				}
			}
		}).start();
		
		InputStream inputStream = new Socket("127.0.0.1", 3637).getInputStream();
		for (;;) {
			
		}
	}

}
