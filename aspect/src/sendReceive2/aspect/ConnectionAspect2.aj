package sendReceive2.aspect;

import sendReceive2.RMIConnection;
import sendReceive2.TCPConnection;
import sendReceive2.Mensagem;
import sendReceive2.AspectMiddleware;

public aspect ConnectionAspect2 {

	pointcut connection(AspectMiddleware aspectMiddleware, Mensagem mensagem): target(aspectMiddleware) &&
    	((call(void AspectMiddleware.send(Mensagem))&& args(mensagem)) ||
    	 (call(void AspectMiddleware.receive(Mensagem)) && args(mensagem)) );
	
	before(AspectMiddleware aspectMiddleware, Mensagem mensagem) :
		connection(aspectMiddleware, mensagem) {
			aspectMiddleware.setConnection(new TCPConnection());
			//aspectMiddleware.setConnection(new RMIConnection());
		}
	
}
