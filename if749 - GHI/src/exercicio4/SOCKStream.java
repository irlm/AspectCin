package exercicio4;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SOCKStream {

	private Socket socket;

	private DataInputStream iStream;

	private DataOutputStream oStream;

	public SOCKStream() {
	}

	public SOCKStream(Socket s) throws IOException {
		this.socket(s);
	}

	public void socket(Socket s) throws IOException {
		this.socket = s;

		this.iStream = new DataInputStream(new BufferedInputStream(s
				.getInputStream()));

		this.oStream = new DataOutputStream(new BufferedOutputStream(s
				.getOutputStream()));
	}

	public Socket socket() {
		return this.socket;
	}

	public void close() throws IOException {
		if (this.socket != null)
			this.socket.close();
		this.socket = null;
	}


	public int send(StringBuffer s) throws IOException {

		String buf = s.toString();

		this.oStream.writeChars(buf.toString());
		this.oStream.writeChar('\n');
		this.oStream.flush();

		return buf.length();
	}

	public int send(String s) throws IOException {
		this.oStream.writeChars(s);
		this.oStream.writeChar('\n');

		this.oStream.flush();

		return s.length();
	}


	public int sendN(byte[] b, int offset, int length) throws IOException {
		this.oStream.write(b, offset, length);
		this.oStream.flush();
		return length;
	}


	public int recv(StringBuffer s) throws IOException {
		int len = 0;
		char in = (char) this.iStream.readByte();

		while (in != '\n') {
			s.append(in);
			in = (char) this.iStream.readByte();
			len++;
		}

		return len;
	}


	public int recvN(byte[] b, int offset, int n) throws IOException {
		this.iStream.readFully(b, offset, n);
		return n;
	}


	public void inputStream(InputStream iStream) {
		this.iStream = new DataInputStream(new BufferedInputStream(iStream));
	}

	public InputStream inputStream() {
		return this.iStream;
	}


	public void outputStream(OutputStream oStream) {
		this.oStream = new DataOutputStream(new BufferedOutputStream(oStream));
	}


	public OutputStream outputStream() {
		return this.oStream;
	}


	public DataOutputStream dataOutputStream() {
		return this.oStream;
	}


	public DataInputStream dataInputStream() {
		return this.iStream;
	}


	protected void finalize() throws Throwable {
		super.finalize();
		this.close();
	}

}
