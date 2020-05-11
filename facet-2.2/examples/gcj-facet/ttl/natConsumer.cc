#include <java/lang/Object.h>
#include <stdio.h>

#include <edu/wustl/doc/facet/EventComm/Event.h>
#include <edu/wustl/doc/facet/EventComm/EventHeader.h>
#include "Consumer.h"

using namespace edu;
using namespace wustl;
using namespace doc;
using namespace facet;
using namespace EventComm;


void
Consumer::push(Event *data)
{
	printf ("Received data with TTL : %d \n", data->header->ttl);
}
