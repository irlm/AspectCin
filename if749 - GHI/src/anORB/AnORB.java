package anORB;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class AnORB {

	private static final String[] argsPadroes = {"192.168.0.21","1050"};

	private static AnORB singleton;

	private InetAddress ip;

	private int port;

	public static AnORB init(String[] args) throws IOException {
		if (singleton == null) {
			synchronized (AnORB.class) {
				if (singleton == null) {
					singleton = new AnORB(checkArgs(args));
				}
			}
		}
		return singleton;
	}

	private static final String[] checkArgs(String[] valores) {
		if(valores == null)
			return argsPadroes;
		String[] ret = new String[argsPadroes.length];
		for (int i = 0; i < ret.length; i++) {
			if (valores.length > i && valores[i] != null) {
				ret[i] = valores[i];
			} else {
				ret[i] = argsPadroes[i];
			}
		}
		return ret;
	}

	public AnORB(String[] args) throws IOException {
		ip = InetAddress.getByName(args[0]);
		port = Integer.parseInt(args[1]);
		
		Socket s = new Socket(ip, port);
		
		IIOPRequest1_0 request = new IIOPRequest1_0(5, "get");
		request.setKey("INIT");
		IIOPPackage pckg = new IIOPPackage(request);
		
		s.getOutputStream().write(pckg.toBytes());
		
		s.close();
		
		System.out.println("Fim");
	}

	public NamingContext getNamingContext() throws UnknownHostException {
		return new NamingContext(ip, port);
	}

}
