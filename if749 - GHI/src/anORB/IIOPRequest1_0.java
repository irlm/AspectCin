package anORB;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IIOPRequest1_0 extends IIOPRequest {

	private int responseExpected = 0x01000000;
	private String key;
	
	public IIOPRequest1_0(int id, String operation) {
		super(id, operation);
	}
	
	public void setKey(String key){
		this.key = key;
	}
	
	public byte[] toBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			DataOutputStream dos = new DataOutputStream(baos);
			
			dos.writeInt(0); // ServiceContexList Lenght
			//dos.write(serviceContext.toBytes());
			dos.writeInt(id);
			dos.writeInt(responseExpected);
			dos.writeInt(key.length());
			dos.write(key.getBytes("ASCII"));
			dos.writeInt(operation.length() + 1);
			dos.write(operation.getBytes("ASCII"));
			dos.writeByte(0);
			
			// Request Principal Lenght
			dos.writeInt(0);
			
			dos.close();
		} catch (IOException e) {}

		return baos.toByteArray();
	}
}
