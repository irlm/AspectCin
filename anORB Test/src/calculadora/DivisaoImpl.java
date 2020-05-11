package calculadora;

import anorb.AnRemoteException;
import anorb.RemoteObject;

public class DivisaoImpl extends RemoteObject implements Divisao {

	private static final long serialVersionUID = 1L;

	public double dividir(double number1, double number2)
			throws AnRemoteException, DivisaoPorZeroException {
		if (number2 == 0)
			throw new DivisaoPorZeroException();
		return number1 / number2;
	}

}
