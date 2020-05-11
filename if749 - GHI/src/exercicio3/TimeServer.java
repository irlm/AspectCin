package exercicio3;

public class TimeServer extends ComponentServer {

	public long getTime() {
		return System.currentTimeMillis();
	}

	@Override
	protected String getNomeServico() {
		return "TimeServer";
	}

	@Override
	protected String getMessage() {
		return Long.toString(getTime());
	}

}
