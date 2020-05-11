package anorb.namingservice;

import java.io.IOException;
import java.util.logging.Logger;

import anorb.Dispatcher;
import anorb.comunication.Receiver;
import anorb.logging.AnLog;

public class Main {

	public static void main(String[] args) throws IOException{
		AnLog.monitorAll();
		Logger log = AnLog.naming;
		log.info("Starting NamingService...");
		NamingServiceImpl impl = new NamingServiceImpl();
		Dispatcher dispatcher = Dispatcher.getSingleton();
		dispatcher.registerRemoteObject("NamingService", impl);
		Receiver receiver = new Receiver(2178, dispatcher);
		receiver.start();
		log.info("NamingService started!");
	}
}
