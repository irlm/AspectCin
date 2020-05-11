#include <ace/streams.h>
#include <orbsvcs/CosEventCommC.h>
#include <orbsvcs/CosEventChannelAdminC.h>
#include <orbsvcs/CosNamingC.h>
#include "orbsvcs/Naming/Naming_Utils.h"
#include "Consumer.h"

EchoEventConsumer_i::EchoEventConsumer_i(CORBA::ORB_ptr orb)  : orb_(CORBA::ORB::_duplicate(orb))
{
};

void EchoEventConsumer_i::push( const CORBA::Any & data/*,CORBA::Environment &ACE_TRY_ENV*/)
     throw (CosEventComm::Disconnected, CORBA::SystemException)
{
	const char *eventData;
	if (data >>= eventData)	{
		cout << "EchoEventConsumer_i::push() : Received event: "
		     << eventData << endl;
	}
}

void EchoEventConsumer_i::disconnect_push_consumer(/*CORBA::Environment &ACE_TRY_ENV*/)
     throw (CORBA::SystemException)
{
	CORBA::Object_var obj = orb_->resolve_initial_references("POACurrent");
	PortableServer::Current_var current = PortableServer::Current::_narrow(obj);
	PortableServer::POA_var poa = current->get_POA();
	PortableServer::ObjectId_var objectId = current->get_object_id();
	poa->deactivate_object(objectId);
}


int main ( int argc, char * argv[])
{
	try
	{
		CORBA::ORB_var orb = CORBA::ORB_init(argc, argv);
		
		// Get the naming client.
		TAO_Naming_Client naming_client;
		if (naming_client.init(orb.in()) != 0) {
				cerr << "Couldnot initialize name client." <<endl;
				return 1;
	       	}
		
		// Get hold of the Event Channel using the naming Service.
		CosNaming::Name name(1);
		name.length(1);
		name[0].id = CORBA::string_dup("CosEventService");
		CORBA::Object_var obj = naming_client->resolve(name);
		
		// Downcast the reference to EventChannel.
		CosEventChannelAdmin::EventChannel_var echoEC = CosEventChannelAdmin::EventChannel::_narrow(obj.in());
		if(CORBA::is_nil(echoEC.in())) {
	       		cerr << "Could not resolve EchoEventChannel. " <<endl;
	       		return 1;
	       	}
		cout << "Found the EchoEventChannel " << endl;

		// Create a consumer
		EchoEventConsumer_i servant(orb.in ());

		//		cout << "The " << endl;

		// Register with the orb.
		obj = orb->resolve_initial_references("RootPOA");

		//                cout << "The 2" << endl;

		PortableServer::POA_var poa = PortableServer::POA::_narrow(obj.in ());

		//                cout << "The 3" << endl;

		PortableServer::ObjectId_var oid = poa->activate_object( &servant);

		//                cout << "The 4" << endl;

		CORBA::Object_var consumer_obj = poa->id_to_reference(oid.in ());

		//                cout << "The 5" << endl;

		CosEventComm::PushConsumer_var consumer = CosEventComm::PushConsumer::_narrow(consumer_obj.in ());

		//                cout << "The 6" << endl;

		// Get the consumer Admin object from the channel. 
		CosEventChannelAdmin::ConsumerAdmin_var consumerAdmin = echoEC->for_consumers();

		//                cout << "The 7" << endl;

		// From the admin object object obtain a default(empty) push supplier
		CosEventChannelAdmin::ProxyPushSupplier_var supplier = consumerAdmin->obtain_push_supplier();

		// Hook up our counsumer to get events.
		supplier->connect_push_consumer(consumer.in ());
		
		//Activate the POA.
		PortableServer::POAManager_var poa_manager = poa->the_POAManager();
		poa_manager -> activate();
      		cout << "Ready to receive events...." << endl;
		
		// Ready to rock and Roll.
		orb->run();


	} catch(CORBA::Exception& ex) 	{

		cerr << "Exception : " << ex <<endl;
		return 1;

       	} catch(...)  {

		cerr << " some thing here " << endl;
       		return 1;
       	}
}
