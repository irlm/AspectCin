package proxy;

import protocol.PDU;

public class ObjectProxy {

	private PDU pdu;
	public static Request request;
	
	public ObjectProxy() {
	}
	
	public ObjectProxy(PDU pdu) {
		this.pdu = pdu;
		if (request == null) {
			request = new Request(this.pdu);
		}
	}
		
	public Request getInstanceRequest() {		
		return request;
	}

	
}
  