package aspectcin.namingservice;

import java.io.IOException;
import java.net.BindException;

import aspectcin.orb.PackageHandler;
import aspectcin.util.Configuration;

public class NamingServiceMain {

	private int port;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void Starting() throws IOException {
		NamingServiceImpl impl = new NamingServiceImpl();
		PackageHandler packageHandler = PackageHandler.getSingleton();
		packageHandler.registerRemoteObject("NamingService", impl);
	}

	public static void main(String[] args) {
		NamingServiceMain serviceMain = new NamingServiceMain();
		try {

			if (args.length == 1) {
				try {
					Integer tempPort = new Integer(args[0]);
					serviceMain.setPort(tempPort);
				} catch (Exception e) {
					serviceMain.setPort(Configuration.getInstance().port());
				}
			} else {
				serviceMain.setPort(Configuration.getInstance().port());
			}

			System.out.println("Starting NamingService...");
			serviceMain.Starting();
			System.out.println("NamingService started!");
		} catch (Exception e) {
			if (e instanceof BindException) {
				System.out.println("Naming Service has been already started!");
			}
		}
	}
}
