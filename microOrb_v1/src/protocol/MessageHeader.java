package protocol;

import java.io.Serializable;

public class MessageHeader implements Serializable {
	
	private char[] magic;
	private Version GIOP_version;
	private boolean byte_order;
	private byte message_type; /**0- request, 1-reply**/
	private int message_size;
	
	public MessageHeader (char[] magic, 
							Version GIOP_version, 
							boolean byte_order, 
							byte message_type, 
							int message_size) {
		this.magic = magic;
		this.GIOP_version = GIOP_version; 
		this.byte_order = byte_order; 
		this.message_type = message_type; 
		this.message_size = message_size;
	}

	public boolean isByte_order() {
		return byte_order;
	}

	public void setByte_order(boolean byte_order) {
		this.byte_order = byte_order;
	}

	public Version getGIOP_version() {
		return GIOP_version;
	}

	public void setGIOP_version(Version giop_version) {
		GIOP_version = giop_version;
	}

	public char[] getMagic() {
		return magic;
	}

	public void setMagic(char[] magic) {
		this.magic = magic;
	}

	public int getMessage_size() {
		return message_size;
	}

	public void setMessage_size(int message_size) {
		this.message_size = message_size;
	}

	public byte getMessage_type() {
		return message_type;
	}

	public void setMessage_type(byte message_type) {
		this.message_type = message_type;
	}

}
