package exercicio4;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;


public class TCPConnector implements Connector {

	private int port;

	private String hostname;

	@SuppressWarnings("unused")
	private SOCKConnector sockConnector;

	public TCPConnector() {
	}

	public TCPConnector(String hostname, int port) {
		this.open(hostname, port);
	}

	/* (non-Javadoc)
	 * @see exercicio4.Connector#open(java.lang.String, int)
	 */
	public void open(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	/* (non-Javadoc)
	 * @see exercicio4.Connector#connect(exercicio4.SvcHandler)
	 */
	public void connect(ServiceHandler sh) throws UnknownHostException,
			SocketException, InstantiationException, IllegalAccessException,
			IOException {

		this.connectServiceHandler(sh);

		this.activateServiceHandler(sh);
	}

	protected void connectServiceHandler(ServiceHandler sh) throws SocketException,
			IOException {

		SOCKStream sockStream = new SOCKStream();

		this.sockConnector = new SOCKConnector(sockStream, this.hostname,
				this.port);
		
		sh.setHandle(sockStream);
	}

	protected void activateServiceHandler(ServiceHandler sh) {
		sh.open(null);
	}

}
