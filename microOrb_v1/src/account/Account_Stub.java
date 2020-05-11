package account;

import ior.IOR;
import proxy.ObjectProxy;
import proxy.Request;

public class Account_Stub extends ObjectProxy implements Account {
	
	private IOR objectReference;
	
	public Account_Stub(Object ior) {
		objectReference = (IOR)ior;
	}
	
	public int add (int a, int b) {
		/*
		 * Medi��es: Tempo No Middleware:
		 * Solicitacao dentro do middleware
		 * para realizar a opera��o.
		 * */	     
		//DataHora inicio request
	    System.out.println(System.nanoTime());
		
		Object[] parameters = new Object[100];
		
		// Cria o request
		Request request = this.getInstanceRequest();
		
		// Seta a opera��o a ser chamada
		String operation = "add";
		
		// Seta os par�metros da opera��o
		parameters[0] = a;
		parameters[1] = b;			
		//
		request.sendRequest(this.objectReference, operation, parameters);
		
		// recebe a resposta 
		int responseForOperation = ((Integer)request.receiveReply()).intValue();
		
		//DataHora fim replay
	    System.out.println(System.nanoTime());
	    
		//Retorna a resposta da Opera��o
		return responseForOperation;
	}

}
