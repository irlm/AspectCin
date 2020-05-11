package namingService.cosNaming;

public class InvalidAddress extends Exception {
	
	private String msg;
	
	public InvalidAddress () {}
	
	public InvalidAddress (String msg) {
		super(msg);
		this.msg = msg;
	}
	
	public String getMessage() {
		return this.msg;
	}
}

