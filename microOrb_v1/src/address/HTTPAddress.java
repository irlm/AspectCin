package address;
import java.net.*;

import transport.HTTPTransport;

public class HTTPAddress extends Address {	
	
	
	public HTTPAddress() {
		try {
			hostname = InetAddress.getLocalHost().getHostName();
			port = this.returnPortFree();			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public HTTPTransport getInstance() {
		return new HTTPTransport(hostname, port);
	}
	
	private int returnPortFree() {
		return 80;
	}
	

}
