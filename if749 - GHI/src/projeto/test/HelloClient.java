package projeto.test;

import java.io.IOException;

import projeto.anORB.AnORB;
import projeto.anORB.namingservice.NamingService;

public class HelloClient {

	public static void main(String[] args) throws IOException {
		AnORB anORB = AnORB.init(null);
		NamingService ns = anORB.getNamingService();
		
		Hello hello = (Hello) ns.lookup("Hello");
		
		hello.sayHello();
	}
}
