

import java.io.Serializable;

public class RemoteObject implements Serializable {

	private static final long serialVersionUID = -2278880122479294831L;

	protected String oid;	
	protected String host;
	protected int port;

	protected String stubClass;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
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
