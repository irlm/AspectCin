package projeto.anORB;

import java.io.Serializable;

public abstract class PackageBody implements Serializable {

		private long stubId;

		public long getStubId() {
			return stubId;
		}

		public void setStubId(long stubId) {
			this.stubId = stubId;
		}
		
}
