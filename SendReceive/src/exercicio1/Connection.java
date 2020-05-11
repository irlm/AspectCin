package exercicio1;

public interface Connection {
	
	void send(Message message);

	Message receive();

}
