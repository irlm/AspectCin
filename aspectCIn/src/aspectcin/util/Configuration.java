package aspectcin.util;



public class Configuration {
	
	private static Configuration instance;

	private final ConstantesLoader constants;
	
	private Configuration() {
		constants = new ConstantesLoader("Configuration");
	}

	public static Configuration getInstance() {
		if (instance == null) {
			synchronized(Configuration.class){
				if(instance == null){
					instance = new Configuration();
				}
			}			
		}
		return instance;
	}
	
	public int port(){
		return new Integer(constants.get("port"));
	}
	
	public String connectionType(){
		return constants.get("connectionType");
	}

	
}
