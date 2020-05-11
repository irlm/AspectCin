package calculadora;


public class DivisaoPorZeroException extends Exception {
	private static final long serialVersionUID = -5578741816754900819L;

	public DivisaoPorZeroException() {
		super("Divisao por zero");
	}
}
