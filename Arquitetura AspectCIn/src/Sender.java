


public class Sender {

	
	/**
	 * @directed true
	 */
	
	Connection sender = null;
	
	/**
	 * @directed true
	 */
	
	AnPackage anPackage;
	
	public void send(RemoteObject target, PackageBody body) {

		anPackage = new AnPackage("Host", CInORB.getLocalPort(), target.getHost(), target.getPort());
		anPackage.setBody(body);
		
		sender.send(anPackage);
	}
}
