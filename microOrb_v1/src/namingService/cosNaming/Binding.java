package namingService.cosNaming;

import java.io.Serializable;

public class Binding implements Serializable {
	private NameComponent[] bindingName;
	private BindingType bindingType;
		
	public Binding() {}

	public Binding(NameComponent[] name, BindingType type) {
		bindingName = name;
		bindingType = type;
	}

	public NameComponent[] getBindingName() {
		return bindingName;
	}

	public void setBindingName(NameComponent[] bindingName) {
		this.bindingName = bindingName;
	}

	public BindingType getBindingType() {
		return bindingType;
	}

	public void setBindingType(BindingType bindingType) {
		this.bindingType = bindingType;
	}	
}
