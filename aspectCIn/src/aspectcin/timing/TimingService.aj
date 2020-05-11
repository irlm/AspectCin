package aspectcin.billing;

import aspectcin.orb.RemoteObject;
import aspectcin.orb.communication.api.AnPackage;
import aspectcin.orb.communication.api.Receiver;
import aspectcin.orb.communication.api.Sender;
import aspectcin.orb.communication.impl.Connection;
import aspectcin.orb.communication.impl.PackageBody;
import aspectcin.timing.*;

public aspect TimingService {

	// ------------------------------------------------------------------------------------//
	// Connection
	public long Connection.totalConnectTime = 0;

	public long getTotalConnectTime(Connection connection) {
		return connection.totalConnectTime;
	}

	private Timer Connection.timer = new Timer();

	public Timer getTimer(Connection conn) {
		return conn.timer;
	}
	
	pointcut ConnectionSend(Connection c, AnPackage p): target(c)
			&& call(void Connection.send(AnPackage)) 
			&& args(p);

	before(Connection c, AnPackage p): ConnectionSend(c, p){
		getTimer(c).start();
	}

	after(Connection c, AnPackage p): ConnectionSend(c, p){
		getTimer(c).stop();
		c.totalConnectTime += getTimer(c).getTime();
		System.out.println("c.totalConnectTime = "
				+ ((double) c.totalConnectTime / 1000) + " segundos");
	}
		
	public long Sender.totalConnectTime = 0;

	public long getTotalConnectTime(Sender sender) {
		return sender.totalConnectTime;
	}
	
	private Timer Sender.timer = new Timer();

	public Timer getTimer(Sender sender) {
		return sender.timer;
	}
	
	pointcut ObjectSend(Sender sender): this(sender)
		&& execution(void Sender.send(RemoteObject, PackageBody));

	
	before(Sender sender): ObjectSend(sender){
		getTimer(sender).start();
	}

	after(Sender sender): ObjectSend(sender){
		getTimer(sender).stop();
		sender.totalConnectTime += getTimer(sender).getTime();
		System.out.println("sender.totalConnectTime = "
				+ ((double) sender.totalConnectTime / 1000) + " segundos");
	}
	// ------------------------------------------------------------------------------------//

	// ------------------------------------------------------------------------------------//
	// Receiver
	public long Receiver.totalConnectTime = 0;

	public long getTotalConnectTime(Receiver receiver) {
		return receiver.totalConnectTime;
	}

	public long Receiver.ConnectionHandler.totalConnectTime = 0;

	public long getTotalConnectTime(Receiver.ConnectionHandler connectionHandler) {
		return connectionHandler.totalConnectTime;
	}

	private Timer Receiver.ConnectionHandler.timer = new Timer();

	public Timer getTimer(Receiver.ConnectionHandler connectionHandler) {
		return connectionHandler.timer;
	}

	pointcut Receiving(Receiver.ConnectionHandler r): target(r) 
			&& execution(void Receiver.ConnectionHandler.run());

	before(Receiver.ConnectionHandler r): Receiving(r){
		getTimer(r).start();
	}

	after(Receiver.ConnectionHandler r): Receiving(r){
		getTimer(r).stop();
		r.totalConnectTime += getTimer(r).getTime();
		r.receiver.totalConnectTime += r.totalConnectTime;
		System.out.println("r.totalConnectTime = "
				+ ((double) r.totalConnectTime / 1000) + " segundos");
		System.out.println("r.receiver.totalConnectTime = "
				+ ((double) r.receiver.totalConnectTime / 1000) + " segundos");
	}
	// ------------------------------------------------------------------------------------//

}
