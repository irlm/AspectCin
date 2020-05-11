package anORB;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IIOPRequest1_2 extends IIOPRequest {
	
	public static final byte SYNC_WITH_TARGET = 3;
	
	private byte flags = SYNC_WITH_TARGET;
	private byte targetAdressDiscriminant = 0;
	private int[] keyAddr;
	
	public IIOPRequest1_2(int id, String operation) {
		super(id, operation);
		// TODO Auto-generated constructor stub
	}
	
	public byte[] toBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeInt(id);
			dos.writeByte(flags);
			// Reserved
			dos.writeByte(0);
			dos.writeByte(0);
			dos.writeByte(0);
			
			dos.writeByte(targetAdressDiscriminant);
			dos.write(keyAddr.length);
			for (int i = 0; i < keyAddr.length; i++) {
				dos.writeByte(keyAddr[i]);	
			}
			dos.write(operation.length());
			dos.writeChars(operation);
			dos.close();
		} catch (IOException e) {}

		return baos.toByteArray();
	}
}
