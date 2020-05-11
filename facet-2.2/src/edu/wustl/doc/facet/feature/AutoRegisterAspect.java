/*
 * $Id: AutoRegisterAspect.java,v 1.1 2002/09/28 19:58:28 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

/**
 * This aspect is intended to be inherited by any aspect that
 * needs to be registered in the FeatureRegistry.
 */

public abstract aspect AutoRegisterAspect {

	abstract protected void register (FeatureRegistry fr);

	private pointcut registry (FeatureRegistry fr) :
		execution (void FeatureRegistry.buildGraph ())
		&& target (fr);

	after (FeatureRegistry fr) : registry (fr)
	{
		register (fr);
	}
}
