package exercicio2;

import java.util.Hashtable;

public class Demultiplexador {

	private static Demultiplexador singleton;

	public static Demultiplexador getSingleton() {
		if (singleton == null) {
			singleton = new Demultiplexador();
		}

		return singleton;
	}

	private Hashtable<String, ListenerMiddleware> tabela;

	private Demultiplexador() {
		this.tabela = new Hashtable<String, ListenerMiddleware>();
	}

	public void registrar(String nome, ListenerMiddleware listener) {
		tabela.put(nome, listener);
	}

	public void desregistrar(String nome) {
		tabela.remove(nome);
		if (tabela.isEmpty()) {
			InfraEstrutura.getSingleton().parar();
		}
	}

	public void armazenar(MensagemInfra message) {
		String nomeServico = message.getDestino();
		ListenerMiddleware listenerMiddleware = tabela.get(nomeServico);
		if (listenerMiddleware != null) {
			listenerMiddleware.addMensagem(message.getMensagem());
			synchronized (listenerMiddleware) {
				listenerMiddleware.notify();
			}
		}
	}
}
