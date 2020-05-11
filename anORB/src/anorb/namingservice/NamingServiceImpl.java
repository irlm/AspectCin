package anorb.namingservice;

import java.util.Hashtable;
import java.util.logging.Logger;

import anorb.RemoteObject;
import anorb.logging.AnLog;

public class NamingServiceImpl implements NamingService {

	private Hashtable<String, RemoteObject> register;

	private Logger log;

	public NamingServiceImpl() {
		register = new Hashtable<String, RemoteObject>();
		log = AnLog.naming;
	}

	public void register(String name, RemoteObject object) {
		log.fine("Registering " + name);
		register.put(name, object);
		log.fine(name + " registered");
	}

	public RemoteObject lookup(String name) {
		log.fine("Searching for " + name);
		RemoteObject remoteObject = register.get(name);
		if (remoteObject != null) {
			log.fine(name + " found");
		}
		return remoteObject;
	}

	public String[] list() {
		log.fine("Requested list");
		String[] ret = new String[register.size()];

		register.keySet().toArray(ret);
		log.finest("The following list was sent:");
		for (String s : ret) {
			log.finest(s);
		}
		log.finest("End of list");
		return ret;
	}

	public void unregister(RemoteObject object) {

		// TODO ate sentir a necessidade
	}

}
