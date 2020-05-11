package account;

import proxy.ObjectImpl;
import proxy.ServerRequest;

public class  Account_Skel extends ObjectImpl implements Account  {
	
	public int add (int a, int b) {	
		return new Account_Impl().add(a, b);
	}
	
	public Object invoke(ServerRequest serverRequest) {
	
		Object response = null;	
		
		String operation = serverRequest.getOperation();
		Object[] parameters = serverRequest.getParameters();
		
		/*
		 * Medi��es: Tempo No Middleware:
		 * Solicitacao dentro do middleware
		 * para realizar a opera��o.
		 * */    	     
		//DataHora fim request/inicio servidor
	    System.out.println(System.nanoTime());
	    
		if("add".equalsIgnoreCase(operation)) {
			response = new Integer((this.add(((Integer)parameters[0]).intValue(), ((Integer)parameters[1]).intValue())));
		}
		
		/*
		 * Medi��es: Tempo No Middleware:
		 * Solicitacao dentro do middleware
		 * para realizar a opera��o.
		 * */    	     
		//DataHora inicio replay/fim servidor
	    System.out.println(System.nanoTime());
	    
		return response;
	}
	
}