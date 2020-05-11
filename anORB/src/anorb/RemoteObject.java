package anorb;

import java.io.Serializable;
import java.net.InetAddress;

public class RemoteObject implements Serializable {

	private static final long serialVersionUID = -2278880122479294831L;

	protected String oid;	
	protected InetAddress host;
	protected int port;
	protected long stubId;
	protected String stubClass;

	public long getStubId() {
		return stubId;
	}

	public InetAddress getHost() {
		return host;
	}

	public void setHost(InetAddress host) {
		this.host = host;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getStubClass() {
		return stubClass;
	}

	public void setStubClass(String stubClass) {
		this.stubClass = stubClass;
	}
}
