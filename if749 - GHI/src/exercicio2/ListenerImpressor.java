package exercicio2;

/*
 * Created on 20/10/2005
 */
public class ListenerImpressor extends ListenerMiddleware {

	public ListenerImpressor(String nomeDoServico) {
		super(nomeDoServico);
	}

	@Override
	public void tratarMensagem(Mensagem mensagem) {
		System.out.println(mensagem.getConteudo());
	}


}
