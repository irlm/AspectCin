package exercicio2;

import java.io.Serializable;

public final class Mensagem implements Serializable {

	private static final long serialVersionUID = 3816724861496579602L;

	private final String origem;

	private final String conteudo;

	public Mensagem(String origem, String conteudo) {
		this.origem = origem;
		this.conteudo = conteudo;
	}
		
	public String getConteudo(){
		return conteudo;
	}

	public String getOrigem() {
		return this.origem;
	}
}
