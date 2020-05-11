package projeto.anORB;

import java.io.Serializable;
import java.net.InetAddress;


public class Package implements Serializable {
	
	private static final long serialVersionUID = -8207355877981302060L;
	
	private InetAddress source;
	private InetAddress destiny;
	private PackageBody body;

	public Package(InetAddress source, InetAddress destiny) {
		this.source = source;
		this.destiny = destiny;
	}

	public PackageBody getBody() {
		return body;
	}

	public void setBody(PackageBody body) {
		this.body = body;
	}

	public InetAddress getDestiny() {
		return destiny;
	}

	public InetAddress getSource() {
		return source;
	}	
	
}
