package exercicio2;

import java.io.Serializable;

public final class MensagemInfra implements Serializable {

	private static final long serialVersionUID = -3034912576323466225L;

	private final String destino;

	private final Mensagem mensagem;

	public MensagemInfra(String destino, Mensagem mensagem) {
		this.destino = destino;
		this.mensagem = mensagem;
	}

	public String getDestino() {
		return destino;
	}

	public Mensagem getMensagem() {
		return mensagem;
	}

}
