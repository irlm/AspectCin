package anorb.comunication;

import anorb.AnRemoteException;


public interface PackageHandler {

	void onPackageArrived(AnPackage pkg) throws AnRemoteException;
	
}
