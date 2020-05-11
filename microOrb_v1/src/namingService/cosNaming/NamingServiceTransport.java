package namingService.cosNaming;

import java.io.*;

import protocol.*;
import transport.Transport;

public class NamingServiceTransport {
	
	private Transport transport;
	
	public NamingServiceTransport(Transport transport) {
		 this.transport = transport;
	}
	
	public void sendRequest(String operation, Object[] parameters) {
		
		/*Debug
		  System.out.println("NamingServiceTransport: cria mensagem! ");
		  System.out.println("NamingServiceTransport: sendRequest::operation: " + operation);
		  System.out.println("NamingServiceTransport: sendRequest::this.transport.getHostLocal(): " + this.transport.getHostLocal());
		  System.out.println("NamingServiceTransport: sendRequest::this.transport.getPortLocal(): " + this.transport.getPortLocal());
		 */
				
		/*
		 * Envia o host e porta do cliente em requisição 
		 * para enviar resposta
		 * */
		parameters[2] = this.transport.getHostLocal();
		parameters[3] = this.transport.getPortLocal();
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); 
		try {
	    	ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(byteStream)); 
	    	oos.flush();  
	    	oos.writeObject(parameters);  
	    	oos.flush(); 
		} catch(IOException io) {			
		}
		byte[] requestBody = byteStream.toByteArray();
		
		//Cria o cabeçalho da mensagem
		byte major = 1;
		byte minor = 0;
		Version version = new Version(major, minor);
		char[] magic = "GIOP".toCharArray();
		boolean byte_order = true;
		byte message_type = 0;
		int message_size = requestBody.length;		
		MessageHeader header = new MessageHeader(
										magic, 
										version,
										byte_order,
										message_type,
										message_size									
								);
		
		//Cria o corpo da mensagem
		ServiceContextList serviceContextList = new ServiceContextList();
		int request_id = -100;
		boolean response_expected = true;
		byte[] object_id = {123};
		String operationH = operation;
		Principal requesting_principal = new Principal();		
		RequestHeader requestHeader = new RequestHeader(serviceContextList, 
								request_id, 
								response_expected, 
								object_id, 
								operationH, 
								requesting_principal);
		


//		Cria uma mensagem a ser enviada
		Message messageRequest = new MessageRequest();
		((MessageRequest)messageRequest).setHeader(header);
		((MessageRequest)messageRequest).setRequestHeader(requestHeader);
		((MessageRequest)messageRequest).setRequestBody(requestBody);
		
		/*Debug
		  System.out.println("NamingServiceTransport: Request! ");
		 */
		
		//Envia uma mensagem
		this.transport.send(messageRequest);
	}
	
	public Object receiveRequest() {
		Object[] parameters = new Object[100];
		String operation = null;		
		Object[] request = new Object[3]; 		
		Message messageRequest = new MessageRequest();
		
		//Recebe uma mensagem
		messageRequest = transport.receive();	
	
		/*Debug
		 System.out.println("NamingServiceTransport: Mensagem Request recebida! ");
		*/
		
		//Recebe o nome da operação
		operation = ((MessageRequest)messageRequest).getRequestHeader().getOperation();
		
		byte[] object_id = ((MessageRequest)messageRequest).getRequestHeader().getObject_id();
		
		//Recebe os parâmetros
		byte[] body = ((MessageRequest)messageRequest).getRequestBody();
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(body);  
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));  
			parameters = (Object[])is.readObject();			
		} catch(IOException io) {
		} catch(ClassNotFoundException io) {
		}	
	    
		request[0] = operation;
		request[1] = parameters;
		request[2] = object_id;
		
		/*
		 * Setar o host e porta do cliente em requisição 
		 * para enviar resposta
		 * */
		this.transport.setHost((String)parameters[2]);
		this.transport.setPort(((Integer)parameters[3]).intValue());		
		
		return request;
	}
	
	public void sendReply(Object response) {
		ByteArrayOutputStream byteStream2 = new ByteArrayOutputStream(); 
		try {
	    	ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(byteStream2)); 
	    	oos.flush();  
	    	oos.writeObject(response);  
	    	oos.flush(); 
		} catch(IOException io) {			
		}
		byte[] replyBody = byteStream2.toByteArray();
		
		/*Debug
		  System.out.println("NamingServiceTransport: cria mensagem Reply! ");
		*/
		
		//Cria o cabeçalho da mensagem
		byte major = 1;
		byte minor = 0;
		Version version = new Version(major, minor);
		char[] magic = "GIOP".toCharArray();
		boolean byte_order = true;
		byte message_type = 1;
		int message_size = replyBody.length;		
		MessageHeader header = new MessageHeader(
										magic, 
										version,
										byte_order,
										message_type,
										message_size									
								);
		
		//Cria o corpo da mensagem
		ServiceContextList serviceContextList = new ServiceContextList();
		int request_id = -100;
		ReplyStatusType replyStatusType = new ReplyStatusType(ReplyStatusType.NO_EXCEPTION);
		ReplyHeader replyHeader = new ReplyHeader(serviceContextList, 
								request_id, 
								replyStatusType);
		
//		Cria uma mensagem a ser enviada
		Message messageReply = new MessageReply();
		((MessageReply)messageReply).setHeader(header);
		((MessageReply)messageReply).setReplyHeader(replyHeader);
		((MessageReply)messageReply).setReplyBody(replyBody);
		
		/*Debug
		  System.out.println("NamingServiceTransport: Replay!");
		*/
		
		transport.send(messageReply);
	}
	
	public Object receiveReply () {
		Object obj = null;
//		Mensagem Replay do Servidor	
		Message messageReply = new MessageReply();
		
		//Recebe uma mensagem
		messageReply = this.transport.receive();
		
		/*Debug
		  System.out.println("NamingServiceTransport: Mensagem Reply recebida " + (messageReply instanceof Message));
		*/
		
		byte[] body = ((MessageReply)messageReply).getReplyBody();			
		try {
			ByteArrayInputStream byteStream2 = new ByteArrayInputStream(body);  
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream2));  
			obj = (Object)is.readObject();			
		} catch(IOException io) {
		} catch(ClassNotFoundException io) {
		}	

		return obj;
	}
}
