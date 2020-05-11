package namingService.cosNaming;

import java.util.Collection;

public interface NamingContext {

	public void bind (NameComponent[] n, Object obj) throws 
		NotFound, CannotProceed, InvalidName, AlreadyBound;
	
	public void rebind (NameComponent[] n, Object obj) throws 
		NotFound, CannotProceed, InvalidName;	
	
	public void bindContext (NameComponent[] n, NamingContext nc) throws 
		NotFound, CannotProceed, InvalidName, AlreadyBound;
	
	public void rebindContext (NameComponent[] n, NamingContext nc) throws 
		NotFound, CannotProceed, InvalidName;
	
	public Object resolve (NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName;
	
	public void unbind (NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName;
	
	public NamingContext newContext();
	
	public NamingContext bindNewContext(NameComponent[] n) throws 
		NotFound, CannotProceed, InvalidName, AlreadyBound;
	
	public void destroy() throws NotEmpty;
	
	public BindingIterator list();
	
}
