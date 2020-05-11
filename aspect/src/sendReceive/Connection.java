package sendReceive;

public interface Connection {
	
	void send(Mensagem mensagem);

	void receive(Mensagem mensagem);

}
