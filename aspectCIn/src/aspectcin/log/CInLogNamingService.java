package aspectcin.log;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import aspectcin.namingservice.NamingServiceImpl;
import aspectcin.orb.RemoteObject;

@Aspect
public class CInLogNamingService {

	private static boolean logging = true; 
	

	@Pointcut("call(aspectcin.orb.RemoteObject aspectcin.namingservice.NamingService.lookup(String)) && args(name)")
	void logLookup(String name) {
	}

	@Before("logLookup(name)")
	public void beforeLogStartingNamingService(String name) {
		if(logging)
			System.out.println("LOG - Stub registered (" + name + ")");
	}
	
	@Pointcut("execution(void aspectcin.namingservice.NamingServiceImpl.register(String, RemoteObject)) && args(name, object)")
	void logRegister(String name, RemoteObject object) {
	}

	@Before("logRegister(name, object)")
	public void beforelogRegisterNamingService(String name, RemoteObject object) {
		if(logging)
			System.out.println("LOG - Registering " + name);
	}
	
	@After("logRegister(name, object)")
	public void afterlogRegisterNamingService(String name, RemoteObject object) {
		if(logging)
			System.out.println("LOG - " + name + " registered");
	}
	
	
	@Pointcut("execution(String[] aspectcin.namingservice.NamingServiceImpl.list()) && target(namingServiceImpl)")
	void logList(NamingServiceImpl namingServiceImpl) {
	}

	@Before("logList(namingServiceImpl)")
	public void beforelogList(NamingServiceImpl namingServiceImpl) {
		if(logging)
			System.out.println("LOG - Requested list");
	}
	
	@Pointcut("execution(RemoteObject aspectcin.namingservice.NamingServiceImpl.lookup(String)) && args(name)")
	void logLookupImpl(String name) {
	}

	@Before("logLookupImpl(name)")
	public void beforelogLookup(String name) {
		if(logging)
			System.out.println("LOG - Searching for " + name);
	}
	

}
