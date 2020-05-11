

public abstract class Receiver extends Thread{

	public abstract int getLocalPort();

	public abstract void stopRunning();
	
	
	/**
	 * @directed true
	 */
	
	PackageHandler packageHandler;
	
	/**
	 * @directed true
	 */
	
	AnPackage anPackage;

	public abstract class ConnectionHandler extends Thread{
		@Override
		public abstract void run();
		
		public Receiver receiver = Receiver.this;
	}
}
