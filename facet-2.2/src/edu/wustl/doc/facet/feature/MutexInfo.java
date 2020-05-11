/*
 * $Id: MutexInfo.java,v 1.6 2003/07/16 16:05:45 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

/**
 * Represents a node in the graph corresponding to a Mutex feature
 *
 * @author Frank Hunleth
 * @version $Revision: 1.6 $
 */
class MutexInfo extends DependenceInfo {

	MutexInfo(Class featureClass) {
		super(featureClass);
	}

	boolean hasCode() {
		return false;
	}

	boolean isSelectable() {
		return true;
	}

	String validate(SimpleDigraph sdg)
	{
		int inDegree = countInDependEdges(sdg);

		if (inDegree > 1) {
			StringBuffer sb = new StringBuffer();
			sb.append("ERROR: Mutex ");
			sb.append(this.getBaseName());
			sb.append(" is acquired by more than one feature.\n");
			sb.append("  Deselect one or more of the following:\n");

			for (int edge = sdg.firstIn(this.vertex_);
			     edge != 0;
			     edge = sdg.nextIn(edge)) {
				if (((Integer) sdg.getEdgeData (edge)).intValue () == FeatureRegistry.Relation.DEPENDS) {
					sb.append("     ");
					DependenceInfo di =
						(DependenceInfo) sdg.getVertexData(sdg.tail(edge));
					sb.append(di.getBaseName());
					sb.append("\n");
				}
			}
			return sb.toString();
		}
		return null;
	}

	int getMinIndegree() {
		return 1;
	}

	int getMaxIndegree() {
		return 1;
	}

	String toDot(String vertexLabel) {
		return " " + vertexLabel +
			" [shape=box, label=\"" + getBaseName() + "\"];";
	}
}
