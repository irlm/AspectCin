package calculadora;

import java.io.IOException;

import anorb.AnORB;
import anorb.namingservice.NamingService;

public class DivisaoServer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Server Divisao - Porta 2171");
		
		AnORB anORB = AnORB.init(2171,args[0], 2178);
		
		NamingService ns = anORB.getNamingService();
		
		DivisaoImpl impl = new DivisaoImpl();
		
		ns.register("Divisao", impl);

	}

}
