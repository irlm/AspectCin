package aspectcin.namingservice;

import java.io.IOException;

import aspectcin.orb.communication.api.Receiver; 
import aspectcin.util.Configuration;

public aspect NamingServiceCommAspect {

	private static Receiver NamingServiceMain.receiver;
	
	pointcut startReceiver(NamingServiceMain namingServiceMain): 
		call(void NamingServiceMain.Starting()) &&
		target(namingServiceMain);
	
	after(NamingServiceMain namingServiceMain) throws IOException:
		startReceiver(namingServiceMain){
			if (Configuration.getInstance().connectionType().equalsIgnoreCase(
					"TCP")) {
				NamingServiceMain.receiver = new aspectcin.orb.communication.impl.tcp.TCPReceiver(
						namingServiceMain.getPort());
			} else if (Configuration.getInstance().connectionType()
					.equalsIgnoreCase("UDP")) {
				NamingServiceMain.receiver = new aspectcin.orb.communication.impl.udp.UDPReceiver(
						namingServiceMain.getPort());
			} else {
				throw new IOException("Error - Connection Type");
			}
			
			NamingServiceMain.receiver.start();
		}
}
