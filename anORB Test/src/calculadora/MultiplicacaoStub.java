package calculadora;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;

import anorb.AnRemoteException;
import anorb.Stub;
import anorb.comunication.AnPackage;
import anorb.comunication.Reply;
import anorb.comunication.ReplyException;

public class MultiplicacaoStub extends Stub implements Multiplicacao{

    private static final long serialVersionUID = 1L;

    public double multiplicar(double number1,double number2) throws AnRemoteException{
        double retorno = 0;
        try {
            Class[] parameterTypes = new Class[]{double.class,double.class};
            Serializable[] parameters = new Serializable[]{number1,number2};
            AnPackage pkg = invokeRemoteMethod("multiplicar", parameterTypes, parameters);
            if(pkg.getBody() instanceof ReplyException){
                Throwable exception = ((ReplyException) pkg.getBody()).getReturned();
                if(exception instanceof AnRemoteException)
                    throw (AnRemoteException) exception;
                throw new AnRemoteException(exception.getMessage());
            }
            retorno = (Double)((Reply)pkg.getBody()).getReturned();
        }catch(UnknownHostException e){
            throw new AnRemoteException(e.getMessage());
        }catch(IOException e){
            throw new AnRemoteException(e.getMessage());
        }
        return retorno;
    }

}
