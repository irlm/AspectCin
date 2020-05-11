package sss;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		double opa = 105;
		double porcentagem = 0.00598;
		
		int limite = 16 * 12;
		
		double total = 0;
		
		for(int i = 0; i < limite; i++){
			total += opa + (total*porcentagem);
		}
		
		System.out.println("total = " + total);
	}

}
