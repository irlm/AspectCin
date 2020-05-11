package calculadora;

import java.io.IOException;

import anorb.AnORB;
import anorb.namingservice.NamingService;

public class SomaServer {

	public static void main(String[] args) throws IOException {

		System.out.println("Server Soma - Porta 2168");
		AnORB anORB = AnORB.init(2168, args[0], 2178);

		NamingService ns = anORB.getNamingService();

		SomaImpl soma = new SomaImpl();

		ns.register("Soma", soma);

	}
}
