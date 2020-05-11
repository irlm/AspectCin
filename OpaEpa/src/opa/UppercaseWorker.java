package opa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

public class UppercaseWorker implements Runnable {
	private Socket _requestSocket;

	public UppercaseWorker(Socket requestSocket) throws IOException {
		System.out.println("Creating new worker");
		_requestSocket = requestSocket;
	}

	public void run() {
		BufferedReader requestReader = null;
		Writer responseWriter = null;
		try {
			requestReader = new BufferedReader(new InputStreamReader(
					_requestSocket.getInputStream()));
			responseWriter = new OutputStreamWriter(_requestSocket
					.getOutputStream());
			while (true) {
				String requestString = requestReader.readLine();
				if (requestString == null) {
					break;
				}
				System.out.println("Got request: " + requestString);
				responseWriter.write(requestString.toUpperCase() + "\n");
				responseWriter.flush();
			}
		} catch (IOException ex) {
		} finally {
			try {
				if (responseWriter != null) {
					responseWriter.close();
				}
				if (requestReader != null) {
					requestReader.close();
				}
				_requestSocket.close();
			} catch (IOException ex2) {
			}
		}
		System.out.println("Ending the session");
	}
}
