package hello;

import java.io.IOException;

import aspectcin.CInORB;
import aspectcin.namingservice.NamingService;

public class HelloServer {
	public static void main(String[] args) throws IOException {

		System.out.println("Server - Porta 2168");
		CInORB cinORB = CInORB.init(2168, "localhost", 2178);

		NamingService ns = cinORB.getNamingService();

		HelloImpl hello = new HelloImpl();
		
		HelloImpl hello2 = new HelloImpl();

		ns.register("Hello", hello);
		ns.register("Hello2", hello2);

	}
}
