package transport;

import java.nio.ByteBuffer;

import protocol.Message;

public abstract class Transport {
	
	protected ByteBuffer buffer = ByteBuffer.allocate(2500);	
	
	public abstract void send(Message message);
	public abstract Message receive();
	public abstract void setPort(int port);
	public abstract void setHost(String hostname);
	public abstract void setPortLocal(int port);
	public abstract void setHostLocal(String hostname);
	public abstract int getPortLocal();
	public abstract String getHostLocal();
}