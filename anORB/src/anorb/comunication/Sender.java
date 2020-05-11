package anorb.comunication;

import java.io.IOException;
import java.util.logging.Logger;

import anorb.logging.AnLog;

public class Sender {

	private static Sender singleton;

	public static Sender getSingleton() {
		if (singleton == null) {
			synchronized (Sender.class) {
				if (singleton == null) {
					singleton = new Sender();
				}
			}
		}
		return singleton;
	}
	
	private ConnectionPool pool;
	private int pkgCount;
	private Logger log = AnLog.comunication;
	
	private Sender(){
		this.pool = new ConnectionPool();
	}
	
	public void send(AnPackage pkg) throws IOException {

		Connection sender = pool.getConnection(pkg.getDestiny(), pkg
				.getDestinyPort());

		if (sender != null) {
			sender.send(pkg);

			log.fine(this.toString() + " - PKG " + pkgCount++ + " SENT TO "
					+ pkg.getDestiny() + "(" + pkg.getDestinyPort() + ")");
		} else {
			throw new IOException("Não foi possível enviar o pacote");
		}
	}
}
