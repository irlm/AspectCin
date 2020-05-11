package protocol;

import java.io.Serializable;

public class RequestHeader implements Serializable {
	private ServiceContextList serviceContextList;
	private int request_id;
	private boolean response_expected;
	private byte[] object_id;
	private String operation;
	private Principal requesting_principal;
	
	public RequestHeader(ServiceContextList serviceContextList, 
							int request_id, 
							boolean response_expected, 
							byte[] object_id, 
							String operation, 
							Principal requesting_principal) {
		this.serviceContextList = serviceContextList;
		this.request_id = request_id;
		this.response_expected = response_expected;
		this.object_id = object_id;
		this.operation = operation;
		this.requesting_principal = requesting_principal;
	}

	public byte[] getObject_id() {
		return object_id;
	}

	public void setObject_id(byte[] object_id) {
		this.object_id = object_id;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public int getRequest_id() {
		return request_id;
	}

	public void setRequest_id(int request_id) {
		this.request_id = request_id;
	}

	public Principal getRequesting_principal() {
		return requesting_principal;
	}

	public void setRequesting_principal(Principal requesting_principal) {
		this.requesting_principal = requesting_principal;
	}

	public boolean isResponse_expected() {
		return response_expected;
	}

	public void setResponse_expected(boolean response_expected) {
		this.response_expected = response_expected;
	}

	public ServiceContextList getServiceContextList() {
		return serviceContextList;
	}

	public void setServiceContextList(ServiceContextList serviceContextList) {
		this.serviceContextList = serviceContextList;
	}
	
}
