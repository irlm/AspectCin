package exercicio3;

/*
 * Created on 02/11/2005
 */
public class DiffTimeServer extends ComponentServer {
	private ComponentRepository componentRepository;

	private long lastTime;

	@Override
	public boolean init(ComponentRepository repository, Object... parameters) {
		this.componentRepository = repository;
		TimeServer timeServer = getTimeServer();
		if (timeServer == null) {
			lastTime = 0;
		} else {
			this.lastTime = timeServer.getTime();
		}
		return super.init(repository, parameters);
	}

	private TimeServer getTimeServer() {
		return ((TimeServer) componentRepository
				.getComponent("exercicio3.TimeServer"));
	}

	@Override
	protected String getNomeServico() {
		return "DiffTimeServer";
	}

	@Override
	protected String getMessage() {
		long temp = lastTime;
		TimeServer timeServer = getTimeServer();
		if (timeServer == null) {
			return "Cannot locate a required component: \"exercicio3.TimeServer\"";
		} else {
			lastTime = timeServer.getTime();
			return Long.toString(lastTime - temp);
		}
	}

}
