/* 
 * This is a big catch : make sure this is always included_first_ ! 
 */
#include <java/lang/Object.h>

#include "testFacet.h"
#include "Consumer.h"
#include "Supplier.h"

using namespace edu;
using namespace wustl;
using namespace doc;
using namespace facet;
using namespace EventComm;
using namespace EventChannelAdmin;

void
facet_test ()
{
	EventChannelImpl *ec = new EventChannelImpl ();
	
	SupplierAdmin *sa = ec->for_suppliers ();
	ConsumerAdmin *ca = ec->for_consumers ();

	ProxyPushSupplier *pps = ca->obtain_push_supplier ();
	EventChannelPushConsumer *cons = new Consumer ();

	/* 
	 * Note that this explicit cast is needed because the headers
	 * throw away this information about the inheritance heirarchy
	 */
	pps->connect_push_consumer ((PushConsumer *)cons);

	/*
	 * This is completely non-standard but for now, this is the only
	 * way because ProxyPushConsumer lacks the push methods !
	 */
	ProxyPushConsumerBase *ppc = 
                       (ProxyPushConsumerBase *) sa->obtain_push_consumer ();

	Supplier *supplier = new Supplier (ppc);

	supplier->sendEvents ();
}

