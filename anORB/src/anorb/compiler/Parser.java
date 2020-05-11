package anorb.compiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Parser {
	private AnInterface classe;

	public void parse(String arquivo) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(arquivo));
		this.classe = new AnInterface();
		while (br.ready()) {
			readLine(br.readLine());
		}
	}

	public AnInterface getParsedInterface() {
		return classe;
	}

	private void readLine(String s) {
		if (s.startsWith("import")) {
			String importado = s.substring("import".length()).trim();
			classe.addImport(importado.substring(0,importado.length()-1));
		} else if (s.startsWith("package")) {
			String pacote = s.substring("package ".length()).trim();
			pacote = pacote.substring(0, pacote.length() - 1);
			this.classe.setPackage(pacote);
		} else {
			if (s.startsWith("public")) {
				s = s.substring("public".length());
			}
			if (s.startsWith("abstract")) {
				s = s.substring("abstract".length());
			}
			s = s.trim();
			if (s.startsWith("interface")) {
				int inicio = "interface ".length();
				String nome = s.substring(inicio, s.indexOf(' ', inicio));
				classe.setNome(nome);
			} else if (!s.equals("") && !s.equals("}") && !s.equals("{")) {
				StringTokenizer st = new StringTokenizer(s);
				String retorno = st.nextToken();
				s = s.substring(retorno.length());
				String nomeMetodo = st.nextToken();
				nomeMetodo = nomeMetodo.substring(0, nomeMetodo.indexOf('('));
				Metodo m = new Metodo(retorno, nomeMetodo);
				String[] parametros = s.substring(s.indexOf('(') + 1,
						s.indexOf(')')).split(",");
				for (String p : parametros) {
					m.addParametro(p.trim());
				}
				String excessao = s.substring(s.indexOf(')') + 1).trim();
				if (excessao.startsWith("throws")) {
					excessao = excessao.substring("throws".length(),
							excessao.length() - 1).trim();
					String[] excessoes = excessao.split(",");
					for (String e : excessoes) {
						m.addExcecao(e.trim());
					}
				}
				classe.addMetodo(m);
			}
		}
	}
}
