package transport;

import java.io.*;
import java.net.*;

import protocol.Message;

public class TCPTransport extends Transport {
	
//	Declaro o socket cliente
	private Socket socket = null;
	private ServerSocket serverSocket = null;
	private String host;
	private int port;
	private int portLocal;
	private String hostLocal;
	private InputStream inFromServer;
	private OutputStream outToServer;	
		
	public TCPTransport (String hostname, int port) {	
		this.host = hostname;
		this.port = port;
		this.portLocal = port;
		this.hostLocal = hostname;
	}	
	
	public void send(Message message) {      
		
        try{         	
        	buffer.clear();
        	
        	/*Debug
        	 * System.out.println("TCPTransport: Estabelecendo conexão... ");
        	 * System.out.println("Enviando mensagem para host: " + host);
        	 * System.out.println("Enviando mensagem para port " + port);
        	 */
        	
        	socket = new Socket(host, port);        	   

        	/*Debug
        	 * System.out.println("TCPTransport: Enviando a mensagem...");
        	 */
    	
        	 //Cria a Stream de saida de dados      	
        	outToServer = socket.getOutputStream();
        	
        	ByteArrayOutputStream byteStream = new ByteArrayOutputStream(buffer.capacity());  
        	ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(byteStream)); 
        	oos.flush();  
        	oos.writeObject(message);  
        	oos.flush(); 
        	buffer.put(byteStream.toByteArray());
        	
        	outToServer.write(buffer.array());
        	
        	/*
    		 * Medições: Tempo No Middleware:
    		 * Solicitacao dentro do middleware
    		 * para realizar a operação.
    		 * */    	     
    		//DataHora fim request cliente/fim replay servidor
    	    System.out.println(System.nanoTime());
    	    
    	    
        	/*Debug
        	 * System.out.println("TCPTransport: Mensagem enviada...");
        	 */
        	
        //Trata possíveis exceções
        }catch(IOException e){     
        	e.printStackTrace();
            System.out.println("TCPTransport: Algum problema ocorreu ao enviar dados pelo socket.");	        
        }finally{            
            try{                
                //Encerra o socket cliente
            	outToServer.close();
            	socket.close(); 
            }catch(Exception e){}	        
        }
	}

	public Message receive() {       
		
        //Declaro a mensagem
		Message message = new Message();
        
        try{   
        	buffer.clear();
        	
        	serverSocket = new ServerSocket(portLocal);
        	
        	/* Debug
        	 * System.out.println("TCPTransport: Aguardando mensagem...");
        	 */
        	
        	socket = serverSocket.accept();        	     
        	    
        	/*
    		 * Medições: Tempo No Middleware:
    		 * Solicitacao dentro do middleware
    		 * para realizar a operação.
    		 * */    	     
    		//DataHora inicio request servidor/inicio replay cliente
    	    System.out.println(System.nanoTime());
    	    
    	    
        	/* Debug
        	 * System.out.println("TCPTransport: Chegou mensagem...");
        	 */
        	inFromServer = socket.getInputStream();
        	
        	/* Debug
        	 * System.out.println("TCPTransport: Recebendo mensagem...");
        	 */ 
        	
        	inFromServer.read(buffer.array());
		 
    		ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer.array());  
    		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));  
    		Object obj = is.readObject();     		
    		message = (Message)obj;  	

        //Trata possíveis exceções
        }catch(IOException e){  
        	e.printStackTrace();
            System.out.println("TCPTransport: Algum problema ocorreu ao receber dados pelo socket.");	        
        } catch(ClassNotFoundException e){          	
        }finally{            
            try{                
                //Encerra o socket cliente
            	inFromServer.close();
            	socket.close();             	
            	serverSocket.close();
            }catch(Exception e){}	        
        }
        
        return message;
	}

	public String getHostLocal() {
		return hostLocal;
	}

	public void setHostLocal(String hostLocal) {
		this.hostLocal = hostLocal;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String hostname) {
		this.host = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPortLocal() {
		return portLocal;
	}

	public void setPortLocal(int portLocal) {
		this.portLocal = portLocal;
	}

}