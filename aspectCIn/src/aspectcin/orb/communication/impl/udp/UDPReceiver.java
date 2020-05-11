package aspectcin.orb.communication.impl.udp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import aspectcin.orb.PackageHandler;
import aspectcin.orb.communication.api.AnPackage;
import aspectcin.orb.communication.api.Receiver;

public class UDPReceiver extends Receiver{

	private DatagramSocket socket;

	private boolean fim;
	
	private ByteBuffer buffer;
	private DatagramPacket packet;

	public UDPReceiver(int port) throws IOException {
		this.socket = new DatagramSocket(port);
		this.socket.setSoTimeout(50);
		
		buffer = ByteBuffer.allocate(2500);
	}

	public int getLocalPort() {
		return this.socket.getLocalPort();
	}
	
	public void run() {

		while (!fim) {
			
			buffer.clear();
			
        	packet = new DatagramPacket(buffer.array(), buffer.capacity());
        	
        	try {
				socket.receive(packet);
			
				new ConnectionHandler().start();
        	} catch (SocketTimeoutException e) {
				// Tente novamente!
			}  catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	

	public class ConnectionHandler extends Receiver.ConnectionHandler {

		public void run() {
			try {					
				
	    		ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());  
	    		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));  
	    		Object obj = is.readObject(); 
				
	    		AnPackage ret = null;
	    		
	    		ret = (AnPackage)obj;
				
	    		//TODO como assim folhosos
				PackageHandler.getSingleton().onPackageArrived(ret);

				is.close();   
			} catch (SocketException e) {
			} catch (Exception e) {
			}
		}
	}

	public void stopRunning() {
		this.fim = true;
	}

}
