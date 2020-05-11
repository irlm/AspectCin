package projeto.anORB.namingservice;

import projeto.anORB.PackageBody;

public class Reply extends PackageBody {

	private static final long serialVersionUID = 859551136526895414L;
	
	private Object result;
	
	public Object getResult(){
		return result;
	}

	public void setResult(String string) {
		this.result = string;		
	}
}
