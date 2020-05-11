#
# WARNING: This is a quick and dirty Makefile, lot's of things can be automated, but
#          I kept it simple to make it easier to understand.
#          In its simplicity, it shows how you should go to make sure that Java and C++
#          don't fight, and don't give you weird linking problems.
#

LIBTOOL = libtool
JXX = gcj
GCJH = gcjh
JAVAX = gcj -C
JXXFLAGS= -g
LDLIBS = -lfacet -laspectjrt
JXXLIBDIR = ../../../lib
JXXINCLUDEDIR = ../../../include

COMPILE = $(LIBTOOL) --mode=compile  $(JXX) -prefer-non-pic $(JXXFLAGS) -I$(JXXINCLUDEDIR) -c
OBJS =  TestLauncher.o natTestLauncher.o  testFacet.o Supplier.o \
	natSupplier.o Consumer.o natConsumer.o	

MAKEDLIB = $(LIBTOOL) --mode=link $(JXX) $(JXXFLAGS) -L$(JXXLIBDIR) $(LDLIBS) \
	   --main=TestLauncher $(OBJS)  -o testLauncher

all:
	$(JAVAX) TestLauncher.java
	$(JAVAX) Consumer.java
	$(JAVAX) Supplier.java
	$(GCJH) TestLauncher 
	$(GCJH) Consumer Supplier
	$(COMPILE) TestLauncher.java -o ./TestLauncher.o
	$(COMPILE) Supplier.java -o Supplier.o
	$(COMPILE) Consumer.java -o Consumer.o
	$(COMPILE) natTestLauncher.cc -o ./natTestLauncher.o
	$(COMPILE) testFacet.cc -o ./testFacet.o
	$(COMPILE) natConsumer.cc -o natConsumer.o
	$(COMPILE) natSupplier.cc -o natSupplier.o
	$(MAKEDLIB)

clean:
	rm -f *.o *.lo *.class TestLauncher.h \
	Consumer.h Supplier.h testLauncher *~

