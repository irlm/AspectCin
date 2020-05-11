package namingService.cosNaming;

import java.util.Collection;

public class CannotProceed extends Exception {
	
	private NamingContext ctx;
	private NameComponent[] restOfName;
	
	public CannotProceed(NamingContext ctx, NameComponent[] name) {
		this.ctx = ctx;
		restOfName = name;
	}
	public NamingContext getCtx() {
		return ctx;
	}
	public void setCtx(NamingContext ctx) {
		this.ctx = ctx;
	}
	public NameComponent[] getRestOfName() {
		return restOfName;
	}
	public void setRestOfName(NameComponent[] restOfName) {
		this.restOfName = restOfName;
	}	

}
