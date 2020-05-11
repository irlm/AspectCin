package exercicio2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class InfraEstrutura {

	private static InfraEstrutura singleton;

	public static InfraEstrutura getSingleton() {
		if (singleton == null) {
			singleton = new InfraEstrutura();
		}
		return singleton;
	}

	private Properties configuracoes;

	private Conexao conexao;

	private SuporteTCP suporteTCP;

	private InfraEstrutura() {
		this.configuracoes = new Properties();
		try {
			FileInputStream fis = new FileInputStream("config.xml");
			configuracoes.loadFromXML(fis);
			fis.close();
		} catch (IOException e) {
			System.err.println("Carregando configuração padrão");
			configuracoes.clear();
			configuracoes.setProperty("targetHost", "localhost");
			configuracoes.setProperty("port", "12346");
			configuracoes.setProperty("targetPort", "12347");
			try {
				FileOutputStream fos = new FileOutputStream("config.xml");
				configuracoes.storeToXML(fos, "Configurações");
				fos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		initSuporteTCP();
		this.conexao = new ConexaoTCP(configuracoes.getProperty("targetHost"),
				Integer.parseInt(configuracoes.getProperty("targetPort")));
	}

	private void initSuporteTCP() {
		if (this.suporteTCP == null) {
			try {
				this.suporteTCP = new SuporteTCP(Integer.parseInt(configuracoes
						.getProperty("port")));
				suporteTCP.comecar();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void send(MensagemInfra mensagem) throws IOException {
		conexao.send(mensagem);
	}

	public void parar() {
		this.suporteTCP.parar();
	}

}