package namingService.cosNaming;

public class NotEmpty extends Exception {
		
	private String msg;
	
	public NotEmpty () {}
	
	public NotEmpty (String msg) {
		super(msg);
		this.msg = msg;
	}
	
	public String getMessage() {
		return this.msg;
	}
}

