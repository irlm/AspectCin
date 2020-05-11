#LDLIBS = -lTAO_CosEvent -lTAO_CosNaming -lTAO_Svc_Utils -lTAO_IORTable -lTAO_PortableServer -lTAO
CPPFLAGS += -I$(TAO_ROOT)/orbsvcs

LDLIBS = -lTAO_CosEvent -lTAO_CosNaming -lTAO_Svc_Utils -lTAO_IORTable -lTAO_Messaging -lTAO_PortableServer -lTAO


SERVER_SRCS = Supplier.cpp 
SERVER_OBJS = $(SERVER_SRCS:.cpp=.o)
SERVER_BIN = server

CLIENT_SRCS =  Consumer.cpp
CLIENT_OBJS = $(CLIENT_SRCS:.cpp=.o)
CLIENT_BIN = client

#LDLIBS = -lTAO

TAO_IDLFLAGS=-in

BIN = $(SERVER_BIN) $(CLIENT_BIN)


include $(ACE_ROOT)/include/makeinclude/wrapper_macros.GNU
include $(ACE_ROOT)/include/makeinclude/macros.GNU
include $(TAO_ROOT)/rules.tao.GNU
include $(ACE_ROOT)/include/makeinclude/rules.common.GNU
include $(ACE_ROOT)/include/makeinclude/rules.nonested.GNU
include $(ACE_ROOT)/include/makeinclude/rules.local.GNU
include $(TAO_ROOT)/taoconfig.mk

$(SERVER_BIN): $(addprefix $(VDIR), $(SERVER_OBJS))
	$(LINK.cc) $(LDFLAGS) -o $@ $^ $(VLDLIBS) $(POSTLINK)

$(CLIENT_BIN): $(addprefix $(VDIR), $(CLIENT_OBJS))
	$(LINK.cc) $(LDFLAGS) -o $@ $^ $(VLDLIBS) $(POSTLINK)

