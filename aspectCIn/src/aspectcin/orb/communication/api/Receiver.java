package aspectcin.orb.communication.api;

public abstract class Receiver extends Thread{

	public abstract int getLocalPort();

	public abstract void stopRunning();

	public abstract class ConnectionHandler extends Thread{
		@Override
		public abstract void run();
		
		public Receiver receiver = Receiver.this;
	}
}
