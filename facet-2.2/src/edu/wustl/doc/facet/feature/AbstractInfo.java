/*
 * $Id: AbstractInfo.java,v 1.5 2003/07/16 16:05:45 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

/**
 * Represents a node in the graph corresponding to an Abstract feature
 *
 * @author Frank Hunleth
 * @version $Revision: 1.5 $
 */
class AbstractInfo extends DependenceInfo {

	AbstractInfo (Class featureClass)
	{
		super (featureClass);
	}

	boolean hasCode()
	{
		return false;
	}

	boolean isSelectable()
	{
		return true;
	}

	String validate (SimpleDigraph sdg)
	{
		int inDegree = countInDependEdges(sdg);

		if (inDegree < 1) {
			return "ERROR: Aspect " + getBaseName() +
				" is abstract and requires at least one dependent feature.\n";
		}

		return null;
	}

	int getMinIndegree()
	{
		return 1;
	}

	int getMaxIndegree()
	{
		return -1;
	}

	String toDot(String vertexLabel)
	{
		return " " + vertexLabel +
			" [shape=diamond, label=\"" + getBaseName() + "\"];";
	}
}
