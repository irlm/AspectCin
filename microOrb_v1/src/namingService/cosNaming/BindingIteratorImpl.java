package namingService.cosNaming;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class BindingIteratorImpl implements BindingIterator, Serializable {

	private Collection<Binding> bindings;
	
	public BindingIteratorImpl(HashMap hashObjects) {
		Iterator it = hashObjects.keySet().iterator();
		while (it.hasNext()) {			
			Binding binding = new Binding((NameComponent[])it.next(), BindingType.BIND_CONTEXT);
			bindings.add(binding);
		}
	}

	public Binding nextOne() {
		return bindings.iterator().next();
	}
	
	public Iterator nextN() {
		return bindings.iterator();
	}
	
	public void destroy() {
		bindings.clear();
	}
}
