package anorb.comunication;



public class ReplyException extends PackageBody  {

	private static final long serialVersionUID = -6371125389722586919L;

	private Throwable returned;
	
	public ReplyException(Throwable throwable) {
		this.returned = throwable;
	}

	public Throwable getReturned() {
		return returned;
	}

	public String toString() {		
			return "ReplyException: Stub_id=" + getStubId() + " Content="+returned;
	}
}
