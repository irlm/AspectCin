/*
 * $Id: BaseTests.java,v 1.1 2002/09/28 19:58:28 ravip Exp $
 */

package edu.wustl.doc.facet;

import junit.framework.*;
import edu.wustl.doc.facet.feature.*;

/**
 * Simple tests that can be run on the base event channel code and any
 * combination of aspects that apply to it.
 *
 */
public class BaseTests extends TestCase {

	public BaseTests (String name)
	{
		super(name);
	}

	public void testAspectDependencies ()
	{
		FeatureRegistry ar = new FeatureRegistry ();

		/*
		 * Make sure that checkList works or there are probably a lot of other
		 * things that don't work.  And, I would imagine that some code just might
		 * not even compile.
		 */
		String errors = ar.validateGraph ();

		if (errors != null) {
			System.out.println (errors);
			assertNull (errors);
		}
	}
}
