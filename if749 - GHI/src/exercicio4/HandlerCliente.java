package exercicio4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HandlerCliente extends ServiceHandler {
	public HandlerCliente() {
	}

	public int open(Object obj) {
		new Thread(this).start();
		return 0;
	}

	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		String msg;
		StringBuffer ack = new StringBuffer();
		int ack_len;
		try {
			while (true) {
				
				System.out.print("Digite qualquer coisa: ");
				System.out.flush();
				
				msg = in.readLine();
				
				if (msg == null)
					break;
				
				this.peer().send(new StringBuffer(msg));
				
				System.out.println("Waiting for ack...");
				ack_len = this.peer().recv(ack);
				if (ack_len == 0)
					break;
				else
					System.out.println(ack);
			}
		} catch (NullPointerException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				this.peer().close();
			} catch (IOException e) {
			}
		}

	}
}
