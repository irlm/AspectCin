package exercicio2;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ListenerMiddleware {

	private String nome;

	private InfraEstrutura comunicacao;

	private ConcurrentLinkedQueue<Mensagem> fila;

	private boolean fim;

	private Demultiplexador demux;

	public ListenerMiddleware(String nomeDoServico) {
		this.nome = nomeDoServico;
		this.comunicacao = InfraEstrutura.getSingleton();
		this.fila = new ConcurrentLinkedQueue<Mensagem>();
		this.demux = Demultiplexador.getSingleton();
		this.demux.registrar(nomeDoServico, this);
	}

	public void enviar(String mensagem, String destino) throws IOException {
		comunicacao.send(new MensagemInfra(destino,
				new Mensagem(nome, mensagem)));
	}

	public synchronized void entrarEmLoop() throws InterruptedException {
		while (!fim) {
			for (Mensagem m : fila) {
				this.tratarMensagem(m);
			}
			if (!fim) {
				this.wait();
			}
		}
	}

	public synchronized void pararOLoop() {
		fim = true;
		this.notify();
	}

	public synchronized void fechar() {
		this.fim = true;
		this.demux.desregistrar(this.nome);
	}

	public synchronized void esperarPorPeloMenosUmaMensagem()
			throws InterruptedException {
		boolean recebeu = false;
		while (!recebeu) {
			for (Mensagem m : fila) {
				this.tratarMensagem(m);
				recebeu = true;
			}
			if (!recebeu) {
				this.wait();
			}
		}
	}

	protected abstract void tratarMensagem(Mensagem mensagem);

	void addMensagem(Mensagem mensagem) {
		fila.add(mensagem);
	}	
}
