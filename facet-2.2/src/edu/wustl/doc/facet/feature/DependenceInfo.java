/*
 * $Id: DependenceInfo.java,v 1.11 2003/07/16 16:05:45 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

abstract class DependenceInfo implements Comparable {
	
	private final Class featureClass_;
	protected int vertex_;

	DependenceInfo(Class featureClass)
	{
		featureClass_ = featureClass;
	}

	Class getFeatureClass()
	{
		return featureClass_;
	}

	String getName()
	{
		return featureClass_.getName();
	}

        String getBaseName ()
        {
                String name = featureClass_.getName ();
                return name.substring (name.lastIndexOf ('.') + 1);
        }

	int getVertex()
	{
		return vertex_;
	}

	void setVertex(int vertex)
	{
		vertex_ = vertex;
	}

	abstract String validate (SimpleDigraph sdg);
	abstract int getMinIndegree ();
	abstract int getMaxIndegree ();
	abstract String toDot (String vertexLabel);

	boolean isInferred()
	{
		return false;
	}

	/**
	 * Returns true if this feature is selectable at build time.
	 */
	abstract boolean isSelectable();

	String getFeatureDir()
	{
		String pkgName = featureClass_.getName();
		pkgName = pkgName.substring (0, pkgName.lastIndexOf ('.'));

		return pkgName.substring (pkgName.lastIndexOf ('.') + 1);
	}

	String getBuildUseName()
	{
		return "use_" + getFeatureDir();
	}

	/**
	 * by default, every feature should be tested.
	 */
	boolean shouldTest()
	{
		return true;
	}

	/**
	 * Implementation of the Comparable interface.
	 * ** Not a standard Comparable interface **
	 * The only use case for compareTo is to sort
	 * combination lists.  Using the vertex number
	 * is the simplest and fastest way of doing
	 * this.
	 *
	 * @param o another <code>DependenceInfo</code> instance
	 * @return standard compareTo return value;
	 */
	public int compareTo (Object o)
	{
		DependenceInfo di = (DependenceInfo) o;
		return this.vertex_ - di.vertex_;
	}

	/**
	 * Returns true if the feature adds code to the event channel.
	 *
	 * @return true if code is added.
	 */
	boolean hasCode()
	{
		return true;
	}

	/**
	 * Helper function to get the number of in depend edges.
	 */
	public int countInDependEdges (SimpleDigraph sdg)
	{
		int degree = 0;
		int edge;

		for (edge = sdg.firstIn(this.vertex_); edge != 0; edge = sdg.nextIn (edge))
			if ( ((Integer)sdg.getEdgeData(edge)).intValue () == FeatureRegistry.Relation.DEPENDS)
				degree++;

		return degree;
	}

	/**
	 * Dumps Ant build file compatible XML to set the appropriate
	 * dependences for this particular node in this specific
	 * configuration.  Note that this is really only useful when
	 * all dependence information has been compiled like when
	 * building a feature graph.
	 */
	void dumpBuildDepInfo(SimpleDigraph sdg)
	{
		// This code seems rather pointless to me in its current state
		
		//for (int e = sdg.firstOut(vertex_); e != 0; e = sdg.nextOut(e)) {
		//	int v = sdg.head(e);
		//
		//}
	}

	List generateCombinations(SimpleDigraph sdg)
	{
		System.out.println ("Total theoretical: " + Math.pow(2.0, sdg.n - 1));

		List result = generateDownwardCombinations (sdg);

		System.out.println ("Size before prune: " + result.size());
		// Prune bad, duplicate, or useless combinations.
		pruneCombinations (sdg, result);
		System.out.println ("Size after prune: " + result.size());

		return result;
	}

	void pruneCombinations(SimpleDigraph sdg, List combinList)
	{
		boolean[] depCheckList = new boolean [sdg.n + 1];
		boolean[] nullCheckList = new boolean [sdg.n + 1];
		int[] dupCheckList = new int [sdg.n + 1];
		// Set combinSet = new HashSet (combinList.size());
		boolean somethingToCheck = false;
		int comboNumber = 0;

		// Keep track of all vertices that have no code.
		for (int v = 1; v <= sdg.n; v++) {
			DependenceInfo di = (DependenceInfo) sdg.getVertexData(v);
			if (di.hasCode() == false) {
				nullCheckList[v] = true;
				somethingToCheck = true;
			}
			if (sdg.outDegree(v) > 1) {
				depCheckList[v] = true;
				somethingToCheck = true;
			}
		}

		// Return if nothing to do here.
		// NOTE: It is valid to skip the duplicate check since
		//       duplicate combinations are only created by
		//       nodes with multiple dependences.
		if (somethingToCheck == false)
			return;

		// Go through all combinations
		Iterator i = combinList.iterator ();
		nextCombo: while (i.hasNext()) {
			List combin = (List) i.next ();
			comboNumber++;

			// Check if we've already seen this combination.
			//Collections.sort(combin);
			//if (combinSet.add(combin) == false) {
			//	i.remove();
			//	continue;
			//}

			// Get vertices into an easier to use form.
			Iterator k = combin.iterator();
			boolean[] vertexList = new boolean[sdg.n + 1];
			while (k.hasNext()) {
				DependenceInfo di = (DependenceInfo) k.next();
				vertexList[di.getVertex()] = true;
			}

			// Go through vertices again and check for bad
			// and duplicate combinations.
			Iterator j = combin.iterator();
			while (j.hasNext()) {
				DependenceInfo di = (DependenceInfo) j.next();
				int v = di.getVertex();

				if (depCheckList[v]) {
					// Check that di has all of its dependences.
					for (int e = sdg.firstOut(v);
					     e != 0;
					     e = sdg.nextOut(e)) {
						if (vertexList[sdg.head(e)] == false) {
							// Not there, so remove this one.
							i.remove();
							continue nextCombo;
						}
					}
				}
				if (nullCheckList[v]) {
					// Check that something depends on this node.
					boolean foundSomething = false;
					for (int e = sdg.firstIn(v);
					     e != 0;
					     e = sdg.nextIn(e)) {
						if (vertexList[sdg.tail(e)] == true) {
							foundSomething = true;
							break;
						}
					}
					if (!foundSomething) {
						// Nothing there, so remove this one.
						i.remove();
						continue nextCombo;
					}
				}
				if (dupCheckList[v] == comboNumber) {
					// Found a duplicate node.
					i.remove();
					continue nextCombo;
				} else
					dupCheckList[v] = comboNumber;
			}
		}
	}


	/**
	 * Generate all possible combinations in a "downward" manner starting
	 * from the Base.  This approach is simple and obeys cardinality
	 * rules for nodes, but includes too many nodes in the end.
	 *
	 * To make the resulting list usable, the following needs to be
	 * pruned:
	 *
	 *   1.  Nodes with multiple "upward" dependences need to be
	 *       checked.  The return list can include nodes that have
	 *       multiple dependences, but not necessarily their
	 *       dependences.
	 *
	 *   2.  Remove duplicates.  Duplicates can be caused by
	 *       nodes with multiple upward dependences.
	 *
	 *   3.  Remove non different combinations.  These are combinations
	 *       that include things like mutexes and abstract features,
	 *       but have no features that actually depend on them.
	 *
	 * @param sdg the feature graph
	 * @return the combination list
	 */
	List generateDownwardCombinations (SimpleDigraph sdg)
	{
		List combinList = new LinkedList ();
		List usesList = new LinkedList ();

		int maxIndegree = getMaxIndegree();
		int[] regionOffset = new int[sdg.inDegree(this.vertex_) + 1];
		int regionNum = 0;
		int currentOffset = 0;

		// Add the empty combination to indicate nothing picked.
		combinList.add (new ArrayList());
		regionOffset [regionNum++] = 0;
		currentOffset++;

		for (int edge = sdg.firstIn (this.vertex_); edge != 0; edge = sdg.nextIn(edge)) {
			int tailVertex = sdg.tail(edge);
			DependenceInfo di = (DependenceInfo) sdg.getVertexData (tailVertex);

			// Check if we should even consider this vertex...
			if (!di.shouldTest())
				continue;

			// Check for "uses" edge to process this one later.
			if (((Integer) sdg.getEdgeData(edge)).intValue () == FeatureRegistry.Relation.USES) {
				usesList.add (di);
				continue;
			}

			List subCombin = di.generateDownwardCombinations (sdg);
			regionOffset[regionNum++] = currentOffset;

			int combinListToDo = combinList.size();
			if (maxIndegree != -1) {
				// Don't add anything that results in a
				// combination of more than maxIndegree
				// permits.
				if (maxIndegree < regionNum) {
					combinListToDo = Math.min (combinListToDo, regionOffset [maxIndegree]);
				}
			}

			currentOffset = mergeList (di, combinList, subCombin, combinListToDo, currentOffset);
		}

		// Prune off anything that results in a
		// combination of less than minIndegree
		// permits.
		int minIndegree = getMinIndegree ();
		if (minIndegree > 0) {
			for (int region = regionOffset.length - 1; region >= 0; --region) {
				int pruneTo;

				if (region == regionOffset.length - 1)
					pruneTo = currentOffset;
				else
					pruneTo = regionOffset[region + 1];

				pruneTo = Math.min (pruneTo, regionOffset[minIndegree]);
				pruneTo = Math.min (pruneTo, combinList.size());

				for (int i = pruneTo - 1; i >= regionOffset[region]; --i) {
					// Prune...
					combinList.remove(i);
				}
			}
		}

		// Now check for nodes connected by "uses"
		// relations to this node, and add all of their
		// combinations to the list of combinations from
		// just following "depends" relationship edges.
		Iterator usesNodes = usesList.iterator ();
		while (usesNodes.hasNext()) {
			
			DependenceInfo di = (DependenceInfo) usesNodes.next();
			List subCombin = di.generateDownwardCombinations(sdg);
			int combinListToDo = combinList.size();

			mergeList (di, combinList, subCombin, combinListToDo, currentOffset);
		}
		
		return combinList;
	}

	protected int mergeList (DependenceInfo di, List combinList, List subCombin,
				 int combinListToDo, int currentOffset)
	{
		for (int i = 0; i < combinListToDo; i++) {
			List sublist = (List) combinList.get(i);
			int sublistSize = sublist.size();
			
			for (int j = 0; j < subCombin.size(); j++) {
				List copySublist = new ArrayList (sublist);
				
				copySublist.add (di);
				concatList (copySublist, (List) subCombin.get(j));
				combinList.add (copySublist);
				currentOffset++;
			}
		}

		return currentOffset;
	}

	protected void concatList (List dest, List src)
	{
		Iterator i = src.iterator();
		while (i.hasNext())
			dest.add (i.next());
	}
}
