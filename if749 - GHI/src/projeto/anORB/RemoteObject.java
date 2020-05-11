package projeto.anORB;

import java.net.InetAddress;

public class RemoteObject {

	protected String oid;
	protected String className;
	protected InetAddress host;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
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

	
}
