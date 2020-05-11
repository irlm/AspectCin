package transport;

import java.io.*;
import java.net.*;

import protocol.Message;

public class HTTPTransport extends Transport {
	
//	Declaro o socket cliente
	private Socket socket = null;
	private ServerSocket serverSocket = null;
	private String host;
	private int port;
	private int portLocal;
	private String hostLocal;
	private InputStream inFromServer;
	private OutputStream outToServer;
	private BufferedReader buffer;
		
	public HTTPTransport (String hostname, int port) {	
		this.host = hostname;
		this.port = port;
	}	
	
	public void send(Message message) {       
			
        try{    
        	System.out.println("HTTPTransport: Estabelecendo conexão..." + message.toString()); 
        	
        	URL url = new URL(message.toString());
        	
        	port = url.getPort()==-1?80:url.getPort();
        	host = url.getHost();
 	
        	socket = new Socket(host, port); 
        	
            //Cria a Stream de saida de dados      	
        	outToServer = socket.getOutputStream();
            
        	System.out.println("HTTPTransport: Enviando a mensagem...");  
            //metodo GET
        	outToServer.write("GET / HTTP/1.0\n\n".getBytes());
        	outToServer.flush();        	
        	System.out.println("HTTPTransport: Mensagem enviada..."); 
        	
        	//resposta do servidor
        	inFromServer = socket.getInputStream();
        	buffer = new BufferedReader(new
        	        InputStreamReader(inFromServer));        	
        	String s = buffer.readLine();
    		while (s!= null)
    		{
	    		System.out.println(s );
	    		s=buffer.readLine();
    		}
        	
        //Trata possíveis exceções
        }catch(IOException e){  
        	e.printStackTrace();
            System.out.println("HTTPTransport: Algum problema ocorreu ao enviar dados pelo socket.");	        
        }finally{            
            try{                
                //Encerra o socket cliente
            	outToServer.close();
            	socket.close();                
            }catch(IOException e){}	        
        }
	}

	public Message receive() {       
        
        //Declaro a mensagem
		Message message = new Message();
        
        try{   
        	serverSocket = new ServerSocket(80);
        	
        	System.out.println("HTTPTransport: Aguardando mensagem...");
        	socket = serverSocket.accept();
        	
        	System.out.println("HTTPTransport: Chegou mensagem...");
        	inFromServer = socket.getInputStream();
        	buffer = new BufferedReader(new
        	        InputStreamReader(inFromServer));
        	
        	System.out.println("HTTPTransport: Recebendo mensagem..."); 
        	//message.setMessage(buffer.readLine());        	

        	String s = buffer.readLine();
    		while (s!= null)
    		{
	    		System.out.println(s );
	    		s=buffer.readLine();
    		}
        	
        //Trata possíveis exceções
        }catch(IOException e){  
            System.out.println("HTTPTransport: Algum problema ocorreu ao receber dados pelo socket.");	        
        }finally{            
            try{                
                //Encerra o socket cliente
            	inFromServer.close();
            	socket.close();             	
            	serverSocket.close();
            }catch(IOException e){}	        
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