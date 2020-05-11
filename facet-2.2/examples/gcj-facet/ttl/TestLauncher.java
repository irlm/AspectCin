/**
 * This class is an example of the Java wrapper that we should write
 * around the BOEING C++ test. This wrapper class simply defines a
 * static native method, which then will call the actual C++ 'main'
 * (which shall be suitably renamed)
 */
public class TestLauncher {
   
	/**
	 * See the native implementation on natTestLauncher.cc
	 */
	private static native void testLogic (String[] args);

	/**
	 * The only job of the main, is that of invoking the test logic.
	 */
	public static void main (String[] args)
	{
		TestLauncher.testLogic(args);
	}
}
