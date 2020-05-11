

import java.io.Serializable;


public class Request extends PackageBody {

	private static final long serialVersionUID = -2263431262587576322L;

	private String oid;

	private String method;

	private Serializable[] parameters;

	private Class[] parameterTypes;

	public Request(long stubId, String objectId, String method,
			Class[] paramiterTypes, Serializable[] parameters) {
		this.setStubId(stubId);
		this.oid = objectId;
		this.method = method;
		this.parameters = parameters;
		this.parameterTypes = paramiterTypes;
	}

	public String getMethod() {
		return method;
	}

	public String getOId() {
		return oid;
	}

	public Serializable[] getParameters() {
		return parameters;
	}

	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Serializable par : parameters) {
			sb.append(par);
		}
		return "Request: " + oid + "." + method + "(" + sb.toString() + ")";
	}
}
