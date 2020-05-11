package protocol;

import java.io.Serializable;

public class MessageRequest extends Message implements Serializable {
	
	private RequestHeader requestHeader;
	private byte[] requestBody;
	
	public MessageRequest() {		
	}
	
	public MessageRequest(MessageHeader header, RequestHeader requestHeader, byte[] requestBody) {
		super(header);
		this.requestHeader = requestHeader;
		this.requestBody = requestBody;
	}

	public byte[] getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(byte[] requestBody) {
		this.requestBody = requestBody;
	}

	public RequestHeader getRequestHeader() {
		return requestHeader;
	}

	public void setRequestHeader(RequestHeader requestHeader) {
		this.requestHeader = requestHeader;
	}
	
}
