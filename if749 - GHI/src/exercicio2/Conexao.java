package exercicio2;

import java.io.IOException;

public interface Conexao {

	void send(MensagemInfra mensagem) throws IOException;

}
