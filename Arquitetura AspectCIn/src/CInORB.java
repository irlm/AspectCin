
public class CInORB {

	/**
	 * @directed true
	 */

	private static CInORB singleton;

	private int port;

	/**
	 * @directed true
	 */

	private NamingServiceStub stub;

	private CInORB(int port, String namingAddress, int namingPort) {

		this.stub = new NamingServiceStub(namingAddress, namingPort);

		this.port = port;

	}

	public static CInORB init(int port, String namingAddress, int namingPort) {
		if (singleton == null) {
			synchronized (CInORB.class) {
				if (singleton == null) {
					singleton = new CInORB(port, namingAddress, namingPort);
				}
			}
		}
		return singleton;
	}

	public static int getLocalPort() {
		int result = 0;

		if (singleton != null) {
			result = singleton.port;
		}

		return result;
	}

	public NamingService getNamingService() {
		return stub;
	}

	public int getPort() {
		return port;
	}

}
