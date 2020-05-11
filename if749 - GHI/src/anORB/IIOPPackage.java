package anORB;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class IIOPPackage {

	private static final byte[] magicNumber = {0x47,0x49,0x4F,0x50}; //GIOP;
	private byte version = 0x01;
	private byte subVersion = 0x00;
	private byte flags = 0x00;
	private int size;
	private IIOPPackageBody body;
	
	public IIOPPackage(IIOPPackageBody body){
		this.body = body;
	}
	
	public byte[] toBytes()
	{
		byte[] retorno = null;
				
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			
			dos.write(magicNumber);
			dos.writeByte(version);
			dos.writeByte(subVersion);
			dos.writeByte(flags);
			dos.writeByte(body.type());
			byte[] bodyData = body.toBytes();
			dos.writeInt(bodyData.length);
			dos.write(bodyData);
			
			retorno = baos.toByteArray();			
			dos.close();
		}catch(Exception e) {}		
		
		return retorno;
	}

	public IIOPPackageBody getBody() {
		return body;
	}

	public void setBody(IIOPPackageBody body) {
		this.body = body;
	}

	public byte getFlags() {
		return flags;
	}

	public void setFlags(byte flags) {
		this.flags = flags;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public byte getSubVersion() {
		return subVersion;
	}

	public void setSubVersion(byte subVersion) {
		this.subVersion = subVersion;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public byte[] getMagicNumber() {
		return magicNumber;
	}
	
}
