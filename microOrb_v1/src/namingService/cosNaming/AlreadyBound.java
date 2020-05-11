package namingService.cosNaming;

public class AlreadyBound extends Exception {
		
	private String msg;
	
	public AlreadyBound () {}
	
	public AlreadyBound (String msg) {
		super(msg);
		this.msg = msg;
	}
	
	public String getMessage() {
		return this.msg;
	}
}
