package aspectcin.namingservice;

import java.util.Hashtable;

import aspectcin.orb.RemoteObject;

public class NamingServiceImpl implements NamingService {

	private Hashtable<String, RemoteObject> register;


	public NamingServiceImpl() {
		register = new Hashtable<String, RemoteObject>();
	}

	public void register(String name, RemoteObject object) {
		register.put(name, object);
	}

	public Object lookup(String name) {
		RemoteObject remoteObject = register.get(name);
		return remoteObject;
	}

	public String[] list() {
		
		String[] ret = new String[register.size()];

		register.keySet().toArray(ret);
		//log.finest("The following list was sent:");
		//for (String s : ret) {
		//	log.finest(s);
		//}
		//log.finest("End of list");
		return ret;
	}

	public void unregister(RemoteObject object) {

		// TODO ate sentir a necessidade
	}

}
