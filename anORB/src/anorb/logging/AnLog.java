package anorb.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AnLog {
	public static final Logger log, naming, comunication, compiler;

	public static final Handler out, err;

	static {
		Logger top = Logger.getLogger("");
		top.setLevel(Level.OFF);
		top.getHandlers()[0].setLevel(Level.OFF);
		log = Logger.getLogger("anOrb");
		naming = Logger.getLogger("anOrb.naming");
		compiler = Logger.getLogger("anOrb.compiler");
		comunication = Logger.getLogger("anOrb.comunication");
		out = new OutHandler(System.out, "[{0,time}]");
		out.setLevel(Level.FINEST);
		out.setFilter(new NotErrorFilter());
		err = new OutHandler(System.err, "[{0,date} at {0,time}]");
		err.setLevel(Level.WARNING);
		log.addHandler(err);
		log.addHandler(out);
		log.setLevel(Level.INFO);
		logToFile();
	}

	public static void logToFile() {
		FileHandler fileH;
		try {
			File dir = new File("log/");
			if (!dir.exists()) {
				dir.mkdir();
			}
			fileH = new FileHandler("log/anLog%g.log", 1024 * 1024, 10, true);
			fileH.setFormatter(new SimpleFormatter());
			log.addHandler(fileH);
		} catch (SecurityException e) {
			log.log(Level.SEVERE, "SEVERE ERROR anorb.logging.AnLog - 01", e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "SEVERE ERROR anorb.logging.AnLog - 02", e);
		}
	}

	public static void monitorCompilation() {
		compiler.setLevel(Level.FINEST);
	}

	public static void monitorComunication() {
		comunication.setLevel(Level.FINEST);
	}

	public static void monitorNaming() {
		naming.setLevel(Level.FINEST);
	}

	public static void monitorAll() {
		log.setLevel(Level.FINEST);
	}

	public static void monitorFine() {
		log.setLevel(Level.FINE);
	}
}
