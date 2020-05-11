package exercicio2;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class ConexaoTCP implements Conexao {

	private String host;

	private int porta;

	public ConexaoTCP(String host, int porta) {
		this.host = host;
		this.porta = porta;
	}

	public void send(MensagemInfra mensagem) throws IOException {
		Socket s = new Socket(host, porta);
		PrintStream out = new PrintStream(s.getOutputStream());
		out.println(mensagem.getDestino() + '|'
				+ mensagem.getMensagem().getOrigem() + '|'
				+ mensagem.getMensagem().getConteudo());
		out.close();
		s.close();
	}

}
