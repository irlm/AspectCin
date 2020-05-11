package ior;

import java.io.Serializable;

import protocol.Version;

public class ProfileBody implements Serializable{

	private Version version;
	String host;
	int port;
	byte[] objectKey;
	
	public ProfileBody(String host, byte[] objectKey, int port, Version version) {
		this.host = host;
		this.objectKey = objectKey;
		this.port = port;
		this.version = version;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public byte[] getObjectKey() {
		return this.objectKey;
	}
	
	public void setObjectKey(byte[] objectKey) {
		this.objectKey = objectKey;
	}
	
	public int getPort() {
		return this.port;
	}
		
	public void setPort(int port) {
		this.port = port;
	}
	
	public Version getVersion() {
		return this.version;
	}
	
	public void setVersion(Version version) {
		this.version = version;
	}
		
}