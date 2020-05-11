package calculadora;

import anorb.AnRemoteException;
import anorb.RemoteObject;

public class SubtracaoImpl extends RemoteObject implements Subtracao{

    private static final long serialVersionUID = 1L;

    public double subtrair(double number1,double number2) throws AnRemoteException{
    	return number1-number2;
    }

}
