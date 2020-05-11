package sendReceive2;


public class AspectMiddleware{

	private Connection connection;	
	
	public void receive(Mensagem mensagem) {
		this.connection.receive(mensagem);		
	}

	public void send(Mensagem mensagem) {
		this.connection.send(mensagem);		
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}	
}
