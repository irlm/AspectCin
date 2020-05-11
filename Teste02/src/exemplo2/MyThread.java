package exemplo2;

public class MyThread extends Thread {

	public MyThread() {
	}
	
	public void run() {
		while (!ComunicacaoImpl.isRecebeu()) {
			try {
				sleep(1000);
			} catch (InterruptedException e) {}			
		}
	}
}
