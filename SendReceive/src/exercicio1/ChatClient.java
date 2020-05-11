package exercicio1;

public class ChatClient {

	public enum ConnectionType{TCP, RMI};	
	
	private Connection connection;
	
	public ChatClient(ConnectionType connectionType, String host)
	{
		if (connectionType == ConnectionType.TCP)
		{
			this.connection = new TCPConnection(host);
		}
		else
		{
			this.connection = new RMIConnection(host);
		}
	}
	
	public void send(Message m)
	{
		this.connection.send(m);
	}
	
	public Message receive()
	{
		return this.connection.receive();
	}
	
	public static void main(String args[])
	{
		//ChatClient cliente1 = new ChatClient(ChatClient.ConnectionType.RMI, "localhost");		
		
		//ChatClient cliente2 = new ChatClient(ChatClient.ConnectionType.RMI, "localhost");
		
		ChatClient cliente3 = new ChatClient(ChatClient.ConnectionType.TCP, "localhost");
		
		ChatClient cliente4 = new ChatClient(ChatClient.ConnectionType.TCP, "localhost");
		
		//cliente2.send(new Message("Hello"));
		
		//cliente4.send(new Message("World"));
		
		//Message m = cliente1.receive();
		
		//System.out.println(m.getMessage());
		
		Message m2 = cliente3.receive();
		
		System.out.println("Foi isso -> " + m2.getMessage());
	}
}
