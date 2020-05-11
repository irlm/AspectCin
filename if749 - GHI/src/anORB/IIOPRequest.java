package anORB;


public abstract class IIOPRequest implements IIOPPackageBody {

	protected int id;
	protected String operation;	
	protected ServiceContext serviceContext;

	public IIOPRequest(int id, String operation){
		this.id = id;
		this.operation = operation;
	}	
	
	public byte type() {
		return 0;
	}

	public abstract byte[] toBytes();
}
