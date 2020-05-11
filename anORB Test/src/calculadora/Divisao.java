package calculadora;

import anorb.AnRemoteException;

public interface Divisao {

	double dividir(double number1, double number2) throws AnRemoteException, DivisaoPorZeroException;
}
