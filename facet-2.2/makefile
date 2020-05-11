#
# Makefile which basically just delegates to ant
#

VERSION=2.2
WORK_DIR=dist/facet-prebuilt-$(VERSION)

all:
	ant

prebuilt:
	rm -rf $(WORK_DIR) 
	mkdir $(WORK_DIR)
	mkdir $(WORK_DIR)/src
	mkdir $(WORK_DIR)/src/edu
	mkdir $(WORK_DIR)/src/edu/wustl	
	mkdir $(WORK_DIR)/src/edu/wustl/doc
	mkdir $(WORK_DIR)/jars 
	mkdir $(WORK_DIR)/examples
	cp -a src/ajworkingdir/edu/wustl/doc/facet $(WORK_DIR)/src/edu/wustl/doc
	cp -a src/edu/wustl/doc/utils $(WORK_DIR)/src/edu/wustl/doc
	cp examples/*.java $(WORK_DIR)/examples

