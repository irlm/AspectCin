package namingService.cosNaming;

import java.util.HashMap;
import address.Address;


public class NamingService {
	
	protected HashMap hashNamingContext = new HashMap();

	public static void main(String[] args) {
		try {
			
			NamingService namingService = new NamingService();
			NamingServiceTransport namingServiceTransport = new NamingServiceTransport((Address.getConnection("tcp")));
			
			NamingContext_Skel nc = new NamingContext_Skel(namingService.hashNamingContext);
						
			while(true) {
				
				System.out.println("NamingService: Aguardando Solicitações... ");	
				
				Object obj = namingServiceTransport.receiveRequest();
								
				//Recebe uma mensagem
				Object response  = nc.invoke(obj);
				if (response != null) {
					namingServiceTransport.sendReply(response);
				}		    	
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	
}
