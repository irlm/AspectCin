package protocol;

import java.io.Serializable;

public class ReplyHeader implements Serializable {
	private ServiceContextList serviceContextList;
	private int request_id;
	private ReplyStatusType replyStatusType;
	
	public ReplyHeader(ServiceContextList serviceContextList, int request_id, ReplyStatusType replyStatusType) {
		super();
		this.serviceContextList = serviceContextList;
		this.request_id = request_id;
		this.replyStatusType = replyStatusType;
	}
	
	public ReplyStatusType getReplyStatusType() {
		return replyStatusType;
	}
	
	public void setReplyStatusType(ReplyStatusType replyStatusType) {
		this.replyStatusType = replyStatusType;
	}
	
	public int getRequest_id() {
		return request_id;
	}
	
	public void setRequest_id(int request_id) {
		this.request_id = request_id;
	}
	
	public ServiceContextList getServiceContextList() {
		return serviceContextList;
	}
	
	public void setServiceContextList(ServiceContextList serviceContextList) {
		this.serviceContextList = serviceContextList;
	}
		
}
