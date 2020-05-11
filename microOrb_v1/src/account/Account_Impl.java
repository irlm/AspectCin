package account;

import proxy.RemoteObject;

public class Account_Impl extends RemoteObject implements Account{
	
	public int add(int a, int b) {
		int result = 0;
		
		result = a + b;
		
		return result;
	}
	
}
