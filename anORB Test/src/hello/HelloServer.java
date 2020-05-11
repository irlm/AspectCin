package hello;

import java.io.IOException;

import anorb.AnORB;
import anorb.logging.AnLog;
import anorb.namingservice.NamingService;

public class HelloServer {
	public static void main(String[] args) throws IOException {
		AnLog.monitorFine();
		System.out.println("Server - Porta 2168");
		AnORB anORB = AnORB.init(2168, "g2c37", 2178);

		NamingService ns = anORB.getNamingService();

		HelloImpl hello = new HelloImpl();

		ns.register("Hello", hello);

	}
}
