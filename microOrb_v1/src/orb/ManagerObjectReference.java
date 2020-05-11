package orb;

import ior.IOR;
import ior.ProfileBody;

import java.util.HashMap;
import java.util.Iterator;

import protocol.Version;
import address.Address;

public class ManagerObjectReference {

  private HashMap  objectsRefereces;

  public ManagerObjectReference() {
    this.objectsRefereces = new HashMap();
  }

  public Object getObjectReferenceServer (byte[] objectId) {
		Object objectReference = null;
		
		Iterator it = this.objectsRefereces.keySet().iterator();
		boolean isObject = true;
		while(it.hasNext()) {
			byte[] id2 = (byte[])it.next();
			for (int i = 0; i < id2.length; i++) {
				if (id2[i] == objectId[i]) {
					isObject = true;
				} else {
					isObject = false;
				}
			}
			if(isObject) {
				objectReference = this.objectsRefereces.get(id2);
				break;
			}
		}
		
		return objectReference;
	}
  
  	public <T> IOR createObjectsReference ( T classes) {
		byte major = 1;
		byte minor = 1;
		Version version = new Version(major, minor);
		String host = Address.hostname;
		int port = Address.port;		
		byte[] objectKey = {1,2,3};
		
		ProfileBody ProfileBody = new ProfileBody(host, objectKey, port, version);
		String type = classes.getClass().getName();
		IOR ior = new IOR(ProfileBody, type);
		try {
			objectsRefereces.put(ior.getProfileBody().getObjectKey(), classes.getClass().newInstance());

		} catch(Exception e){
			System.out.println("Deu erro ao criar nova instancia!" + classes.getClass().getName());
			e.printStackTrace();
		}
		
		return ior;
		
	}

}