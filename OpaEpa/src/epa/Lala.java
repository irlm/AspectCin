package epa;

public class Lala {

	public <T> T getClass(Class<T> clazz){
		
		T ret = null;
		
		try {
			ret = (T)Class.forName(clazz.getName()).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
}
