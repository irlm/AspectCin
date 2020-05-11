package edu.wustl.doc.facet.feature;

/*
 * The feature validator
 *
 * We check to ensure that the feature combination currently in use is
 * a valid one
 *
 */

public class FeatureValidator {

        static public void main (String [] args)
        {
                FeatureRegistry ar = new FeatureRegistry ();
                
		String errors = ar.validateGraph ();
                
		if (errors != null) {
			System.err.println (errors);
                        System.exit (1);
                } else
                        System.exit (0);
        }
}
