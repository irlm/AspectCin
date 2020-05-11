package sendReceive2;

public class Cliente {

	public static void main(String[] args) {

		AspectMiddleware aspectMiddleware = new AspectMiddleware();
		Mensagem mensagem = new Mensagem("01010101");
		
		aspectMiddleware.send(mensagem);
		System.out.println("Fim!!!");
	}
}
