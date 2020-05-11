package projeto.anORB.namingservice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import projeto.anORB.Dispatcher;
import projeto.anORB.Package;
import projeto.anORB.RemoteObject;
import projeto.anORB.Request;
import projeto.anORB.comunication.PackageHandler;

public class NamingServiceSkeleton implements PackageHandler {

	private NamingServiceImpl namingService;

	public NamingServiceSkeleton(NamingServiceImpl impl) {
		namingService = impl;
	}

	public void onPackageArrived(Package receivedPkg) {
		System.out.println(receivedPkg.getSource());
		System.out.println(receivedPkg.getDestiny());
		Request req = (Request) receivedPkg.getBody();

		if (req.getObjectId().equals("NamingService")) {
			if (req.getMetodo().equals("register")) {
				Object[] param = req.getParametros();
				String oid = (String) param[0];
				String classe = (String) param[1];

				RemoteObject ro = new RemoteObject();
				ro.setOid(oid);
				ro.setClassName(classe);
				ro.setHost(receivedPkg.getSource());
				namingService.register(oid, ro);
			} else if (req.getMetodo().equals("lookup")) {
				String name = (String) req.getParametros()[0];

				RemoteObject ro = namingService.lookup(name);
				if (ro != null) {
					Reply reply = new Reply();
					reply.setStubId(req.getStubId());
					reply.setResult(ro.getOid() + "|" + ro.getClassName() + "|" + ro.getHost());
					try {
						Package pkg = new Package(InetAddress.getLocalHost(), receivedPkg.getSource());
						pkg.setBody(reply);
						Dispatcher.getSingleton().send(pkg);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					// TODO
				}

			}
		}
	}


}
