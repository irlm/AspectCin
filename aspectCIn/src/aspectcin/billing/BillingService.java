package aspectcin.billing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import aspectcin.orb.RemoteObject;
import aspectcin.orb.communication.api.Request;

@Aspect
public class BillingService {

	private static SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.getDefault());
	
	@Pointcut("call(void aspectcin.orb.ServerProxy.invoke(RemoteObject, Request, Object)) && args(target, request, impl)")
	void invoke(RemoteObject target, Request request, Object impl){}
	
	@Before("invoke(target, request, impl)")
	public void beforeInvoke(RemoteObject target, Request request, Object impl){
		Date date = new Date(System.currentTimeMillis());
		System.out.println( "Host " + target.getHost() + " invoke " + request.getMethod() + " method - " + RFC822DATEFORMAT.format(date));
	}
}
