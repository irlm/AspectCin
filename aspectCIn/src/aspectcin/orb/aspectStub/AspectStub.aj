package aspectcin.orb.aspectStub;



import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.UnknownHostException;

import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import aspectcin.namingservice.NamingServiceStub;
import aspectcin.orb.AnRemoteException;
import aspectcin.orb.ClientProxy;
import aspectcin.orb.communication.api.Reply;



public aspect AspectStub {
	
	pointcut withinMyClass(ClientProxy clientProxy) : within(ClientProxy +) &&
					! within(ClientProxy) &&
					//! within(NamingServiceStub) &&
					execution(* *.*(..)) &&
					target(clientProxy);
		
	Object around(ClientProxy clientProxy) throws AnRemoteException : withinMyClass(clientProxy) && !within(AspectStub +)
	{
		Object retorno = null;
		
		Signature signature = thisJoinPoint.getStaticPart().getSignature();

		if (signature instanceof MethodSignature ) {
			MethodSignature  methodSignature = (MethodSignature ) signature;
			
			Method method = methodSignature.getMethod();
						
	        try {
	            Class<?>[] parameterTypes = method.getParameterTypes();
	           
	            Object[] args = thisJoinPoint.getArgs();
	            Serializable[] parameters = null;

	            if(args.length > 0){
	            	parameters = new Serializable[args.length];
	            	for (int i = 0; i < args.length; i++) {
	            		parameters[i] = ((Serializable)parameterTypes[i].cast(args[i]));
					}

	            }else{
	            	parameters = new Serializable[]{};
	            }
	            
	            Reply reply = clientProxy.invokeRemoteMethod(method.getName(), parameterTypes, parameters);
	            
	            if(reply.getReturned() instanceof Throwable){
	                
	            	Throwable exception = (Throwable)reply.getReturned();
	                Class<?>[] exceptions = method.getExceptionTypes();
	                for (int i = 0; i < exceptions.length; i++) {
	                	Class temp = exceptions[i];	                	
	                	if(exception.getClass().getName().equals(temp.getName())){
	                		throw new AnRemoteException((Throwable)temp.cast(exception));
	                	}
					}
	                throw new AnRemoteException(exception.getMessage());
	            }else{
	            	retorno = method.getReturnType().cast(reply.getReturned());
	            }	            	         
	        }catch(UnknownHostException e){
	        	throw new AnRemoteException(e.getMessage());
	        }catch(IOException e){
	        	throw new AnRemoteException(e.getMessage());
	        }
		}
		
		return retorno;
	}


}
