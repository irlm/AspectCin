package hello;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;

import aspectcin.orb.AnRemoteException;
import aspectcin.orb.ClientProxy;
import aspectcin.orb.communication.api.Reply;

public class HelloStub extends ClientProxy implements Hello{

    private static final long serialVersionUID = 1L;

    public void sayHello() throws AnRemoteException{
        try {
            Class<?>[] parameterTypes = new Class[]{};
            Serializable[] parameters = new Serializable[]{};
            Reply reply = invokeRemoteMethod("sayHello", parameterTypes, parameters);
            if(reply.getReturned() instanceof Throwable){
                Throwable exception = (Throwable)reply.getReturned();
                if(exception instanceof AnRemoteException)
                    throw (AnRemoteException) exception;
            }
        }catch(UnknownHostException e){
            throw new AnRemoteException(e.getMessage());
        }catch(IOException e){
            throw new AnRemoteException(e.getMessage());
        }
    }

    public void sayHelloWithParameter(String msg) throws AnRemoteException,ParametroNuloException{
        try {
            Class<?>[] parameterTypes = new Class[]{String.class};
            Serializable[] parameters = new Serializable[]{msg};
            Reply reply = invokeRemoteMethod("sayHelloWithParameter", parameterTypes, parameters);
            if(reply.getReturned() instanceof Throwable){
                Throwable exception = (Throwable)reply.getReturned();
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
            Class<?>[] parameterTypes = new Class[]{String.class};
            Serializable[] parameters = new Serializable[]{msg};
            Reply reply = invokeRemoteMethod("sayHelloWithEcho", parameterTypes, parameters);
            if(reply.getReturned() instanceof Throwable){
                Throwable exception = (Throwable)reply.getReturned();
                if(exception instanceof AnRemoteException)
                    throw (AnRemoteException) exception;
                if(exception instanceof ParametroNuloException)
                    throw (ParametroNuloException) exception;
                throw new AnRemoteException(exception.getMessage());
            }
            retorno = (String)reply.getReturned();
        }catch(UnknownHostException e){
            throw new AnRemoteException(e.getMessage());
        }catch(IOException e){
            throw new AnRemoteException(e.getMessage());
        }
        return retorno;
    }
    
    public Primitive primitive(Primitive primitive) throws AnRemoteException,ParametroNuloException{
    	Primitive retorno =  null;
        try {
            Class<?>[] parameterTypes = new Class[]{Primitive.class};
            Serializable[] parameters = new Serializable[]{primitive};
            Reply reply = invokeRemoteMethod("primitive", parameterTypes, parameters);
            if(reply.getReturned() instanceof Throwable){
                Throwable exception = (Throwable)reply.getReturned();
                if(exception instanceof AnRemoteException)
                    throw (AnRemoteException) exception;
                if(exception instanceof ParametroNuloException)
                    throw (ParametroNuloException) exception;
                throw new AnRemoteException(exception.getMessage());
            }
            retorno = (Primitive)reply.getReturned();
        }catch(UnknownHostException e){
            throw new AnRemoteException(e.getMessage());
        }catch(IOException e){
            throw new AnRemoteException(e.getMessage());
        }
        return retorno;
    }

}
