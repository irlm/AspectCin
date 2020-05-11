package anorb;

import java.io.IOException;

import anorb.comunication.Receiver;
import anorb.namingservice.NamingService;
import anorb.namingservice.NamingServiceStub;

public class AnORB {

	private static AnORB singleton;
	
	private NamingServiceStub stub;
	private Receiver receiver;
	private Dispatcher dispatcher;

	public AnORB(int port, String namingAddress, int namingPort)
			throws IOException {

		this.dispatcher = Dispatcher.getSingleton();
		this.stub = new NamingServiceStub(namingAddress, namingPort);
		stub.init();
		this.receiver = new Receiver(port, dispatcher);
		this.receiver.start();
	}

	public static AnORB init(int port, String namingAddress, int namingPort)
			throws IOException {
		if (singleton == null) {
			synchronized (AnORB.class) {
				if (singleton == null) {
					singleton = new AnORB(port, namingAddress, namingPort);
				}
			}
		}
		return singleton;
	}

	public static int getLocalPort() {
		int result = 0;

		if (singleton != null) {
			result = singleton.receiver.getLocalPort();
		}

		return result;
	}

	public NamingService getNamingService() throws IOException {
		
		return stub;
	}

	public static void destroy() {
		if (singleton != null) {
			synchronized (AnORB.class) {
				if (singleton != null) {
					singleton.receiver.stopRunning();
					singleton = null; // Let GC do it work
				}
			}
		}
	}
}
