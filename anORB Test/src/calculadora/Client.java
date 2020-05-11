package calculadora;

import anorb.AnORB;
import anorb.logging.AnLog;
import anorb.namingservice.NamingService;

public class Client {

	public static void main(String[] args) throws Exception {

		AnLog.monitorFine();

		System.out.println("Cliente - Porta 2198");
		AnORB anORB = AnORB.init(2198, "172.17.65.15", 2178);

		NamingService ns = anORB.getNamingService();

		Soma soma = (Soma) ns.lookup("Soma");

		Subtracao subtracao = (Subtracao) ns.lookup("Subtracao");
		Multiplicacao multiplicacao = (Multiplicacao) ns
				.lookup("Multiplicacao");
		Divisao divisao = (Divisao) ns.lookup("Divisao");
		System.out.println(subtracao.subtrair(soma.somar(5, 3), 2));
		System.out.println(multiplicacao.multiplicar(divisao.dividir(8, 2), 3));
		try {
			divisao.dividir(0, 0);
			System.err.println("DIVIDIU POR ZERO... Como?");
		} catch (DivisaoPorZeroException e) {
			System.out.println(e.getMessage());
		}
		AnORB.destroy();
	}
}
