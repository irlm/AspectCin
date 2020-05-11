package ior;

import java.io.Serializable;

public class IOR implements Serializable{

	private String type_id;
	private ProfileBody profileBody;
	
	public IOR(ProfileBody profileBody, String type_id) {
		this.profileBody = profileBody;
		this.type_id = type_id;
	}

	public ProfileBody getProfileBody() {
		return this.profileBody;
	}

	public void setProfileBody(ProfileBody profileBody) {
		this.profileBody = profileBody;
	}

	public String getType_id() {
		return this.type_id;
	}

	public void setType_id(String type_id) {
		this.type_id = type_id;
	}
	
}