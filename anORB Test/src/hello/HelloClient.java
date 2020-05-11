package hello;

import java.io.IOException;

import anorb.AnORB;
import anorb.logging.AnLog;
import anorb.namingservice.NamingService;

public class HelloClient {

	public static void main(String[] args) throws IOException {
		AnLog.monitorAll();
		
		System.out.println("Cliente - Porta 2198");
		
		AnORB anORB = AnORB.init(2198, "g2c37", 2178);
		
		NamingService ns = anORB.getNamingService();
		
		System.out.println("Listing services...");
		String[] services = ns.list();
		for(String service : services){
			System.out.println(service);
		}
		
		//Hello hello1 = (Hello) ns.lookup("Hello1");
		Hello hello3 = (Hello) ns.lookup("Hello");
		
		//hello1.sayHello();	
		//hello3.sayHelloWithParameter("Epa");
		try {
			System.out.println(hello3.sayHelloWithEcho(null));		
		} catch (ParametroNuloException e) {
			e.printStackTrace();
		}		
		System.out.println("Fim");
	}
}
