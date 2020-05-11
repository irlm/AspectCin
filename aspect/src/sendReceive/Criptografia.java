package sendReceive;

public class Criptografia {

	public String encriptar(Mensagem mensagem){
		String ret = "";
		
		ret = zeroUmUmZero(mensagem.getMensagem());
		
		return ret;
	}
	
	public String decriptar(Mensagem mensagem){
		String ret = "";
		
		ret = zeroUmUmZero(mensagem.getMensagem());
		
		return ret;
	}
	
	private String zeroUmUmZero(String mensagem) {
		String ret = "";
		
		for (int i = 0; i < mensagem.length(); i++) {
			
			char c = mensagem.charAt(i);
			switch (c) {
				case '0':
					c = '1';
					break;
				case '1':
					c = '0';
					break;
			}
			
			ret += c;
		}
		
		return ret;
	}
}
