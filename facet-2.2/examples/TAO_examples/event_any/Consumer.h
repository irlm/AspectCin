#include <orbsvcs/CosEventCommS.h>
#include <orbsvcs/CosEventChannelAdminC.h>
#include <orbsvcs/CosEventCommC.h>
#include <orbsvcs/CosEventChannelAdminC.h>
#include <orbsvcs/CosNamingC.h>
#include "orbsvcs/Naming/Naming_Utils.h"

class EchoEventConsumer_i : public virtual POA_CosEventComm::PushConsumer
{
public:
  EchoEventConsumer_i(CORBA::ORB_ptr orb);

    void push (const CORBA::Any & data) throw (CosEventComm::Disconnected, CORBA::SystemException);
  
    void disconnect_push_consumer() throw (CORBA::SystemException); 

private:
  CORBA::ORB_var orb_;
};


