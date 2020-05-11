package transport;

import java.io.*;
import java.net.*;

import protocol.Message;

public class UDPTransport extends Transport {
	
//	Declaro o socket cliente
	private DatagramSocket socket;
	private String host;
	private int port;
	private int portLocal;
	private String hostLocal;
		
	public UDPTransport (String hostname, int port) {	
		this.host = hostname;
		this.port = port;
		this.portLocal = port;
		this.hostLocal = hostname;
	}
	
	
	public void send(Message message) { 
       
        try{    
        	buffer.clear();
        	
//     	 	declara socket cliente
            socket = new DatagramSocket();
            
            /* Debug
        	 * System.out.println("UDPTransport: Estabelecendo conexão...");
        	 */
            
//        	obtem endereço IP do servidor com o DNS
            InetAddress iPAddress = InetAddress.getByName(host);

            /* Debug
        	 * System.out.println("UDPTransport: Enviando a mensagem..." + host + " port " + port);
        	 */
            
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(buffer.capacity());  
        	ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(byteStream)); 
        	oos.flush();  
        	oos.writeObject(message);  
        	oos.flush();    
        	
        	buffer.put(byteStream.toByteArray()); 
     
    		DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity(), iPAddress, port);   
    	
    		socket.send(packet);
    		
    		/*
    		 * Medições: Tempo No Middleware:
    		 * Solicitacao dentro do middleware
    		 * para realizar a operação.
    		 * */    	     
    		//DataHora fim request cliente/fim replay servidor
    	    System.out.println(System.nanoTime());
    	    
    		/* Debug
        	 * System.out.println("UDPTransport: Mensagem enviada...");
        	 */ 
    		
        //Trata possíveis exceções
        }catch(IOException e){ 
        	e.printStackTrace();
            System.out.println("UDPTransport: Algum problema ocorreu ao enviar dados pelo socket.");	        
        } finally{            
            try{                
                //Encerra o socket cliente
            	socket.close();              
            }catch(Exception e){e.printStackTrace();}	        
        }
	}

	public Message receive() {       
        
        //Declaro a mensagem
		Message message = new Message();
		
        try{
        	buffer.clear();
        	/*
        	 * System.out.println("UDPTransport: Escuta port ..." + portLocal );
        	 */
//        	cria socket do servidor com a porta 
            socket = new DatagramSocket(portLocal);
            
           /* Debug
        	*  System.out.println("UDPTransport: Aguardando mensagem... portLocal " + portLocal );
        	*/
        	DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity());
        	
        	socket.receive(packet);  
        	
        	/*
    		 * Medições: Tempo No Middleware:
    		 * Solicitacao dentro do middleware
    		 * para realizar a operação.
    		 * */    	     
    		//DataHora inicio request servidor/inicio replay cliente
    	    System.out.println(System.nanoTime());
        	
			/*
			 * System.out.println("UDPTransport: Mensagem Recebida...");
			 */       	
        	 
    		ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());  
    		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));  
    		Object obj = is.readObject(); 

    		message = (Message)obj;
    		
        //Trata possíveis exceções
        }catch(IOException e){  
        	e.printStackTrace();
            System.out.println("UDPTransport: Algum problema ocorreu ao receber dados pelo socket.");	        
        } catch (ClassNotFoundException ce) {        
        } finally{            
            try{                
                //Encerra o socket cliente        	
            	socket.close();              	
            }catch(Exception e){e.printStackTrace();}	        
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