package aspectcin.orb.communication.impl.udp;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import aspectcin.orb.communication.api.AnPackage;
import aspectcin.orb.communication.impl.Connection;

public class UDPConnection extends Connection {

	private DatagramSocket socket;

	private ByteBuffer buffer;

	public UDPConnection(InetAddress host, int port) {
		super(host, port);
		
		this.buffer = ByteBuffer.allocate(2500);
	}

	public void send(AnPackage p) throws IOException {

		buffer.clear();

		socket = new DatagramSocket();

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(buffer
				.capacity());
		ObjectOutputStream oos = new ObjectOutputStream(
				new BufferedOutputStream(byteStream));
		oos.flush();
		oos.writeObject(p);
		oos.flush();

		buffer.put(byteStream.toByteArray());

		DatagramPacket packet = new DatagramPacket(buffer.array(), buffer
				.capacity(), this.getHost(), this.getPort());

		socket.send(packet);
	}

	public void disconnect() {
		socket.close();
	}
	
	public boolean equals(Object o){
		UDPConnection c = (UDPConnection) o;
		return c.socket.getInetAddress().equals(this.socket.getInetAddress()) && c.socket.getPort() == this.socket.getPort();
	}
}
