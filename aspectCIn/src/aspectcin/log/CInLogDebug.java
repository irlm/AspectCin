package aspectcin.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class CInLogDebug {

	//&& !within(Configuration) && !within(ConstantesLoader) é para nao gerar esse erro:
	//Exception in thread "main" org.aspectj.lang.NoAspectBoundException:
	@Pointcut("(execution(* *.*(..)) || execution(*.new(..))) " +
			"&& !within(aspectcin.log.*) " +
			"&& !within(aspectcin.util.Configuration) " +
			"&& !within(aspectcin.util.ConstantesLoader)")
	void traceMethods(JoinPoint jp){
	}
	
    @Before("traceMethods(jp)")
    public void beforeTraceMethods(JoinPoint jp) {

        Signature sig = jp.getSignature();     

	        System.out.println("LOG - Entering ["
	        					+ sig.getDeclaringType().getName() + "."  
	        					+ sig.getName() + "]");                  
    }
}
