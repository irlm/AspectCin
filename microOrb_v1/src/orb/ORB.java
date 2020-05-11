package orb;

import java.util.Properties;

import namingService.cosNaming.NamingContext;
import namingService.cosNaming.NamingContext_Stub;
import protocol.PDU;
import proxy.ObjectImpl;
import proxy.ObjectProxy;
import proxy.ServerRequest;
import transport.Transport;
import address.Address;

public class ORB {

	//private ServerNames serverNames;
	private ManagerObjectReference managerObjectReference;
	private String type;
	private PDU pdu;
	private Transport transport;
	
	public ORB(String type) {
		this.type = type;
	}
	
	public void init() {	
		
//		Busca uma instância do transporte
		this.transport =  Address.getConnection("tcp");
		
		//Recebe uma mensagem
		this.pdu = new PDU(transport);
		
		if("Client".equalsIgnoreCase(type)) {
			ObjectProxy objectProxy = new ObjectProxy(pdu);
		} else if("Server".equalsIgnoreCase(type)) {
			this.managerObjectReference = new ManagerObjectReference();
		}
	}
	
	 
	public NamingContext getInitialNamingContext(Properties p) {
		this.transport.setHost(p.getProperty("host"));
		this.transport.setPort(Integer.parseInt(p.getProperty("port")));
		NamingContext namingContext = new NamingContext_Stub(this.transport);
		return namingContext;
	}	
	
	public ManagerObjectReference getManagerObjectReference() {
		return this.managerObjectReference;
	}
	
	public void run () {

		while (true) {	
			
			ServerRequest serverRequest = new ServerRequest(pdu);
			
			serverRequest.receiveRequest();
			
			ObjectImpl obj = (ObjectImpl)this.managerObjectReference.getObjectReferenceServer(serverRequest.getObjectKey());
			
			Object response = obj.invoke(serverRequest);	
			
			serverRequest.sendReply(response);
		} 			
		
	}
	
}
