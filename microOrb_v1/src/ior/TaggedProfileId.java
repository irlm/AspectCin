package ior;

public class TaggedProfileId {
	private ProfileId tag;
	private ProfileBody profileBody;
	
	public TaggedProfileId(ProfileBody profileBody, ProfileId tag) {
		this.profileBody = profileBody;
		this.tag = tag;
	}

	public ProfileBody getProfileBody() {
		return this.profileBody;
	}

	public void setProfileBody(ProfileBody profileBody) {
		this.profileBody = profileBody;
	}

	public ProfileId getTag() {
		return this.tag;
	}

	public void setTag(ProfileId tag) {
		this.tag = tag;
	}
	
}
