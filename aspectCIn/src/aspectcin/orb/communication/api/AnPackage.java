package aspectcin.orb.communication.api;

import java.io.Serializable;
import java.net.InetAddress;

import aspectcin.orb.communication.impl.PackageBody;


public class AnPackage implements Serializable {

	private static final long serialVersionUID = -8207355877981302060L;

	private InetAddress source;

	private int sourcePort;

	private InetAddress destiny;

	private int destinyPort;

	private PackageBody body;

	public AnPackage(InetAddress source, int sourcePort, InetAddress destiny,
			int destinyPort) {
		this.source = source;
		this.sourcePort = sourcePort;
		this.destiny = destiny;
		this.destinyPort = destinyPort;
	}

	public int getDestinyPort() {
		return destinyPort;
	}

	public int getSourcePort() {
		return sourcePort;
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

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Destination: ");
		sb.append(destiny.toString());
		sb.append(':');
		sb.append(destinyPort);
		sb.append('\n');
		sb.append("\tOrigin: ");
		sb.append(source.toString());
		sb.append(':');
		sb.append(sourcePort);
		sb.append('\n');
		sb.append("\tBody:");
		sb.append("\n\t\t");
		sb.append(body);
		return sb.toString();
	}

}
