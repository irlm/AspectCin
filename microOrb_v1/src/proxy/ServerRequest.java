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
	
	/* Método que recebe uma requisição do cliente */
	public Object[] receiveRequest() {
		Object[] request = (Object[])pdu.receiveRequest();
		this.operation = (String)request[0];
		this.parameters = (Object[])request[1];
		this.object_id = (byte[])request[2];
		
		return request;
	}
	
	/* Método que faz a chamada para o método reply do PDU 
	   e passa o identificador do objetReference
	*/ 
	public void sendReply(Object response) {
		int idRequest = this.getIdRequest();
		pdu.sendReply(response, idRequest);
	}
	
	
	/* Método que faz a chamada para o método reply do PDU 
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
		
	/* Método que gera o identificador da requisição */
	private int getIdRequest() {
		return countRequest+1;
	}
	
	
}