package anorb.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class AnInterface {

	private String pacote;

	private String nome;

	private ArrayList<Metodo> metodos;

	private TreeSet<String> imports, tempImports;

	public AnInterface() {
		this.pacote = "";
		this.metodos = new ArrayList<Metodo>();
		this.imports = new TreeSet<String>();
		this.tempImports = new TreeSet<String>();
	}

	public void setPackage(String pacote) {
		this.pacote = pacote;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void addMetodo(Metodo m) {
		metodos.add(m);
	}

	public ArrayList<Metodo> getMetodos() {
		return metodos;
	}

	public String getNome() {
		return nome;
	}

	public String getPacote() {
		return pacote;
	}

	public void addImport(String impo) {
		imports.add(impo);
	}
	
	public void addTempImport(String impo) {
		tempImports.add(impo);
	}
	
	public void clearImports() {
		tempImports.clear();
	}

	public List<String> getImports() {
		ArrayList<String> lista = new ArrayList<String>();
		for (String impor : imports) {
			lista.add("import " + impor + ';');
		}
		for (String impor : tempImports) {
			lista.add("import " + impor + ';');
		}
		return lista;
	}

}
