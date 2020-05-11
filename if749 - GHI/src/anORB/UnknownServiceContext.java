package anORB;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UnknownServiceContext implements ServiceContext {

	public byte[] toBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			DataOutputStream dos = new DataOutputStream(baos);
			
			dos.writeInt(17); // Unknown
			dos.writeInt(2);
			dos.writeShort(2);
			dos.writeShort(0);

			dos.close();
		} catch (IOException e) {
		}

		return baos.toByteArray();
	}

}
