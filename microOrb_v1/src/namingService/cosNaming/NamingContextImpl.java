package namingService.cosNaming;

import java.util.HashMap;

public class NamingContextImpl implements NamingContext {
	
	private HashMap hashObjects;	
	private NameComponent[] name;
	protected HashMap hashNamingContext;
	
	public NamingContextImpl() {}
	
	public NamingContextImpl(HashMap hashNamingContext) {	
		this.hashNamingContext = hashNamingContext;
		this.hashObjects = new HashMap();
	}
	
	public void bind (NameComponent[] n, Object obj) throws 
		    NotFound, CannotProceed, InvalidName, AlreadyBound {
		try {
			
			if(n == null || n.length == 0) {
				throw new InvalidName("Object has Invalid Name!");
			}
			
			if(!hashObjects.containsKey(n[0].getId())){	
				this.hashObjects.put(n[0].getId(), obj);
			} else {
				 throw new AlreadyBound("Object Already Bound!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CannotProceed(this, n);
		}
	}

	public void rebind (NameComponent[] n, Object obj) throws 
		NotFound, CannotProceed, InvalidName {
		try {
			if(n == null || n.length == 0) {
				throw new InvalidName("Object has Invalid Name!");
			}
			if(!hashObjects.isEmpty() && hashObjects.containsKey(n[0].getId())) {
				this.hashObjects.put(n[0].getId(), obj);
			} else {
				 //throw new NotFound(NotFoundReason, n);
			}
		} catch (Exception e) {
			throw new CannotProceed(this, n);
		}
	}
	
	public void bindContext (NameComponent[] n, NamingContext nc) throws 
		NotFound, CannotProceed, InvalidName, AlreadyBound {
		try {
			if(n == null || n.length == 0) {
				throw new InvalidName("Context has Invalid Name!");
			}
			if(!hashNamingContext.containsKey(n[0].getId())) {
				this.name = n;
				this.hashNamingContext.put(n[0].getId(), nc);
			} else {
				 throw new AlreadyBound("Context Already Bound!");
			}
		} catch (Exception e) {
			throw new CannotProceed(nc, n);
		}	
	}
	
	public void rebindContext (NameComponent[] n, NamingContext nc) throws 
		NotFound, CannotProceed, InvalidName {
		try {
			if(n == null || n.length == 0) {
				throw new InvalidName("Context has Invalid Name!");
			}
			if(!hashNamingContext.isEmpty() && hashNamingContext.containsKey(n[0].getId())) {
				this.hashNamingContext.put(n[0].getId(), nc);
			} else {
//				throw new NotFound(NotFoundReason, n);
			}
		} catch (Exception e) {
			throw new CannotProceed(nc, n);
		}
	}
	
	public Object resolve (NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName {
		Object obj = null;
		try {
			if(n == null || n.length == 0) {
				throw new InvalidName("Object has Invalid Name!");
			}
			if(!hashObjects.isEmpty() && hashObjects.containsKey(n[0].getId())) {
				obj = this.hashObjects.get(n[0].getId());
			} else {
				 //throw new NotFound(NotFoundReason, n);
			}
		} catch (Exception e) {
			throw new CannotProceed(this, n);
		}
		
		return obj;
	}
	
	public void unbind (NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName {
		try {
			if(n == null || n.length == 0) {
				throw new InvalidName("Object has Invalid Name!");
			}
			if(!hashObjects.isEmpty() && hashObjects.containsKey(n[0].getId())) {
				this.hashObjects.remove(n[0].getId());
			} else {
				 //throw new NotFound(NotFoundReason, n);
			}
		} catch (Exception e) {
			throw new CannotProceed(this, n);
		}		
	}
	
	public NamingContext newContext() {
		return new NamingContextImpl();
	}
	
	public NamingContext bindNewContext(NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName, AlreadyBound {
		
		NamingContext nc = null;
		
		try {
			if(n == null || n.length == 0) {
				throw new InvalidName("Context has Invalid Name!");
			}
			if(!hashNamingContext.containsKey(n[0].getId())) {
				nc = this.newContext();
				this.name = n;
				this.hashNamingContext.put(n[0].getId(), nc);				
			} else {
				 throw new AlreadyBound("Context Already Bound!");
			}
		} catch (Exception e) {
			throw new CannotProceed(nc, n);
		}	
		
		return nc;
	}
	
	public void destroy() throws NotEmpty {
		if(hashNamingContext.isEmpty()) {
			throw new NotEmpty("Context not removed, it contains bindings!");
		} else {
			this.hashNamingContext.remove(this.name[0].getId());
		}
	}
	
	public BindingIterator list() {
		return new BindingIteratorImpl(this.hashObjects);
	}
	

}
