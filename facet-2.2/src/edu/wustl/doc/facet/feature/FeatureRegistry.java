/*
 * $Id: FeatureRegistry.java,v 1.21 2003/07/16 16:05:45 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Common hook point for features to register their dependences and
 * mutexes on other features.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @version 1.0
 */
public class FeatureRegistry {

	/**
	 * Feature relation enumeration.
	 *   DEPENDS -> default relationship.  Implies that edge connecting
	 *     two features requires that the head end be present in the system
	 *     AND that the tail feature is counted in the "in" edge set of
	 *     the head feature.  This latter part means, for example, that
	 *     if the head feature is abstract, that the tail feature concretizes
	 *     it.
	 *   USES -> Implies that the edge connecting two features requires that
	 *     the head feature be present in the system if the tail feature is
	 *     present, BUT that the tail feature should not be counted in the
	 *     "in" edge set of the head feature.  This means that the tail
	 *     feature does not concretize the head feature if it happens to
	 *     be abstract.
	 */
	public static final class Relation
	{
		private Relation() {}
		public static final int DEPENDS = 1;
		public static final int USES = 2;
	}

	private SimpleDigraph dependenceGraph_;
	private int baseVertex_;

	/**
	 * Constructor for FeatureRegistry
	 */
	public FeatureRegistry ()
	{
		this.buildGraph();
		this.addBase();
	}

	public SimpleDigraph getDependenceGraph ()
	{
		return dependenceGraph_;
	}

	public int getBaseVertex ()
	{
		return baseVertex_;
	}

	protected int createVertex (DependenceInfo info)
	{
		int vertex =
			dependenceGraph_.findVertexByFinder (new AspectFinder (info.getFeatureClass ()));

		if (vertex == 0) {
			// Not found, so create a new feature node.
			vertex = dependenceGraph_.createVertex(info);
		} else {
			//            System.out.println("Updating node " + info.getName());
			// Found.  Most likely this was an inferred vertex,
			// so just replace it.  If not, we just waste a little
			// time.
			dependenceGraph_.setVertexData(vertex, info);
		}

		info.setVertex (vertex);

		return vertex;
	}

	static final class AspectFinder implements Finder
	{
		private final Class featureClass_;

		AspectFinder(Class featureClass)
		{
			featureClass_ = featureClass;
		}

		public boolean check (Object o)
		{
			DependenceInfo di = (DependenceInfo) o;
			return di.getFeatureClass ().equals (featureClass_);
		}
	}

	/**
	 * Register a dependency.
	 *
	 * @param feature      The feature.
	 * @param depends      The feature that this feature is dependent on
	 */
	private void registerDependence(Class feature, Class depends)
	{
		int us = dependenceGraph_.findVertexByFinder (new AspectFinder (feature));
		int them = dependenceGraph_.findVertexByFinder (new AspectFinder (depends));

		if (us == 0) {
			// Not yet registered, so create a normal node for us.
			us = dependenceGraph_.createVertex(new ConcreteInfo (feature));
		}

		if (them == 0) {
			// Dependency wasn't registered yet, so create an inferred
			// node.
			them = dependenceGraph_.createVertex(new InferredInfo (depends));
		}

		// Link the nodes together from us to them.
		dependenceGraph_.createEdge(us, them, new Integer (Relation.DEPENDS));
	}

	/**
	 * Mark the relationship between the "us" feature to the
	 * "target" feature as a Uses relationship rather than the
	 * default Depends relationship.
	 *
	 * @param us a <code>Class</code> value
	 * @param target a <code>Class</code> value
	 */
	public void markContainsRelationship(Class us, Class target)
	{
		int vertexUs = dependenceGraph_.findVertexByFinder (new AspectFinder (us));
		int vertexThem = dependenceGraph_.findVertexByFinder (new AspectFinder (target));

		int edge = dependenceGraph_.findEdge (vertexUs, vertexThem);

		if (edge != 0) {
			dependenceGraph_.setEdgeData (edge, new Integer (Relation.USES));
		} else {
			System.err.println("Couldn't find edge to mark relationship!");
		}
	}

	/**
	 * Introspect the interfaces that this feature extends and add
	 * them to the dependence graph.
	 *
	 * @param feature
	 */
	private void registerDependents (Class feature)
	{
		Class[] dependents = feature.getInterfaces ();
		for (int i = 0; i < dependents.length; i++) {
			registerDependence (feature, dependents [i]);
		}
	}

	/**
	 * Register an aspect.  This method should only be called from
	 * aspects that register "after" advice to buildGraph.
	 *
	 * @param aspectName   The Aspect
	 */
	public void registerFeature (Class feature)
	{
		registerFeature (feature, false);
	}

	/**
	 * Register an aspect.  This method should only be called from
	 * aspects that register "after" advice to buildGraph.
	 *
	 * @param aspectName   The Aspect
	 */
	public void registerFeature (Class feature, boolean dontTest)
	{
		this.createVertex (new ConcreteInfo(feature, dontTest));
		registerDependents (feature);
	}

	/**
	 * Register a mutex.  This method should only be called from
	 * aspects that register after advice to buildGraph.
	 *
	 * @param mutexName    The name of the mutex that should be checked.
	 */
	public void registerMutexFeature(Class mutexFeature)
	{
		this.createVertex (new MutexInfo (mutexFeature));
		registerDependents (mutexFeature);
	}

	/**
	 * Register an abstract aspect.  This method should only be called from
	 * aspects that register after advice to buildGraph.
	 *
	 * @param aspectName    The name of the mutex that should be checked.
	 */
	public void registerAbstractFeature(Class abstractFeature)
	{
		this.createVertex (new AbstractInfo (abstractFeature));
		registerDependents (abstractFeature);
	}

	/**
	 * Hooking point for all aspects that need to register a dependency.
	 */
	protected void buildGraph ()
	{
		/* Toss out any old dependence graph and start fresh. */
		dependenceGraph_ = new SimpleDigraph ();
		baseVertex_ = 0;

		/* After the after advice, everything will be in the graph. */
	}

	protected void buildEmptyGraph ()
	{
		dependenceGraph_ = new SimpleDigraph ();
		baseVertex_ = 0;
	}

	/**
	 * Add the "Base" node as a dependence of everything that
	 * doesn't have a dependence.
	 */
	protected void addBase()
	{
		baseVertex_ = this.createVertex (new BaseInfo());

		/*
		 * "less than" below is subtle - we don't add an edge from
		 * Base to Base.
		 */
		for (int i = 1; i < dependenceGraph_.n; i++)
			if (dependenceGraph_.outDegree (i) == 0)
				dependenceGraph_.createEdge (i,
							     baseVertex_,
							     new Integer (Relation.DEPENDS));
	}

	/**
	 * Check if the dependency graph is valid and return a list of
	 * errors if it isn't.
	 */
	public String validateGraph ()
	{
		StringBuffer sb = new StringBuffer();
		boolean failed = false;

		for (int i = 1; i <= dependenceGraph_.n; i++) {
			DependenceInfo di = (DependenceInfo) dependenceGraph_.getVertexData(i);
			String s = di.validate (dependenceGraph_);

			if (s != null) {
				sb.append (s);
				failed = true;
			}
		}

		if (failed)
			return sb.toString();
		else
			return null;
	}

	public void generateDotContents(PrintWriter pw)
	{
		// Vertices.
		for (int i = 1; i <= dependenceGraph_.n; i++) {
			DependenceInfo di = (DependenceInfo) dependenceGraph_.getVertexData (i);
			pw.println (di.toDot("v"+i));
		}

		// Edges.
		for (int i = 1; i <= dependenceGraph_.m; i++) {
			pw.println(" v" + dependenceGraph_.head(i) + " -> v" +
				   dependenceGraph_.tail(i) + " [dir=back" +
				   (((Integer) dependenceGraph_.getEdgeData(i)).intValue () == Relation.USES ?
				    ",style=dotted" : "") + " ]");
		}

	}

	public void dumpCombinations(List combin)
	{
		for (int i = 0; i < combin.size(); i++) {
			System.out.println("-------------------- " + i +
					   " --------------------");

			List subCombin = (List) combin.get(i);
			for (int j = 0; j < subCombin.size(); j++) {
				DependenceInfo di = (DependenceInfo) subCombin.get(j);
				System.out.println (di.getName());
			}
		}
	}

	public List generateAllCombinations ()
	{
		DependenceInfo base = (DependenceInfo) dependenceGraph_.getVertexData (baseVertex_);

		return base.generateCombinations (dependenceGraph_);
	}

	public boolean isValidCombination (boolean [] feature_vector)
	{
		int num_features = dependenceGraph_.n;
		FeatureRegistry testing_ground = new FeatureRegistry ();

		testing_ground.buildEmptyGraph ();

		for (int i = 1; i <= num_features; ++i) {
			if (feature_vector [i - 1] == false)
				continue;

			DependenceInfo vertex = (DependenceInfo) dependenceGraph_.getVertexData (i);
			Class feature_class = vertex.getFeatureClass ();

			if (vertex instanceof BaseInfo)
				continue;

			if (vertex instanceof ConcreteInfo)
				testing_ground.registerFeature (feature_class);
			else if (vertex instanceof AbstractInfo)
				testing_ground.registerAbstractFeature (feature_class);
			else if (vertex instanceof MutexInfo)
				testing_ground.registerMutexFeature (feature_class);
		}

		testing_ground.addBase ();

		String errors = testing_ground.validateGraph ();
		if (errors != null)
			return false;

		return true;
	}

	public void dumpCombination (PrintWriter pw, boolean [] feature_vector)
	{
		ArrayList combin = new ArrayList ();
		
		for (int i = 0; i < feature_vector.length; ++i) {
			if (!feature_vector [i])
				continue;
			
			DependenceInfo di = (DependenceInfo) dependenceGraph_.getVertexData (i + 1);
			combin.add (di);
		}
		
		pw.print("[");

		int len = combin.size ();
		
		for (int i = 0; i < len; ++i) {
			
			pw.print (((DependenceInfo) combin.get (i)).getVertex ());
			
			if (i != len - 1)
				pw.print(",");
		}

		pw.print ("]");
	}

	public void dumpAllCombinationsToFile (PrintWriter pw)
	{
		int num_features = dependenceGraph_.n;
		boolean [] feature_vector = new boolean [num_features];

		for (int i = 0; i < num_features; ++i)
			feature_vector [i] = false;

		//
		// Dump feature list
		//
		pw.print ("feature_list = [''");
		
		for (int v = 1; v < dependenceGraph_.n; v++) {
			DependenceInfo di = (DependenceInfo) dependenceGraph_.getVertexData (v);
			pw.print (",\n    '" + di.getBuildUseName () + "'");
		}
		
		pw.print ("]\n\n");
		pw.println ("combo_list = [");

		//
		// Run through the possible combinations and validate each one
		//
		boolean flag = false;
		int i = 0;
		
		do {
			if (isValidCombination (feature_vector)) {
				if (flag)
					pw.print (",\n");
				dumpCombination (pw, feature_vector);
				flag = true;
			}

		} while (getNextCombination (feature_vector));
			
		pw.println ("]");
	}

	public boolean getNextCombination (boolean [] feature_vector)
	{
		//
		// The feature vector basically indicates which features are turned
		// on and which ones are off.
		//
		// We cycle through all combinations by starting at 'all off' and then
		// doing binary addition with '1' everytime
		//
		int last = feature_vector.length;
		boolean carry = true;
		
		for (int i = last - 1; i >= 0; i--) {
			boolean old = feature_vector [i];
			feature_vector [i] = old || carry;

			if (old && carry) {
				feature_vector [i] = false;
				carry = true;
			} else
				break;
		}

		for (int i = last - 1; i >= 0; i--) {
			if (feature_vector [i])
				continue;
			else
				return true;
		}

		return false;
	}

	public void dumpCombinationsForPython (PrintWriter pw, List combin)
	{
		// Dump feature list

		pw.print("feature_list = [''");
		
		for (int v = 1; v < dependenceGraph_.n; v++) {
			DependenceInfo di = (DependenceInfo) dependenceGraph_.getVertexData (v);
			pw.print (",\n    '" + di.getBuildUseName () + "'");
		}
		
		pw.print ("]\n\n");

		// Dump combinations.

		pw.println("combo_list = [");

		for (int i = 0; i < combin.size(); i++) {
			List subCombin = (List) combin.get (i);

			if (i > 0)
				pw.println(",");

			pw.print("[");

			for (int j = 0; j < subCombin.size(); j++) {
				DependenceInfo di = (DependenceInfo) subCombin.get (j);

				if (j > 0)
					pw.print(",");

				pw.print (di.getVertex());
			}

			pw.print ("]");
		}
		
		pw.println ("]");
	}


	/*
	 * Main driver function to generate all valid combinations
	 */
	public static void main (String args[])
	{
		FeatureRegistry ar = new FeatureRegistry ();

		if (args.length != 1) {
			System.err.println ("Usage : java edu.wustl.doc.facet.feature.FeatureRegistry [filename]");
			return;
		}

		PrintWriter pw = null;
		
		try {
			pw = new PrintWriter (new FileWriter (args [0]));

		} catch (Exception e) { e.printStackTrace (); }

		ar.dumpAllCombinationsToFile (pw);

		//List combin = ar.generateAllCombinations ();
		//ar.dumpCombinationsForPython (pw, combin);
		
		pw.close ();
	}
}
