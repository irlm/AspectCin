package hello;

import java.io.IOException;

import aspectcin.CInORB;
import aspectcin.namingservice.NamingService;

public class HelloClient {

	public static void main(String[] args) throws IOException {

		
		System.out.println("Cliente - Porta 2198");
		
		CInORB cinORB = CInORB.init(2198, "localhost", 2178);
		
		NamingService ns = cinORB.getNamingService();
		
		/*System.out.println("Listing services...");
		String[] services = ns.list();
		for(String service : services){
			System.out.println(service);
		}*/
		
		Hello hello = (Hello) ns.lookup("Hello");
		Hello hello2 = (Hello) ns.lookup("Hello2");

		try {
			System.out.println("hello.sayHello()");
			hello.sayHello();
			System.out.println("hello.sayHelloWithParameter(\"Opa, quem disse epa?\")");
			hello2.sayHelloWithParameter("Opa, quem disse epa?");
			
			
			for (int i = 0; i < 10; i++) {
				System.out.println(hello.sayHelloWithEcho("Epa i=" + i));
			}

		} catch (ParametroNuloException e) {
			e.printStackTrace();
		}		
		System.out.println("Fim");
	}
}
