/*
 * $Id: InferredInfo.java,v 1.5 2003/07/07 22:05:54 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

class InferredInfo extends DependenceInfo {

	InferredInfo (Class featureClass)
	{
		super (featureClass);
	}

	boolean isSelectable()
	{
		return false;
	}

	String validate(SimpleDigraph sdg)
	{
		return "ERROR : Feature " + this.getBaseName () + " not found. \n";
	}

	int getMinIndegree()
	{
		return 0;
	}

	int getMaxIndegree()
	{
		return 0;
	}

	boolean isInferred()
	{
		return true;
	}

	String toDot(String vertexLabel)
	{
		return " " + vertexLabel +
			" [shape=circle, label=\"" + getBaseName() + "\"];";
	}
}
