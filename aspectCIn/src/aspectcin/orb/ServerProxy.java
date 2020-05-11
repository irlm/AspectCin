package aspectcin.orb;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import aspectcin.orb.communication.api.Reply;
import aspectcin.orb.communication.api.Request;
import aspectcin.orb.communication.api.Sender;

public class ServerProxy {

	public static void invoke(RemoteObject target, Request request, Object impl)
			throws NoSuchMethodException, IllegalAccessException, IOException {
		
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		Method method = impl.getClass().getMethod(request.getMethod(),
				(Class[]) parameterTypes);
		
		Reply reply = null;
		
		try {
			Serializable returned = (Serializable) method.invoke(impl,
					parameters);
			reply = new Reply(returned);
		} catch (InvocationTargetException e) {
			reply = new Reply(e.getTargetException());
		}
		
		reply.setStubId(request.getStubId());		
		Sender.getSingleton().send(target, reply);
	}

}
