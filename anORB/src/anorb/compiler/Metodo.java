package anorb.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class Metodo {
	private String retorno;

	private String nome;

	private List<String> tiposParametros;

	private List<String> nomesParametros;

	private TreeSet<String> excessoes;

	public Metodo(String retorno, String nome) {
		this.nome = nome;
		this.retorno = retorno;
		this.tiposParametros = new ArrayList<String>();
		this.nomesParametros = new ArrayList<String>();
		this.excessoes = new TreeSet<String>();
		excessoes.add("AnRemoteException");
	}

	public void addExcecao(String excecao) {
		excessoes.add(excecao);
	}

	public void addParametro(String parametro) {
		if (!parametro.equals("")) {
			StringTokenizer st = new StringTokenizer(parametro);
			tiposParametros.add(st.nextToken());
			nomesParametros.add(st.nextToken());
		}
	}

	public TreeSet<String> getExcessoes() {
		return excessoes;
	}

	public List<String> getParametros() {
		ArrayList<String> lista = new ArrayList<String>();
		for (int i = 0; i < tiposParametros.size(); i++) {
			lista.add(tiposParametros.get(i) + ' ' + nomesParametros.get(i));
		}
		return lista;
	}

	public String getNome() {
		return nome;
	}

	public String getRetorno() {
		return retorno;
	}

	public String getAssinatura() {
		StringBuilder retorno = new StringBuilder("    public " + this.retorno
				+ ' ' + nome + '(');
		for (String s : getParametros()) {
			retorno.append(s);
			retorno.append(',');
		}
		if (getParametros().size() > 0) {
			retorno.deleteCharAt(retorno.length() - 1);
		}
		retorno.append(") throws ");
		for (String s : getExcessoes()) {
			retorno.append(s);
			retorno.append(',');
		}
		retorno.deleteCharAt(retorno.length() - 1);
		retorno.append('{');
		return retorno.toString();
	}

	public String getFimMetodo() {
		String valor = getValorPadrao();
		if (valor == null) {
			return "    }";
		} else {
			return "        return " + valor + ";\n    }";
		}
	}

	public String getValorPadrao() {
		if (retorno.equals("void")) {
			return null;
		} else if (retorno.equals("boolean")) {
			return "false";
		} else if (isNumeric(retorno)) {
			return "0";
		} else {
			return " null";
		}
	}

	private boolean isNumeric(String retorno2) {
		return retorno2.equals("int") || retorno2.equals("double")
				|| retorno2.equals("float") || retorno2.equals("long")
				|| retorno2.equals("byte") || retorno2.equals("short");
	}

	public List<String> getTiposParametros() {
		return tiposParametros;
	}

	public List<String> getNomesParametros() {
		return nomesParametros;
	}
}
