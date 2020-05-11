package sendReceive.aspect;

import sendReceive.Criptografia;
import sendReceive.Mensagem;
import sendReceive.Connection;

public aspect ConnectionAspect {

	pointcut send(Mensagem mensagem):
		call (public void Connection.send(Mensagem))
			&& args(mensagem);
	
	pointcut receive(Mensagem mensagem):
		call (public void receive(Mensagem))
		&& args(mensagem);
		
	
	before(Mensagem mensagem) : 
		send(mensagem) {			
			Criptografia criptografia = new Criptografia();
			String temp = criptografia.encriptar(mensagem);
			mensagem.setMensagem(temp);
		}
	
	after(Mensagem mensagem) returning : 
		receive(mensagem) {
			Criptografia criptografia = new Criptografia();
			String temp = criptografia.decriptar(mensagem);
			mensagem.setMensagem(temp);
		}
	
}
