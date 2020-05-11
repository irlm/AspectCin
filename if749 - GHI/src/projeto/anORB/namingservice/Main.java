package projeto.anORB.namingservice;

import java.io.IOException;

import projeto.anORB.comunication.Receiver;

public class Main {

	public static void main(String[] args) throws IOException{
		System.out.println("Starting NamingService...");
		NamingServiceSkeleton skeleton = new NamingServiceSkeleton(new NamingServiceImpl());
		Receiver receiver = new Receiver(2179, skeleton);
		receiver.start();
		System.out.println("NamingService started!");
	}
}
