package aspectcin.orb;

import java.io.IOException;

import aspectcin.namingservice.NamingService;
import aspectcin.namingservice.NamingServiceStub;

public class CInORB{

	private static CInORB singleton;
	
	private int port;
	
	private NamingServiceStub stub;

	private CInORB(int port, String namingAddress, int namingPort)
			throws IOException {

		this.stub = new NamingServiceStub(namingAddress, namingPort);
				
		this.port = port;
	}

	public static CInORB init(int port, String namingAddress, int namingPort)
			throws IOException {
		if (singleton == null) {
			synchronized (CInORB.class) {
				if (singleton == null) {
					singleton = new CInORB(port, namingAddress, namingPort);
				}
			}
		}
		return singleton;
	}
	
	public static int getLocalPort() {
		int result = 0;

		if (singleton != null) {
			result = singleton.port;
		}

		return result;
	}

	public NamingService getNamingService() throws IOException {		
		return stub;
	}
	
}
