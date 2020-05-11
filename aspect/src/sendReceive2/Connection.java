package sendReceive2;

public interface Connection {
	
	void send(Mensagem mensagem);

	void receive(Mensagem mensagem);

}
