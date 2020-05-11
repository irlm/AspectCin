#ifndef __Supplier__
#define __Supplier__

#pragma interface

#include <java/lang/Object.h>

extern "Java"
{
  class Supplier;
  namespace edu
  {
    namespace wustl
    {
      namespace doc
      {
        namespace facet
        {
          class ProxyPushConsumerBase;
        }
      }
    }
  }
};

class ::Supplier : public ::java::lang::Object
{
public:
  Supplier (::edu::wustl::doc::facet::ProxyPushConsumerBase *);
  virtual void sendEvents ();
  virtual void disconnect_push_supplier ();
public: // actually package-private
  ::edu::wustl::doc::facet::ProxyPushConsumerBase *ppc_;
public:

  static ::java::lang::Class class$;
};

#endif /* __Supplier__ */
