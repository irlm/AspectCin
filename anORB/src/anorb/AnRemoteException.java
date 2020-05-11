package anorb;

import java.io.IOException;

public class AnRemoteException extends IOException {

	private static final long serialVersionUID = 1312395624065104733L;

	public AnRemoteException(String msg){
		super(msg);
	}
}
