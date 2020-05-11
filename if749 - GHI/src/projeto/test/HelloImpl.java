package projeto.test;

import projeto.anORB.RemoteObject;

public class HelloImpl extends RemoteObject implements Hello {

	public void sayHello() {
		System.out.println("Hello");		
	}
	

}
