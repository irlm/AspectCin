package hello;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import aspectcin.namingservice.NamingService;
import aspectcin.orb.CInORB;

public class HelloClient {

	public static void main(String[] args) throws IOException {

		System.out.println("Cliente - Porta 2198");

		CInORB cinORB = CInORB.init(2198, "localhost", 2178);

		NamingService ns = cinORB.getNamingService();

		Hello hello = (Hello) ns.lookup("Hello");

		Primitive primitive = new Primitive(Byte.MAX_VALUE, Short.MAX_VALUE,
				Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE,
				Double.MAX_VALUE, true, Character.MAX_VALUE);
		
		long start = 0;
		long finished = 0;
		
		//FileWriter fstream = new FileWriter("out.txt");
		//BufferedWriter out = new BufferedWriter(fstream);
		//for (int i = 0; i < 11500; i++) {

			try {
			//	start = System.nanoTime();
				hello.primitive(primitive);

				System.out.println(hello.sayHelloWithEcho(" Opa Epa"));
			//	finished = System.nanoTime();
			} catch (ParametroNuloException e) {
				e.printStackTrace();
			}

			///out.write((i + 1) + "\t" + (finished - start) + "\n");
		//}
		//out.close();
		 
		System.out.println("Fim");
	}
}
