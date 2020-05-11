package exemplo3;

import java.io.Serializable;

public class Mensagem implements Serializable {

	private static final long serialVersionUID = 4459089200897950638L;
	
	private String mensagem;

	public Mensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String message) {
		this.mensagem = message;
	}	
}
