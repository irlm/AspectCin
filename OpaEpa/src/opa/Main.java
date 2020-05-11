package opa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("localhost", 2222);
			BufferedReader requestReader = null;
			Writer responseWriter = null;
			try {
				requestReader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				responseWriter = new OutputStreamWriter(socket
						.getOutputStream());
				while (true) {
					String requestString = requestReader.readLine();
					if (requestString == null) {
						break;
					}
					System.out.println("send: " + requestString);
					responseWriter.write(requestString.toLowerCase() + "\n");
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
					socket.close();
				} catch (IOException ex2) {
				}
			}
			System.out.println("Ending the session");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
