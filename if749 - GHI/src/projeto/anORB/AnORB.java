package projeto.anORB;

import java.io.IOException;

import projeto.anORB.comunication.Receiver;
import projeto.anORB.namingservice.NamingService;
import projeto.anORB.namingservice.NamingServiceStub;

public class AnORB {

	private static AnORB singleton;

	private String namingAdress; // Ip do serviço de nomes	
	private Receiver receiver;
	private Dispatcher dispatcher;

	public AnORB(String namingAdress) throws IOException {
		this.namingAdress = namingAdress;		
		this.dispatcher = Dispatcher.getSingleton();
		this.receiver = new Receiver(2178, dispatcher);
		this.receiver.start();
	}

	public static AnORB init(String namingAdress) throws IOException {
		if (singleton == null) {
			synchronized (AnORB.class) {
				if (singleton == null) {
					singleton = new AnORB(namingAdress);					
				}
			}
		}
		return singleton;
	}

	public NamingService getNamingService() throws IOException {
		return new NamingServiceStub(namingAdress);
	}
}
