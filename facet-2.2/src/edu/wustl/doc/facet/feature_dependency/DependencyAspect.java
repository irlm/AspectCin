package edu.wustl.doc.facet.feature_dependency;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

aspect DependencyAspect implements Upgradeable, DependencyFeature {

	pointcut upgradeLocations() :
		this(Upgradeable) &&
		!this(DependencyFeature);

	after() returning(ConsumerQOS qos) :
		call(ConsumerQOS.new()) && upgradeLocations ()
        {
		qos.dependencies = new Dependency[0];
	}

        public EventHeader Dependency.getHeader ()
        {
                return this.header;
        }

        public void Dependency.setHeader (EventHeader h)
        {
                this.header = h;
        }

        public int ConsumerQOS.getDependenciesLength ()
        {
                return this.dependencies.length;
        }

        public Dependency [] ConsumerQOS.getDependencies ()
        {
                return this.dependencies;
        }

        public void ConsumerQOS.setDependencies (Dependency [] d)
        {
                this.dependencies = d;
        }

        public Dependency ConsumerQOS.getDependency (int i)
        {
                return this.dependencies [i];
        }

        public void ConsumerQOS.setDependency (int i,
                                               Dependency d)
        {
                this.dependencies [i] = d;
        }
}
