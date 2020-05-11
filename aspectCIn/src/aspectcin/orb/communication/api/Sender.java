package aspectcin.orb.communication.api;

import java.io.IOException;
import java.net.InetAddress;

import aspectcin.orb.CInORB;
import aspectcin.orb.RemoteObject;
import aspectcin.orb.communication.impl.Connection;
import aspectcin.orb.communication.impl.ConnectionPool;
import aspectcin.orb.communication.impl.PackageBody;

public class Sender {

	private ConnectionPool pool;
	
	private static Sender singleton;

	private Sender(){
		this.pool = new ConnectionPool();
	}
	
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
	
	public void send(RemoteObject target, PackageBody body) throws IOException {

		AnPackage anPackage = new AnPackage(InetAddress.getLocalHost(), CInORB.getLocalPort(), InetAddress.getByName(target.getHost()), target.getPort());
		anPackage.setBody(body);
		
		Connection sender = pool.getConnection(anPackage.getDestiny(), anPackage
				.getDestinyPort());

		if (sender != null) {
			sender.send(anPackage);
		} else {
			throw new IOException("Não foi possível enviar o pacote");
		}
	}
}
