package exercicio2;


public class Cliente {
	public static void main(String[] args) throws Exception {
		ListenerImpressor l = new ListenerImpressor("C1");
		l.enviar("FIM", "C2");
		l.esperarPorPeloMenosUmaMensagem();
		l.fechar();
	}
}
