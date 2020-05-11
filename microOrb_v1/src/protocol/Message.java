package protocol;
import java.io.Serializable;

public class Message implements Serializable {
				
	private MessageHeader header;

	public Message() {}
	
	public Message(MessageHeader header) {
		super();
		this.header = header;
	}

	public MessageHeader getHeader() {
		return header;
	}

	public void setHeader(MessageHeader header) {
		this.header = header;
	}	

}
