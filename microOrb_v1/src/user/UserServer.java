package user;

import java.util.Properties;

import namingService.cosNaming.NameComponent;
import namingService.cosNaming.NamingContext;

import orb.ORB;
import account.Account_Skel;

public class UserServer {

	public static void main(String[] args) {
		try {
					    
			ORB orb = new ORB("Server");		
			orb.init();
	
//			Properties para Referencia Inicial ao Contexto do Serviço de Nomes 
			Properties p = System.getProperties();
		    p.put("port", "1030");
		    p.put("host", "172.17.105.3");
		    
			NamingContext namingContext = orb.getInitialNamingContext(p);	
			NameComponent[] n = {new NameComponent("Account", "")};			
			namingContext.bind(n, orb.getManagerObjectReference().createObjectsReference(new Account_Skel()));				
			
			System.out.println("Serviço Inicializado...");
			orb.run();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}