

import java.io.Serializable;


public class Reply extends PackageBody {

	private static final long serialVersionUID = 859551136526895414L;

	private Serializable returned;

	public Reply(Serializable returned) {
		this.returned = returned;
	}

	public Serializable getReturned() {
		return returned;
	}

	public String toString() {
		if (returned == null) {
			return "Reply: Stub_id=" + getStubId() + " Content=void";
		} else {
			return "Reply: Stub_id=" + getStubId() + " Content="+returned;
		}
	}
}
