package hello;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;

import anorb.AnRemoteException;
import anorb.Stub;
import anorb.comunication.AnPackage;
import anorb.comunication.Reply;
import anorb.comunication.ReplyException;

public class HelloStub extends Stub implements Hello{

    private static final long serialVersionUID = 1L;

    public void sayHello() throws AnRemoteException{
        try {
            Class[] parameterTypes = new Class[]{};
            Serializable[] parameters = new Serializable[]{};
            AnPackage pkg = invokeRemoteMethod("sayHello", parameterTypes, parameters);
            if(pkg.getBody() instanceof ReplyException){
                Throwable exception = ((ReplyException) pkg.getBody()).getReturned();
                if(exception instanceof AnRemoteException)
                    throw (AnRemoteException) exception;
                throw new AnRemoteException(exception.getMessage());
            }
        }catch(UnknownHostException e){
            throw new AnRemoteException(e.getMessage());
        }catch(IOException e){
            throw new AnRemoteException(e.getMessage());
        }
    }

    public void sayHelloWithParameter(String msg) throws AnRemoteException,ParametroNuloException{
        try {
            Class[] parameterTypes = new Class[]{String.class};
            Serializable[] parameters = new Serializable[]{msg};
            AnPackage pkg = invokeRemoteMethod("sayHelloWithParameter", parameterTypes, parameters);
            if(pkg.getBody() instanceof ReplyException){
                Throwable exception = ((ReplyException) pkg.getBody()).getReturned();
                if(exception instanceof AnRemoteException)
                    throw (AnRemoteException) exception;
                if(exception instanceof ParametroNuloException)
                    throw (ParametroNuloException) exception;
                throw new AnRemoteException(exception.getMessage());
            }
        }catch(UnknownHostException e){
            throw new AnRemoteException(e.getMessage());
        }catch(IOException e){
            throw new AnRemoteException(e.getMessage());
        }
    }

    public String sayHelloWithEcho(String msg) throws AnRemoteException,ParametroNuloException{
        String retorno =  null;
        try {
            Class[] parameterTypes = new Class[]{String.class};
            Serializable[] parameters = new Serializable[]{msg};
            AnPackage pkg = invokeRemoteMethod("sayHelloWithEcho", parameterTypes, parameters);
            if(pkg.getBody() instanceof ReplyException){
                Throwable exception = ((ReplyException) pkg.getBody()).getReturned();
                if(exception instanceof AnRemoteException)
                    throw (AnRemoteException) exception;
                if(exception instanceof ParametroNuloException)
                    throw (ParametroNuloException) exception;
                throw new AnRemoteException(exception.getMessage());
            }
            retorno = (String)((Reply)pkg.getBody()).getReturned();
        }catch(UnknownHostException e){
            throw new AnRemoteException(e.getMessage());
        }catch(IOException e){
            throw new AnRemoteException(e.getMessage());
        }
        return retorno;
    }

}
