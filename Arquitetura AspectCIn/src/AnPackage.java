

import java.io.Serializable;
import java.net.InetAddress;


public class AnPackage implements Serializable {

	private static final long serialVersionUID = -8207355877981302060L;

	private String source;

	private int sourcePort;

	private String destiny;

	private int destinyPort;

	
	/**
	 * @directed true
	 */
	
	private PackageBody body;

	public AnPackage(String source, int sourcePort, String destiny,
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

	public String getDestiny() {
		return destiny;
	}

	public String getSource() {
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
