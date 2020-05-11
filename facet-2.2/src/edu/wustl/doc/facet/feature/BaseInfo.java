/*
 * $Id: BaseInfo.java,v 1.2 2002/12/10 19:56:48 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

class BaseInfo extends ConcreteInfo {

	BaseInfo ()
	{
		super(null);
	}

	String getName ()
	{
		return "Base";
	}

	String getFeatureDir ()
	{
		return null;
	}

	String getBuildUseName ()
	{
		return "Base";
	}

	boolean isSelectable ()
	{
		return false;
	}
}
