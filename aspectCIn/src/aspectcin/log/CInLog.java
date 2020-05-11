package aspectcin.log;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import aspectcin.orb.ClientProxy;
import aspectcin.orb.PackageHandler;
import aspectcin.orb.communication.api.AnPackage;



@Aspect
public class CInLog {
	
	private static boolean logging = true; 
	
	@Pointcut("call(long aspectcin.orb.PackageHandler.registerStub(ClientProxy)) && args(clientProxy) && target(packageHandler)")
	void logRegisterStub(ClientProxy clientProxy, PackageHandler packageHandler) {		
	}
	 
	@After("logRegisterStub(clientProxy, packageHandler)")
	public void aroundLogRegisterStub(ClientProxy clientProxy, PackageHandler packageHandler) {
	 
		if(logging){
			System.out.println("LOG - PackageHandler registered!");
			System.out.println("LOG - PackageHandler registered " + clientProxy.getClass() + " with stub_id = // " + (packageHandler.getStubCount() - 1));
			System.out.println("LOG - Next stub_id on PackageHandler = " + packageHandler.getStubCount());
		}
	} 
	
	
	@Pointcut("call(void aspectcin.orb.PackageHandler.onReplyArrived(AnPackage))&& args(pkg)")
	void logOnReplyArrived(AnPackage pkg) {
	}

	@Before("logOnReplyArrived(pkg)")
	public void beforeLogOnReplyArrived(AnPackage pkg) {
		if(logging){
			System.out.println("LOG - Reply arrived");
			System.out.println("LOG - \t" + pkg.toString());
		}
	}

	@Pointcut("call(void aspectcin.orb.PackageHandler.onRequestArrived(AnPackage))&& args(pkg)")
	void logOnRequestArrived(AnPackage pkg) {
	}

	@Before("logOnRequestArrived(pkg)")
	public void beforeLogOnRequestArrived(AnPackage pkg) {
		if(logging){
			System.out.println("LOG - Request arrived");
			System.out.println("LOG - \t" + pkg.toString());
		}
	}

}
