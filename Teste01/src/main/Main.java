package main;

import teste.Teste01;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Teste01 teste01 = new Teste01();
		System.out.println("Inicio");
		teste01.printTeste01();
		System.out.println("Meio");
		teste01.printTeste01("Opa epa");
		System.out.println("fim");
	}

}
    