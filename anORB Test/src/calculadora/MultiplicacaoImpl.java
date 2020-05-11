package calculadora;

import anorb.AnRemoteException;
import anorb.RemoteObject;

public class MultiplicacaoImpl extends RemoteObject implements Multiplicacao{

    private static final long serialVersionUID = 1L;

    public double multiplicar(double number1,double number2) throws AnRemoteException{
        
    	return number1*number2;
    }

}
