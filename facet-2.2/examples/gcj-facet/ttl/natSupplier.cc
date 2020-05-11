#include <stdio.h>

#include "testFacet.h"
#include "Supplier.h"

using namespace edu;
using namespace wustl;
using namespace doc;
using namespace facet;
using namespace EventComm;
using namespace EventChannelAdmin; 


void
Supplier::sendEvents ()
{
	printf ("Inside : Supplier::sendEvents () \n");
	for (int i = 0; i < 10; ++i) {
		Event *data  = new Event ();
		data->header = new EventHeader ();
		
		data->header->ttl = i;
		
		ppc_->push (data);
	}
}


void
Supplier::disconnect_push_supplier ()
{
	printf ("Supplier::disconnect_push_supplier () was called \n");

}
