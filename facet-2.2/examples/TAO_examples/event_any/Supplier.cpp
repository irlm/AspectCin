#include <orbsvcs/CosEventCommC.h>
#include <orbsvcs/CosEventChannelAdminC.h>
#include <orbsvcs/CosNamingC.h>
#include "orbsvcs/Naming/Naming_Utils.h"


int main ( int argc, char * argv[])
{
	try {
		CORBA::ORB_var orb = CORBA::ORB_init(argc, argv);

		// Get hold of the naming Service.
		TAO_Naming_Client naming_client;
		if (naming_client.init(orb.in()) != 0) {
			cerr << "Could not initialize name client , Supplier." <<endl;
			return 1;
	       	}
     
		// Obtaing the event channel from the naming service 
		CosNaming::Name name(1);
		name.length(1);
		name[0].id = CORBA::string_dup("CosEventService");
		CORBA::Object_var obj = naming_client->resolve(name);

		// Downcast to event channel reference
		CosEventChannelAdmin::EventChannel_var echoEC = CosEventChannelAdmin::EventChannel::_narrow(obj.in());
		if(CORBA::is_nil(echoEC.in())) {
	      		cerr << "Could not resolve EchoEventChannel. " <<endl;
       	       		return 1;
	      	}


		// Get hold of a supplier admin.
		CosEventChannelAdmin::SupplierAdmin_var supplierAdmin = echoEC->for_suppliers();

		// Get the reference/instance of a default consumer.
		CosEventChannelAdmin::ProxyPushConsumer_var consumer = supplierAdmin->obtain_push_consumer();
      
		// Connect our supplier to the consumer, with a NIL "name". We dont want the consumer to call
		// us back if it goes down. 
		consumer->connect_push_supplier(CosEventComm::PushSupplier::_nil());

		// Prepare a simple event, String "Hello World"
		CORBA::String_var eventData = CORBA::string_dup("Hello World");

		// Cast it to Corba Any type.
		CORBA::Any any;
		any <<= eventData;
		// There we go, push the event....
		consumer->push(any);

       	} catch(CORBA::Exception& ex) {

		cout << "Exception : "<< ex << endl;
		return 1;

       	} catch(...) {

		return 1;

	}
}
