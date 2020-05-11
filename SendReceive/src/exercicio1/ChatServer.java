package exercicio1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatServer {

	private int id;
	private Vector<Client> clientes;

	public ChatServer() {
		this.clientes = new Vector<Client>();
	}

	public int register() {
		Client c = new Client(id++);
		this.clientes.add(c);
		return c.getId();
	}

	public void publish(Message message, int id) {

		for (Client c : clientes) {
			if (c.getId() != id)
			c.addMessage(message);
		}

		System.out.println("Message pusblished: " + message.getMessage());
	}

	@SuppressWarnings("unchecked")
	public Message getMessage(int id) {
		Message retorno = null;

		int indice = Collections.binarySearch(clientes, new Client(id));

		if (indice >= 0) {
			Client c = clientes.get(indice);
			retorno = c.getMessage();
		}

		return retorno;
	}

	public void initRMICommunicationSupport() {

		System.out.println("Iniciando suporte a comunicacao via RMI...");

		try {
			System.out.println("Iniciando RMI registry...");
			LocateRegistry.createRegistry(1099);
			System.out.println("RMI registry rodando.");
			RMIChatSupportImpl rmiSupportImpl = new RMIChatSupportImpl(this);
			RMIChatSupport rmiChatSupport = (RMIChatSupport) UnicastRemoteObject
					.exportObject(rmiSupportImpl);
			Registry registry = LocateRegistry.getRegistry();
			System.out.println("Registrando objeto remoto: RMIChatSupport");
			registry.bind("RMIChatSupport", rmiChatSupport);
			System.out.println("Suporte a RMI OK!");
		} catch (Exception e) {
			System.out.println("Erro:");
			e.printStackTrace();
		}
	}

	public void initTCPCommunicationSupport() {
		
		try {
			System.out.println("Iniciando suporte a comunicacao via TCP...");
			ServerSocket srvr = new ServerSocket(1234);
			Socket sktS = srvr.accept();
			System.out.print("Suporte a TCP OK!");
			PrintWriter outServer = new PrintWriter(sktS.getOutputStream(), true);
			BufferedReader inServer = new BufferedReader(new InputStreamReader(
					sktS.getInputStream()));
			outServer.print("Opa quem disse epa");
			outServer.close();
			sktS.close();
			srvr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String args[]) {
		ChatServer server = new ChatServer();
		//server.initRMICommunicationSupport();
		server.initTCPCommunicationSupport();
	}

}

class Client implements Comparable {

	private int id;
	private Queue<Message> messages;

	public Client(int id) {
		this.id = id;
		this.messages = new ConcurrentLinkedQueue<Message>();
	}

	public int getId() {
		return this.id;
	}

	public Message getMessage() {
		Message retorno = null;

		if (messages.size() > 0) {
			retorno = messages.remove();
		}

		return retorno;
	}

	public void addMessage(Message message) {
		this.messages.add(message);
	}

	public int compareTo(Object o) {
		return getId() - ((Client) o).getId();
	}
}
