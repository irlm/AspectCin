package protocol;

import java.io.Serializable;

public class MessageReply extends Message implements Serializable {

	private ReplyHeader replyHeader;
	private byte[] replyBody;
	
	public MessageReply() {		
	}
	
	public MessageReply(MessageHeader header, ReplyHeader replyHeader, byte[] replyBody) {
		super(header);
		this.replyHeader = replyHeader;
		this.replyBody = replyBody;
	}

	public byte[] getReplyBody() {
		return replyBody;
	}

	public void setReplyBody(byte[] replyBody) {
		this.replyBody = replyBody;
	}

	public ReplyHeader getReplyHeader() {
		return replyHeader;
	}

	public void setReplyHeader(ReplyHeader replyHeader) {
		this.replyHeader = replyHeader;
	}
	
}
