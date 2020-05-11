package namingService.cosNaming;

import transport.Transport;

public class NamingContext_Stub implements NamingContext {

	private NamingServiceTransport namingServiceTransport;
	
	public NamingContext_Stub(Transport transport) {
		this.namingServiceTransport = new NamingServiceTransport(transport);
	}
	
	public void bind (NameComponent[] n, Object obj) throws 
		    NotFound, CannotProceed, InvalidName, AlreadyBound {	
		
		Object[] parameters = new Object[4];
		
		// Seta a operação a ser chamada
		String operation = "bind";
		
		// Seta os parâmetros da operação
		parameters[0] = n;
		parameters[1] = obj;	
		
		//
		this.namingServiceTransport.sendRequest(operation, parameters);
	}
		
	public void rebind (NameComponent[] n, Object obj) throws 
		NotFound, CannotProceed, InvalidName {
		
		Object[] parameters = new Object[4];
		
		// Seta a operação a ser chamada
		String operation = "rebind";
		
		// Seta os parâmetros da operação
		parameters[0] = n;
		parameters[1] = obj;	
		
		//
		this.namingServiceTransport.sendRequest(operation, parameters);
	}
		
	public void bindContext (NameComponent[] n, NamingContext nc) throws 
		NotFound, CannotProceed, InvalidName, AlreadyBound {
		
		Object[] parameters = new Object[4];
		
		// Seta a operação a ser chamada
		String operation = "bindContext";
		
		// Seta os parâmetros da operação
		parameters[0] = n;
		parameters[1] = nc;	
		
		//
		this.namingServiceTransport.sendRequest(operation, parameters);
	}
	
	public void rebindContext (NameComponent[] n, NamingContext nc) throws 
		NotFound, CannotProceed, InvalidName {
		
		Object[] parameters = new Object[4];
		
		// Seta a operação a ser chamada
		String operation = "rebindContext";
		
		// Seta os parâmetros da operação
		parameters[0] = n;
		parameters[1] = nc;	
		
		
		//
		this.namingServiceTransport.sendRequest(operation, parameters);
	}
	
	public Object resolve (NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName {
		Object obj = null;
		
		Object[] parameters = new Object[4];
		
		// Seta a operação a ser chamada
		String operation = "resolve";
		
		// Seta os parâmetros da operação
		parameters[0] = n;
		
		//
		this.namingServiceTransport.sendRequest(operation, parameters);
		
		obj = this.namingServiceTransport.receiveReply();
	
		return obj;
	}
		
	public void unbind (NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName {
		
		Object[] parameters = new Object[4];
		
		// Seta a operação a ser chamada
		String operation = "unbind";
		
		// Seta os parâmetros da operação
		parameters[0] = n;
		
		//
		this.namingServiceTransport.sendRequest(operation, parameters);	
	}
		
	public NamingContext newContext() {
		NamingContext nc = null;
		
		Object[] parameters = new Object[4];
		
		// Seta a operação a ser chamada
		String operation = "newContext";
		
		//
		this.namingServiceTransport.sendRequest(operation, parameters);	
		
		nc = (NamingContext)this.namingServiceTransport.receiveReply();
		
		return nc;
	}
		
	public NamingContext bindNewContext(NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName, AlreadyBound {
		
		NamingContext nc = null;
		Object[] parameters = new Object[4];
		
		// Seta a operação a ser chamada
		String operation = "bindNewContext";
		
		// Seta os parâmetros da operação
		parameters[0] = n;	
		
		//
		this.namingServiceTransport.sendRequest(operation, parameters);	
		
		nc = (NamingContext)this.namingServiceTransport.receiveReply();
		
		return nc;
	}
		
	public void destroy() throws NotEmpty {
		
		Object[] parameters = new Object[4];
		
		// Seta a operação a ser chamada
		String operation = "destroy";
		
		//
		this.namingServiceTransport.sendRequest(operation, parameters);	
	}
		
	public BindingIterator list() {
		
		Object[] parameters = new Object[4];
		BindingIterator bi = null;
		
		// Seta a operação a ser chamada
		String operation = "list";
		
		//
		this.namingServiceTransport.sendRequest(operation, parameters);	
		
		bi = (BindingIterator)this.namingServiceTransport.receiveReply();
		
		return bi;
	}
}
