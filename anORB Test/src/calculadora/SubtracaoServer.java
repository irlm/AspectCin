package calculadora;

import java.io.IOException;

import anorb.AnORB;
import anorb.namingservice.NamingService;

public class SubtracaoServer {

	public static void main(String[] args) throws IOException {
		
		System.out.println("Server Subtracao- Porta 2169");
		
		AnORB anORB = AnORB.init(2169, args[0], 2178);
		
		NamingService ns = anORB.getNamingService();
		
		SubtracaoImpl subtracao = new SubtracaoImpl();
		
		ns.register("Subtracao", subtracao);
	}
}
