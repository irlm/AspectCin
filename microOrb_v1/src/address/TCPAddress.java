package address;
import transport.TCPTransport;


public class TCPAddress extends Address {	
	
	private static int PORTA_TCP = 2222;
	private static String HOSTNAME_TCP = "172.17.95.12";
	
	public TCPAddress() {
		this.hostname = HOSTNAME_TCP;			
		this.port = PORTA_TCP;			
	}
	
	public TCPTransport getInstance() {		
		return new TCPTransport(hostname, port);
	}	


}