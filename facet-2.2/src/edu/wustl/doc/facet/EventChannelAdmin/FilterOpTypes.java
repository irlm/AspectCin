package edu.wustl.doc.facet.EventChannelAdmin;

public interface FilterOpTypes {

        // Constants for Filter and Correlation operations
        //
        // MATCHOR is a shortcut operation to match the current header
        //         or the next one.  It is used mimic the behavior of
        //         the noncorrellation filters.  The choice of 0 is
        //         intentional as it is the Java default value for this
        //         the filter op field.
        //
        // Note: We don't use an enum here since it isn't legal to
        //       assign values to IDL enums.  We'd like to do this
        //       so that we keep type constant rather than changing
        //

        // Correlation filter constants
        
	final int CORRELATE_MATCHOR = 0;
	final int CORRELATE_MATCH   = 1;
	final int CORRELATE_AND     = 2;
	final int CORRELATE_OR      = 3;

        // Source and Type filtering constants

        final int TYPE_ANY          = -1;
        final int SOURCE_ANY        = -1;
}
		
