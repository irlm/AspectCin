package exercicio4;

import java.io.IOException;
import java.net.SocketException;


public class TCPAcceptor implements Acceptor {

	protected int port = 1234;

	protected Class handlerFactory;

	protected SOCKAcceptor sockAcceptor;

	public TCPAcceptor() {
	}

	public TCPAcceptor(Class handlerFactory) {
		this.handlerFactory = handlerFactory;
	}

	/* (non-Javadoc)
	 * @see exercicio4.Acceptor#setHandlerFactory(java.lang.Class)
	 */
	public void setHandlerFactory(Class handlerFactory) {
		this.handlerFactory = handlerFactory;
	}

	/* (non-Javadoc)
	 * @see exercicio4.Acceptor#open(int)
	 */
	public void open(int port) throws IOException {
		this.port = port;
		this.sockAcceptor = new SOCKAcceptor(port);
	}

	/* (non-Javadoc)
	 * @see exercicio4.Acceptor#accept()
	 */
	public void accept() throws SocketException, InstantiationException,
			IllegalAccessException, IOException {

		ServiceHandler sh = this.makeServiceHandler();

		this.acceptServiceHandler(sh);

		this.activateServiceHandler(sh);
	}

	protected ServiceHandler makeServiceHandler() throws InstantiationException,
			IllegalAccessException {

		return (ServiceHandler) handlerFactory.newInstance();
	}

	protected void acceptServiceHandler(ServiceHandler sh) throws SocketException,
			IOException {

		SOCKStream sockStream = new SOCKStream();

		this.sockAcceptor.accept(sockStream);

		sh.setHandle(sockStream);
	}

	protected void activateServiceHandler(ServiceHandler sh) {
		sh.open(null);
	}

}
