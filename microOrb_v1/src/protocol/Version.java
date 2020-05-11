package protocol;

import java.io.Serializable;

public class Version implements Serializable {

	private byte major;
	private byte minor;
	
	public Version(byte major, byte minor) {
		this.major = major;
		this.minor = minor;
	}
	
	public void setMajor(byte major) {
		this.major = major;
	}
	
	public byte getMajor() {
		return this.major;
	}
	
	public void setMinor(byte minor) {
		this.minor = minor;
	}
	
	public byte getMinor() {
		return this.minor;
	}	
	
}
