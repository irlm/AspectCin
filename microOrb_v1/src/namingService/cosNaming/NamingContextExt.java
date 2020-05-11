package namingService.cosNaming;

import java.util.Collection;

public interface NamingContextExt extends NamingContext {
	
	//private String stringName;
	//private String address;
	//private String URLString;
	
	String toString(Collection <NameComponent> n) throws InvalidName;
	
	Collection <NameComponent> toName(String stringName) throws InvalidName;
	
	String toUrl(String address, String stringName) throws InvalidAddress, InvalidName;
	
	Object resolveStr(String stringName) throws NotFound, CannotProceed, InvalidName;
 
}
