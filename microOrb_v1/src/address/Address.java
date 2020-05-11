package address;

import transport.Transport;

public abstract class Address {
	
	public static String hostname;
	public static int port;
	
	public static Transport getConnection(String transport) {
		if ("TCP".equalsIgnoreCase(transport)) {
			return new TCPAddress().getInstance();
		} else if ("UDP".equalsIgnoreCase(transport)) {
			return new UDPAddress().getInstance();
		} else if ("HTTP".equalsIgnoreCase(transport)) {
			return new HTTPAddress().getInstance();
		}
		else 
			return null;
	}	

}
