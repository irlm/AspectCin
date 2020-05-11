package exercicio4;

import java.io.IOException;


public abstract class ServiceHandler implements Runnable {

	protected SOCKStream stream;

	public ServiceHandler() {
	}

	public void setHandle(SOCKStream s) throws IOException {
		this.stream = s;
	}

	public SOCKStream peer() {
		return this.stream;
	}

	public abstract int open(Object obj);

}
