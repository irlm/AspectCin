package proxy;

import ior.IOR;
import protocol.PDU;

public class Request {
	
	private PDU pdu; 
	private int countRequest = 0;
	
	public Request(PDU pdu) {
		this.pdu = pdu;
	}
		
	/* Método que faz a chamada para o método request do PDU 
	   e passa o identificador do objetReference
	*/ 
	public void sendRequest(IOR objectReference, String operation, Object[] parameters) {
		int idRequest = this.getIdRequest();
		pdu.sendRequest(operation, parameters, objectReference, idRequest);
	}
	
	/* Método que recebe uma resposta do servidor */
	public Object receiveReply() {
		return pdu.receiveReply();
	}
	
	/* Método que gera o identificador da requisição */
	private int getIdRequest() {
		return countRequest+1;
	}
	
}
