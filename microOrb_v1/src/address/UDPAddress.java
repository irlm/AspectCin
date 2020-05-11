package address;
import transport.UDPTransport;


public class UDPAddress extends Address {	
		
	private static int PORTA_UDP = 2224;
	private static String HOSTNAME_UDP = "172.17.95.12";
	
	public UDPAddress() {
		this.hostname = HOSTNAME_UDP;			
		this.port = PORTA_UDP;			
	}
	
	public UDPTransport getInstance() {		
		return new UDPTransport(hostname, port);
	}	

}