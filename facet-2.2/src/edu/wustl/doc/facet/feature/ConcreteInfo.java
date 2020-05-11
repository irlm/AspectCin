/*
 * $Id: ConcreteInfo.java,v 1.4 2003/07/16 16:05:45 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

/**
 * Represents a node in the graph corresponding to a Concrete feature
 *
 * @author Frank Hunleth
 * @version $Revision: 1.4 $
 */
class ConcreteInfo extends DependenceInfo {
	
	private boolean dontTest_ = false;

	ConcreteInfo (Class featureClass)
	{
		super(featureClass);
	}

	ConcreteInfo (Class featureClass, boolean dontTest)
	{
		super(featureClass);
		dontTest_ = dontTest;
	}

	boolean isSelectable()
	{
		return true;
	}

	boolean shouldTest()
	{
		return !dontTest_;
	}

	String validate(SimpleDigraph sdg)
	{
		// Always valid.
		return null;
	}

	int getMinIndegree()
	{
		return 0;
	}

	int getMaxIndegree()
	{
		return -1;
	}

	String toDot (String vertexLabel)
	{
		return " " + vertexLabel + " [label=\"" + getBaseName() + "\"];";
	}
}
