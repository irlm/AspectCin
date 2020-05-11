package namingService.cosNaming;

import java.io.Serializable;
import java.util.Collection;

public class NotFound extends Exception implements Serializable {
	
	private NotFoundReason why;
	private NameComponent[] restOfName;
	
	public NotFound (NotFoundReason why, NameComponent[] restOfName) {
		this.why = why;
		this.restOfName = restOfName;		
	}

	public NameComponent[] getRestOfName() {
		return restOfName;
	}

	public void setRestOfName(NameComponent[] restOfName) {
		this.restOfName = restOfName;
	}

	public NotFoundReason getWhy() {
		return why;
	}

	public void setWhy(NotFoundReason why) {
		this.why = why;
	}
	
	

}
