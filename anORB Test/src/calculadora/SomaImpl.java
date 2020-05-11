package calculadora;

import anorb.AnRemoteException;
import anorb.RemoteObject;

public class SomaImpl extends RemoteObject implements Soma{

    private static final long serialVersionUID = 1L;

    public double somar(double number1,double number2) throws AnRemoteException{
        return number1+number2;
    }

}
