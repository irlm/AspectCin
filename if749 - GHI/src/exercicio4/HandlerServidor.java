package exercicio4;

import java.io.IOException;

public class HandlerServidor extends ServiceHandler {

	public HandlerServidor() {
	}

	public int open(Object obj) {
		new Thread(this).start();
		return 0;
	}

	public void run() {
		int msg_len;
		System.out.println("Esperando por mensagens...");
		try {
			while (true) {
				StringBuffer msg = new StringBuffer();
				msg_len = this.peer().recv(msg);
				if (msg_len == 0)
					break;
				System.out.println("recebi: " + msg);
				this.peer().send(new StringBuffer("o servidor recebeu"));
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
