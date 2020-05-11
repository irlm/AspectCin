package opa;

import java.net.ServerSocket;
import java.net.Socket;

public class UppercaseServer {
    public static void main(String[] args) throws Exception {
      if (args.length != 1) {
          System.out.println("Usage: java UppercaseServer <portNum>");
          System.exit(1);
      }
      int portNum = Integer.parseInt(args[0]);
      ServerSocket serverSocket = new ServerSocket(portNum);
        
        while(true) {
            Socket requestSocket = serverSocket.accept();
            Thread serverThread 
                = new Thread(new UppercaseWorker(requestSocket));
            serverThread.start();
        }
    }
}