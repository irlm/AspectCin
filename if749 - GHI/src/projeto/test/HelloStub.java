package projeto.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import projeto.anORB.Dispatcher;
import projeto.anORB.Package;
import projeto.anORB.RemoteObject;
import projeto.anORB.Request;

public class HelloStub extends RemoteObject implements Hello {

	private Dispatcher dispatcher;
	private long id;
	
	public HelloStub() throws UnknownHostException{
		this.dispatcher = Dispatcher.getSingleton();	
		this.id = dispatcher.register(host, 2178);		
	}
	
	public void sayHello() throws AnRemoteException {
		try {					
			Package pkg = new Package(InetAddress.getLocalHost(), host);
			Request req = new Request(oid, "sayHello");
			req.setStubId(this.id);
			pkg.setBody(req);
			dispatcher.send(pkg);
			//Package pckConfirmation = dispatcher.receive(id);
		}catch (UnknownHostException e){			
		
		} catch (IOException e){
			
		}	
	}

}
