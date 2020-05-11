package aspectcin.orb;

import java.io.IOException;

import aspectcin.util.Configuration;
import aspectcin.orb.communication.api.Receiver;

public aspect CinORBCommAspect {

	private static Receiver CInORB.receiver;
	
	pointcut startReceiver(CInORB cinORB, int port, String NsHost, int NsPort): 
		(execution(aspectcin.orb.CInORB.new(int, String, int)) &&
		args(port, NsHost, NsPort) )&&
		target(cinORB);
	
	after(CInORB cinORB, int port, String NsHost, int NsPort) throws IOException:
		startReceiver(cinORB, port, NsHost, NsPort){
			if (Configuration.getInstance().connectionType().equalsIgnoreCase(
					"TCP")) {
				CInORB.receiver = new aspectcin.orb.communication.impl.tcp.TCPReceiver(
						port);
			} else if (Configuration.getInstance().connectionType()
					.equalsIgnoreCase("UDP")) {
				CInORB.receiver = new aspectcin.orb.communication.impl.udp.UDPReceiver(
						port);
			} else {
				throw new IOException("Error - Connection Type");
			}
			
			CInORB.receiver.start();
		}
}
