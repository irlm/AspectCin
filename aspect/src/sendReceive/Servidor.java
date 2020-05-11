package sendReceive;


public class Servidor {

	public static void main(String[] args) {

		Mensagem mensagem = new Mensagem("");
		
		System.out.println("Servidor Rodando!!!");
		Connection connection = new TCPConnection();
		connection.receive(mensagem);
		System.out.println("Mensagem recebida  = " + mensagem.getMensagem());
	}
}
