package protocol;

import java.io.Serializable;
import java.util.List;

public class ServiceContextList implements Serializable {
	
	private List<ServiceContextList> serviceContextList;

	public List<ServiceContextList> getServiceContextList() {
		return serviceContextList;
	}

	public void setServiceContextList(List<ServiceContextList> serviceContextList) {
		this.serviceContextList = serviceContextList;
	}

}
