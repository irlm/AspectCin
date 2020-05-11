package projeto.test;

import java.io.IOException;

import projeto.anORB.AnORB;
import projeto.anORB.namingservice.NamingService;

public class HelloServer {
	public static void main(String[] args) throws IOException {
		AnORB anORB = AnORB.init(null);
		NamingService ns = anORB.getNamingService();
		ns.register("Hello", new HelloImpl());
	}
}
