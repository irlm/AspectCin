package namingService.cosNaming;

import java.util.HashMap;

public class NamingContext_Skel implements NamingContext {
	
	private NamingContextImpl namingContext;
	
	public NamingContext_Skel(HashMap hashNamingContext) {
		namingContext = new NamingContextImpl(hashNamingContext);		
	}
		
	public void bind (NameComponent[] n, Object obj) throws 
	    NotFound, CannotProceed, InvalidName, AlreadyBound {	
		namingContext.bind(n, obj);
	}
	
	public void rebind (NameComponent[] n, Object obj) throws 
		NotFound, CannotProceed, InvalidName {
		namingContext.rebind(n, obj);
	}
	
	public void bindContext (NameComponent[] n, NamingContext nc) throws 
		NotFound, CannotProceed, InvalidName, AlreadyBound {
		namingContext.bindContext(n, nc);
	}
	
	public void rebindContext (NameComponent[] n, NamingContext nc) throws 
		NotFound, CannotProceed, InvalidName {
		namingContext.rebindContext(n, nc);
	}
	
	public Object resolve (NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName {
		return namingContext.resolve(n);
	}
	
	public void unbind (NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName {
		namingContext.unbind(n);
	}
	
	public NamingContext newContext() {
		return namingContext.newContext();
	}
	
	public NamingContext bindNewContext(NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName, AlreadyBound {
		return namingContext.bindNewContext(n);
	}
	
	public void destroy() throws NotEmpty {
		namingContext.destroy();
	}
	
	public BindingIterator list() {
		return namingContext.list();
	}
	
	public Object invoke(Object request) {
		Object response = null;	
		try {		
			
			Object[] objects = (Object[])request;
			
			String operation = (String)objects[0];
			Object[] parameters = (Object[])objects[1];
			
			/* Debug
			 * System.out.println("NamingContext_Skel: operation... " + operation);	
			 */
			
			if("bind".equalsIgnoreCase(operation)) {
				this.bind((NameComponent[])parameters[0], (Object)parameters[1]);
			} else if ("rebind".equalsIgnoreCase(operation)) {
				this.rebind((NameComponent[])parameters[0], (Object)parameters[1]);
			} else if ("bindContext".equalsIgnoreCase(operation)) {
				this.bindContext((NameComponent[])parameters[0], (NamingContext)parameters[1]);
			} else if ("rebindContext".equalsIgnoreCase(operation)) {
				this.rebindContext((NameComponent[])parameters[0], (NamingContext)parameters[1]);
			} else if ("resolve".equalsIgnoreCase(operation)) {
				response = this.resolve((NameComponent[])parameters[0]);
			} else if ("unbind".equalsIgnoreCase(operation)) {
				this.unbind((NameComponent[])parameters[0]);
			} else if ("newContext".equalsIgnoreCase(operation)) {
				response = this.newContext();
			} else if ("bindNewContext".equalsIgnoreCase(operation)) {
				response = this.bindNewContext((NameComponent[])parameters[0]);
			} else if ("destroy".equalsIgnoreCase(operation)) {
				this.destroy();
			} else if ("list".equalsIgnoreCase(operation)) {
				response = this.list();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return response;
	}

}
