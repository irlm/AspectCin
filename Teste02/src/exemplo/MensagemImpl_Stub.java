// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package exemplo;

public final class MensagemImpl_Stub
    extends java.rmi.server.RemoteStub
    implements exemplo.Mensagem, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    
    private static java.lang.reflect.Method $method_getMensagem_0;
    private static java.lang.reflect.Method $method_setMensagem_1;
    
    static {
	try {
	    $method_getMensagem_0 = exemplo.Mensagem.class.getMethod("getMensagem", new java.lang.Class[] {});
	    $method_setMensagem_1 = exemplo.Mensagem.class.getMethod("setMensagem", new java.lang.Class[] {java.lang.String.class});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
    }
    
    // constructors
    public MensagemImpl_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of getMensagem()
    public java.lang.String getMensagem()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getMensagem_0, null, 7480571838461314731L);
	    return ((java.lang.String) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of setMensagem(String)
    public void setMensagem(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_setMensagem_1, new java.lang.Object[] {$param_String_1}, -8546037537679349206L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}