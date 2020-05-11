package namingService.cosNaming;

public class InvalidName  extends Exception {
	
	private String msg;
	
	public InvalidName () {}
	
	public InvalidName (String msg) {
		super(msg);
		this.msg = msg;
	}
	
	public String getMessage() {
		return this.msg;
	}
}
