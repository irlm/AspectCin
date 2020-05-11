package exercicio1;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 4459089200897950638L;
	
	private String message;

	public Message(String string) {
		this.message = string;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
