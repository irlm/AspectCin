package namingService.cosNaming;

import java.io.Serializable;

public class NameComponent implements Serializable {
	private String id;
	private String kind;
	
	public NameComponent() {}
	
	public NameComponent(String id, String kind) {
		this.id = id;
		this.kind = kind;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}
	
}
