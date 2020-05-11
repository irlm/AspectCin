package aspectcin.util;

import java.util.*;

public class ConstantesLoader {

   public static final String DEFAULT_PACKAGE = "aspectcin.util";
   private ResourceBundle resource;
   private String file;

   public ConstantesLoader(String _package, String file) {
	   try {
		   this.file = _package + "." + file;
		   resource = ResourceBundle.getBundle(this.file);
	   }
	   catch (MissingResourceException ex) {
		   throw ex;
	   }
   }


   public ConstantesLoader(String arquivo) {
	   this(DEFAULT_PACKAGE, arquivo);
   }


   public String get(String key){
	   String retorno = null;
	   try {
		   retorno = resource.getString(key);
	   } catch (MissingResourceException ex) {
		   throw ex;
	   }
	   return retorno;
   }

}

