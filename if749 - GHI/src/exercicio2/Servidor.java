package exercicio2;

import java.io.IOException;

public class Servidor extends ListenerMiddleware {

	public Servidor(String nomeDoServico) {
		super(nomeDoServico);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ListenerMiddleware c2 = new Servidor("C2");
		System.out.println("Esperando pra receber e enviar mensagem");
		c2.entrarEmLoop();
		c2.fechar();
	}

	@Override
	public void tratarMensagem(Mensagem mensagem) {
		try {
			this.enviar("Do servidor: Voce enviou esta mensagem para mim: "
					+ mensagem.getConteudo(), mensagem.getOrigem());
			if (mensagem.getConteudo().equals("FIM")) {
				System.out.println("Recebi a ordem para parar");
				this.pararOLoop();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
