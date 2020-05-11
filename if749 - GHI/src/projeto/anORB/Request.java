package projeto.anORB;

public class Request extends PackageBody {

	private static final long serialVersionUID = -2263431262587576322L;
	
	private String objectId;
	private String metodo;
	private Object[] parametros;

	public Request(String objectId, String metodo, Object... parametros) {
		this.objectId = objectId;
		this.metodo = metodo;
		this.parametros = parametros;
	}

	public String getMetodo() {
		return metodo;
	}

	public String getObjectId() {
		return objectId;
	}

	public Object[] getParametros() {
		return parametros;
	}
}
