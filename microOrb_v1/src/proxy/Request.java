package proxy;

import ior.IOR;
import protocol.PDU;

public class Request {
	
	private PDU pdu; 
	private int countRequest = 0;
	
	public Request(PDU pdu) {
		this.pdu = pdu;
	}
		
	/* M�todo que faz a chamada para o m�todo request do PDU 
	   e passa o identificador do objetReference
	*/ 
	public void sendRequest(IOR objectReference, String operation, Object[] parameters) {
		int idRequest = this.getIdRequest();
		pdu.sendRequest(operation, parameters, objectReference, idRequest);
	}
	
	/* M�todo que recebe uma resposta do servidor */
	public Object receiveReply() {
		return pdu.receiveReply();
	}
	
	/* M�todo que gera o identificador da requisi��o */
	private int getIdRequest() {
		return countRequest+1;
	}
	
}
