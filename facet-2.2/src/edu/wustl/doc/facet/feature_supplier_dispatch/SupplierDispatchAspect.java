package edu.wustl.doc.facet.feature_supplier_dispatch;

import edu.wustl.doc.facet.Dispatcher;

aspect SupplierDispatchAspect {

	Dispatcher around() : call(Dispatcher.new()) {
		return new EventTypeDispatcher();
	}
}
