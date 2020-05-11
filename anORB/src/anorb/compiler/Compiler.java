package anorb.compiler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import anorb.logging.AnLog;

public class Compiler {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// args = new String[] { "test/anorb/compiler/interfaces/Simple.java",
		// "test/" };
		if (args.length < 1) {
			System.out.println("Usage: Compiler java-file [output_dir]");
			return;
		} else if (args.length == 1) {
			args = new String[] { args[0], "" };
		}
		AnLog.monitorCompilation();
		Parser p = new Parser();
		p.parse(args[0]);
		AnInterface interfac = p.getParsedInterface();
		String nomeInterface = args[1]
				+ interfac.getPacote().replaceAll("\\.", "/");
		if (!nomeInterface.equals("") && !nomeInterface.endsWith("/")) {
			nomeInterface += '/';
		}
		nomeInterface += interfac.getNome();
		impl(interfac, nomeInterface);
		interfac.clearImports();
		stub(interfac, nomeInterface);
	}

	private static void impl(AnInterface interfac, String nomeInterface)
			throws FileNotFoundException {
		interfac.addImport("anorb.RemoteObject");
		interfac.addImport("anorb.AnRemoteException");
		String nomeArquivo = nomeInterface + "Impl.java";
		PrintStream out = new PrintStream(new FileOutputStream(nomeArquivo));
		out.println("package " + interfac.getPacote() + ';');
		out.println();
		for (String s : interfac.getImports()) {
			out.println(s);
		}
		out.println();
		out.println("public class " + interfac.getNome()
				+ "Impl extends RemoteObject implements " + interfac.getNome()
				+ "{");
		out.println();
		out.println("    private static final long serialVersionUID = 1L;");
		for (Metodo m : interfac.getMetodos()) {
			out.println();
			out.println(m.getAssinatura());
			out.println("        //TODO Implement method");
			out.println(m.getFimMetodo());
		}
		out.println();
		out.println("}");
		out.close();
	}

	private static void stub(AnInterface interfac, String nomeInterface)
			throws FileNotFoundException {
		interfac.addImport("anorb.RemoteObject");
		interfac.addImport("java.io.Serializable");
		interfac.addImport("java.io.IOException");
		interfac.addImport("java.net.UnknownHostException");
		interfac.addImport("anorb.AnRemoteException");
		interfac.addImport("anorb.comunication.*");
		interfac.addImport("anorb.Stub");
		String nomeArquivo = nomeInterface + "Stub.java";
		PrintStream out = new PrintStream(new FileOutputStream(nomeArquivo));
		out.println("package " + interfac.getPacote() + ';');
		out.println();

		for (String s : interfac.getImports()) {
			out.println(s);
		}
		out.println();
		out.println("public class " + interfac.getNome()
				+ "Stub extends Stub implements " + interfac.getNome() + "{");
		out.println();
		out.println("    private static final long serialVersionUID = 1L;");
		for (Metodo m : interfac.getMetodos()) {
			out.println();
			out.println(m.getAssinatura());
			String valorPadrao = m.getValorPadrao();
			String retorno = m.getRetorno();
			if (valorPadrao != null) {
				out.println("        " + retorno + " retorno = " + valorPadrao
						+ ";");
			}
			out.println("        try {");
			getCorpoMetodoStub(out, m);
			out.println("        }catch(UnknownHostException e){");
			out
					.println("            throw new AnRemoteException(e.getMessage());");
			out.println("        }catch(IOException e){");
			out
					.println("            throw new AnRemoteException(e.getMessage());");
			out.println("        }");
			if (valorPadrao != null) {
				out.println("        return retorno;");
			}
			out.println("    }");
		}
		out.println();
		out.println("}");
		out.close();
	}

	private static void getCorpoMetodoStub(PrintStream out, Metodo m) {
		String valorPadrao = m.getValorPadrao();
		String retorno = m.getRetorno();
		out.print("            Class[] parameterTypes = new Class[]{");
		StringBuilder sb = new StringBuilder();
		if (m.getTiposParametros().size() > 0) {
			for (String tipoParametro : m.getTiposParametros()) {
				sb.append(tipoParametro);
				sb.append(".class,");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		out.print(sb.toString());
		sb.setLength(0);
		out.println("};");
		out
				.print("            Serializable[] parameters = new Serializable[]{");
		if (m.getNomesParametros().size() > 0) {
			for (String nome : m.getNomesParametros()) {
				sb.append(nome);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		out.print(sb.toString());
		sb.setLength(0);
		out.println("};");
		out.println("            AnPackage pkg = invokeRemoteMethod(\""
				+ m.getNome() + "\", parameterTypes, parameters);");
		out.println("            if(pkg.getBody() instanceof ReplyException){");
		out
				.println("                Throwable exception = ((ReplyException) pkg.getBody()).getReturned();");
		for (String excecao : m.getExcessoes()) {
			out.println("                if(exception instanceof " + excecao
					+ ")");
			out.println("                    throw (" + excecao
					+ ") exception;");
		}
		out
				.println("                throw new AnRemoteException(exception.getMessage());");

		out.println("            }");
		String retornoUpper;
		if (retorno.equals("int")) {
			retornoUpper = "Integer";
		} else {
			retornoUpper = (retorno.charAt(0) + "").toUpperCase()
					+ retorno.substring(1);
		}
		if (valorPadrao != null) {
			out.println("            retorno = (" + retornoUpper
					+ ")((Reply)pkg.getBody()).getReturned();");
		}
	}

}
