package sendReceive2;


public class Servidor {

	public static void main(String[] args) {

		AspectMiddleware aspectMiddleware = new AspectMiddleware();
		Mensagem mensagem = new Mensagem("");
		
		System.out.println("Servidor Rodando!!!");
		aspectMiddleware.receive(mensagem);
		System.out.println("Fim!!!");
	}
}
