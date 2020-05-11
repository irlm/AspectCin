


public interface NamingService {

	public void register(String name, RemoteObject object);

	public Object lookup(String name);

	public String[] list();

	public void unregister(RemoteObject object);
}
