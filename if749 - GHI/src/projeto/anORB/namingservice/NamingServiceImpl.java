package projeto.anORB.namingservice;

import java.util.Hashtable;

import projeto.anORB.RemoteObject;

public class NamingServiceImpl implements NamingService {

	private Hashtable<String, RemoteObject> register; 
		
	public NamingServiceImpl() {		
		register = new Hashtable<String, RemoteObject>();
	}

	public void register(String name, RemoteObject object) {
		
		register.put(name, object);
		
		System.out.println("Objeto registrado");
	}

	public RemoteObject lookup(String name) {		
		return register.get(name);
	}

	public String[] list() {
		
		String[] ret = new String[register.size()];
		
		register.keySet().toArray(ret);
		
		return ret;
	}

	public void unregister(RemoteObject object) {
		
		//TODO ate sentir a necessidade
	}

}
