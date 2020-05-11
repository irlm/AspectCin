package sendReceive;

public class Cliente {

	public static void main(String[] args) {
		
		Mensagem mensagem =  new Mensagem("01010101");
		
		Connection connection = new TCPConnection();
		
		System.out.println("Mensagem enviada = " + mensagem.getMensagem());
		connection.send(mensagem);
	}
}
