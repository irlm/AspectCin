package aspc;

import teste.Teste01;

public aspect Aspect01 {

	pointcut print() :
		call (void Teste01.printTeste01())||
		call (void Teste01.printTeste01(String));
	
	before() : print() {
		System.out.println("opa aspect print() before");
	}
	
	after() returning : print() {
		System.out.println("epa aspect print() after");
	}

}
