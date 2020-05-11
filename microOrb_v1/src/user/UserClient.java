package user;

import java.util.Properties;

import namingService.cosNaming.NameComponent;
import namingService.cosNaming.NamingContext;
import orb.ORB;
import account.Account;
import account.Account_Stub;

public class UserClient {

	public static void main(String[] args) {
		try {			
			ORB orb = new ORB("Client");	    
			orb.init();
			
//			Properties para Referencia Inicial ao Contexto do Serviço de Nomes 
			Properties p = System.getProperties();
		    p.put("port", "1030");
		    p.put("host", "172.17.105.3");
		    
			/*
			 * Medições: Tempo Cliente/Cliente:
			 * Solicitacao do Serviço pelo serviço de nomes -> solicitacao ao servidor
			 * para realizar a operação.
			 * */
		     
			//DataHora solicitacao
		    //System.out.println(System.currentTimeMillis());
		    
			NamingContext namingContext = orb.getInitialNamingContext(p);	
			NameComponent[] n = {new NameComponent("Account", "")};
			Account account = new Account_Stub(namingContext.resolve(n));			
				
			int result = account.add(1, 201);
			
			//DataHora retorno
			//System.out.println(System.currentTimeMillis());
			
			System.out.println("Resultado: " + result);	
			
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}

}