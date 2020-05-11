package exercicio3;

import java.util.Vector;

public class ComponentRepository {

	private Vector<Component> components;

	private ClassLoader loader;

	public ComponentRepository() {
		this.components = new Vector<Component>();
		this.loader = ClassLoader.getSystemClassLoader();
	}

	public void insert(String service, Object... parameters) {
		if (!contains(service)) {
			try {
				Class classe = loader.loadClass(service);
				Component component = (Component) classe.newInstance();
				if (component.init(this,parameters)) {
					new Thread(component).start();
					this.components.add(component);
				} else {
					throw new RuntimeException("unable to initialize service.");	
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("unknown service");
			} catch (InstantiationException e) {
				throw new RuntimeException(e.getLocalizedMessage());
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e.getLocalizedMessage());
			}
		} else {
			throw new RuntimeException("the service is already running");
		}
	}

	public void remove(Component component) {
		int index = components.indexOf(component);

		if (index >= 0) {			
			components.remove(index).fini();			
		}
	}

	private boolean contains(String service) {
		boolean result = false;

		for (Component c : components) {
			result = c.getClass().getName().equalsIgnoreCase(service);
			if (result) {
				break;
			}
		}

		return result;
	}
	
	public Vector<Component> getComponents(){
		return this.components;
	}
	
	public Component getComponent(String service) {
		Component result = null;

		for (Component c : components) {
			if (c.getClass().getName().equalsIgnoreCase(service)) {
				result = c;
				break;
			}
		}

		return result;
	}
	
	public void shutdown(){
		for (Component c : components) {
			c.fini();
		}
	}
	
	
}
