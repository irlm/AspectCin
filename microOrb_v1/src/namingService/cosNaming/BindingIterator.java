package namingService.cosNaming;

import java.util.Iterator;

public interface BindingIterator {

	public Binding nextOne();
	
	public Iterator nextN();
	
	public void destroy();
	
}
