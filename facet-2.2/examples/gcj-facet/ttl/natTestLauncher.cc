#include "TestLauncher.h"
#include <stdio.h>
#include "testFacet.h"

/**
 * This is the wrapper method that just calls the C++ test.
 */
void
TestLauncher::testLogic (JArray< ::java::lang::String *> *) 
{
	/* 
	 * I am not passing anything right now...But here we should just
	 * convert the ARGs to the proper formant i.e. (int, char *[]).
	 */

	facet_test ();
}
