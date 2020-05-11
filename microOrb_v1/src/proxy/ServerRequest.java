package proxy;

import protocol.PDU;

public class ServerRequest {

	private PDU pdu; 
	private int countRequest = 0;
	private byte[] object_id;
	private String operation;
	private Object[] parameters;
	
	public ServerRequest(PDU pdu) {
		this.pdu = pdu;
	}
	
	/* M�todo que recebe uma requisi��o do cliente */
	public Object[] receiveRequest() {
		Object[] request = (Object[])pdu.receiveRequest();
		this.operation = (String)request[0];
		this.parameters = (Object[])request[1];
		this.object_id = (byte[])request[2];
		
		return request;
	}
	
	/* M�todo que faz a chamada para o m�todo reply do PDU 
	   e passa o identificador do objetReference
	*/ 
	public void sendReply(Object response) {
		int idRequest = this.getIdRequest();
		pdu.sendReply(response, idRequest);
	}
	
	
	/* M�todo que faz a chamada para o m�todo reply do PDU 
	   e passa o identificador do objetReference
	*/ 
	public byte[] getObjectKey() {
		return this.object_id;
	}
	
	public String getOperation() {
		return this.operation;
	}
	
	public Object[] getParameters() {
		return this.parameters;
	}
		
	/* M�todo que gera o identificador da requisi��o */
	private int getIdRequest() {
		return countRequest+1;
	}
	
	
}