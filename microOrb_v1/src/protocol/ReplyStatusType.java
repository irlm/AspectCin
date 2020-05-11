package protocol;

import java.io.Serializable;

public class ReplyStatusType implements Serializable {

	public static String NO_EXCEPTION = "NO_EXCEPTION";
	public static String USER_EXCEPTION = "USER_EXCEPTION";
	
	private String status;

	public ReplyStatusType(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
