package aspectcin.log;

import java.net.InetAddress;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import aspectcin.orb.communication.api.AnPackage;
import aspectcin.orb.communication.impl.tcp.TCPReceiver;
import aspectcin.orb.communication.impl.udp.UDPReceiver;


@Aspect
public class CInLogComunication {

	private static boolean logging = true; 
	

	@Pointcut("(call(aspectcin.orb.communication.impl.tcp.TCPConnection.new(InetAddress, int)) && args(host, port))")
	void logTCPConnectionConnect(InetAddress host, int port) {
	}

	@Before("logTCPConnectionConnect(host, port)")
	public void beforelogTCPConnectionConnect(InetAddress host, int port) {
		if(logging)
			System.out.println("LOG - Trying to (TCP) connect to " + host + ":" + port);
	}

	@After("logTCPConnectionConnect(host, port)")
	public void afterlogTCPConnectionConnect(InetAddress host, int port) {
		if(logging)
			System.out.println("LOG - (TCP) Connected to " + host + ":" + port);
	}	
	
	
	@Pointcut("(call(aspectcin.orb.communication.impl.udp.UDPConnection.new(InetAddress, int)) && args(host, port))")
	void logUDPConnectionConnect(InetAddress host, int port) {
	}
	
	@Before("logUDPConnectionConnect(host, port)")
	public void beforelogUDPConnectionConnect(InetAddress host, int port) {
	if(logging)
		System.out.println("LOG - (UDP) Trying to connect to " + host + ":" + port);
	}
	
	@After("logUDPConnectionConnect(host, port)")
	public void afterlogUDPConnectionConnect(InetAddress host, int port) {
	if(logging)
		System.out.println("LOG - (UDP) Connected to " + host + ":" + port);
	}
	
	
	@Pointcut("call(void aspectcin.orb.communication.impl.Connection.send(AnPackage)) && args(p)")
	void logSend(AnPackage p) {
	}
	
	@Before("logSend(p)")
	public void beforelogSend(AnPackage p) {
		//TODO Arrumar esse sysout
		//System.out.println("LOG - " .toString() + " - PKG " + pkgCount++ + " SENT TO "
		//		+ pkg.getDestiny() + "(" + pkg.getDestinyPort() + ")");
	}
	
	@After("logSend(p)")
	public void afterlogSend(AnPackage p) {
		if(logging){
			System.out.println("LOG - Sender sent a package");
			System.out.println("LOG - " + p.toString());
		}
	}

	//TODO Verificar pq execution em vez do call
	@Pointcut("execution(* aspectcin.orb.communication.impl.tcp.TCPConnection.disconnect())")
	void logDisconnect() {
	}

	@After("logDisconnect()")
	public void afterlogDisconnect() {
		if(logging)
			System.out.println("LOG - Connection closed");
	}

	
	@Pointcut("( call(aspectcin.orb.communication.impl.tcp.TCPReceiver.new(int)) && args(port)) ||" +
			" (call(aspectcin.orb.communication.impl.udp.UDPReceiver.new(int)) && args(port)) ")
	void logReceiver(int port) {
	}

	@Before("logReceiver(port)")
	public void beforelogReceiver(int port) {
		if(logging)
			System.out.println("LOG - Creating receiver on port " + port);
	}

	@After("logReceiver(port)")
	public void afterlogReceiver(int port) {
		if(logging)
			System.out.println("LOG - Receiver created sucessfully");
	}

	//TODO Verificar pq execution em vez do call
	@Pointcut("execution(* aspectcin.orb.communication.impl.tcp.TCPReceiver.run()) && target(receiver)")
	void logTCPReceiverRun(TCPReceiver receiver) {
	}

	@Before("logTCPReceiverRun(receiver)")
	public void beforelogTCPReceiverRun(TCPReceiver receiver) {
		if(logging)
			System.out.println("LOG - TCP Starting receiver");
	}

	@After("logTCPReceiverRun(receiver)")
	public void afterlogTCPReceiverRun(TCPReceiver receiver) {
		if(logging)
			System.out.println("LOG - TCP Connection accepted on port " + receiver.getLocalPort());
	}
	
	//TODO Verificar pq execution em vez do call
	@Pointcut("execution(* aspectcin.orb.communication.impl.udp.UDPReceiver.run()) && target(receiver)")
	void logUDPReceiverRun(UDPReceiver receiver) {
	}

	@Before("logUDPReceiverRun(receiver)")
	public void beforelogRun(UDPReceiver receiver) {
		if(logging)
			System.out.println("LOG - Starting UDP receiver");
	}

	@After("logUDPReceiverRun(receiver)")
	public void afterlogRun(UDPReceiver receiver) {
		if(logging){
			System.out.println("LOG - UDP Connection accepted on port " + receiver.getLocalPort());
			System.out.println("LOG - UDPReceiver time = " + System.nanoTime());
		}
	}

}
