package edu.wustl.doc.facet.feature;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Common hook point for aspects to register their dependencies and
 * mutexes on other aspects.
 *
 */
public class FeatureDepBuilder {

	private SimpleDigraph dependenceGraph_;
	private int baseVertex_;

	public FeatureDepBuilder (FeatureRegistry ar)
	{
		dependenceGraph_ = ar.getDependenceGraph();
		baseVertex_ = ar.getBaseVertex();
	}

	private void traverseBuildDeps(int v, LinkedList sortedList, boolean[] done)
	{
		if (!done[v]) {
			
			done[v] = true;

			for (int e = dependenceGraph_.firstOut(v); e != 0; e = dependenceGraph_.nextOut(e))
				traverseBuildDeps(dependenceGraph_.head(e), sortedList, done);
	
			DependenceInfo di = (DependenceInfo) dependenceGraph_.getVertexData(v);

			if (di.isSelectable())
				sortedList.addFirst(di);
		}
	}

	/**
	 * Generate a topological sort of the build dependencies and
	 * return a list of DependenceInfos.
	 *
	 * @return the sorted <code>List</code>
	 */
	private List generateSortedDepList()
	{
		LinkedList sortedList = new LinkedList();

		//
		// Find the "roots" for the topological sort.  We look at the
		// graph upsidedown.  The "roots" are the leaves and the base
		// is the only leaf node in this view.
		//
		List roots = new ArrayList();
		for (int v = 1; v <= dependenceGraph_.n; v++) {
			if (dependenceGraph_.inDegree(v) == 0) {
				roots.add(new Integer(v));
			}
		}

		//
		// Figure out the topological ordering of the dependence list.
		//
		Iterator iter = roots.iterator();
		boolean[] done = new boolean[dependenceGraph_.n + 1];
		while (iter.hasNext()) {
			int v = ((Integer) iter.next()).intValue();
			traverseBuildDeps(v, sortedList, done);
		}
		return sortedList;
	}

	private void generatePatternSet(PrintWriter pw,
					String id,
					String baseDir,
					String fileSpec,
					String[] defaultIncludes)
	{

		pw.println ("<patternset id=\"" + id + "\">");

		for (int i = 0; i < defaultIncludes.length; i++)
			pw.println(defaultIncludes[i]);
		

		for (int v = 1; v <= dependenceGraph_.n; v++) {

			DependenceInfo di = (DependenceInfo) dependenceGraph_.getVertexData(v);

			if (di.isSelectable()) {
				pw.println("<include name=\""
					   + baseDir
					   + "/"
					   + di.getFeatureDir()
					   + "/"
					   + fileSpec
					   + "\" if=\""
					   + di.getBuildUseName()
					   + "\"/>");
			}
		}

		pw.println ("<exclude name=\"" + baseDir + "/feature*/*Test*\" if=\"no_unittests\"/>");
                pw.println ("<exclude name=\"" + baseDir + "/feature/Test*\" if=\"no_unittests\"/>");
                pw.println ("<exclude name=\"" + baseDir + "/*Test*\" if=\"no_unittests\"/>");
                pw.println ("<exclude name=\"" + baseDir +
                            "/feature_eventvector/EventVecHackAspect.java\" if=\"use_feature_disable_corba\"/>");
		pw.println ("</patternset>");
	}

	private void generateDepTarget (int v, PrintWriter pw)
	{
		DependenceInfo di = (DependenceInfo) dependenceGraph_.getVertexData(v);

		if (!di.isSelectable())
			return;

		pw.println ("<target name=\"Check." + di.getBuildUseName() + "\" if=\"" + di.getBuildUseName() + "\">");
			   
		for (int e = dependenceGraph_.firstOut(v); e != 0; e = dependenceGraph_.nextOut(e)) {

			DependenceInfo dep = (DependenceInfo) dependenceGraph_.getVertexData (dependenceGraph_.head(e));

			if (dep.isSelectable())
				pw.println ("<property name=\"" + dep.getBuildUseName() + "\" value=\"t\"/>");
			
		}
		
		pw.println ("<echo message=\"--> " + di.getBaseName() + " selected.\"/>");
		pw.println ("</target>");
	}

	private void generateAllDepTargets (PrintWriter pw)
	{
		for (int v = 1; v <= dependenceGraph_.n; v++)
			generateDepTarget(v, pw);
	}

	public void generateFeatureDeps (PrintWriter pw)
	{
		List sortedDepList = generateSortedDepList();

		pw.println ("<!-- Feature dependencies -->");
		pw.println ("<!--+");
		pw.println ("    | THIS IS AN AUTOMATICALLY GENERATED FILE.");
		pw.println ("    | USE \"ant featuredeps\" TO UPDATE.");
		pw.println ("    +-->\n");

		pw.println ("<!-- Include the FACET configuration description. -->");
		pw.println ("<property name=\"facet.properties.file\"");
		pw.println ("          value=\"${facetsrc}/facet.properties\"/>");
		pw.println ("<property file=\"${facet.properties.file}\"/>\n");

		pw.print ("<target name=\"feature.selector\" depends=\"");

		Iterator i = sortedDepList.iterator ();
		
		while (i.hasNext ()) {

			DependenceInfo di = (DependenceInfo) i.next();
			pw.print("Check." + di.getBuildUseName());

			if (i.hasNext())
				pw.print(",");
		}

		pw.println ("\">");

		String [] aspect_introduction_includes =
			new String [] {"<include name=\"${facetsrc}/feature/*.java\"/>",
				       "<include name=\"${facetsrc}/Event*/*.java\"/>"};

		generatePatternSet (pw,
				    "aspect.introduction.srcfiles",
				    "${facetsrc}",
				    "*IntroAspect.java",
				    aspect_introduction_includes);

                String [] feature_validator_includes =
                        new String [] {"<include name=\"${facetsrc}/feature/*.java\"/>"};
                                       
                generatePatternSet (pw,
                                    "feature.validator.srcfiles",
                                    "${facetsrc}",
                                    "*Feature.java",
                                    feature_validator_includes);
                
		String [] main_src_includes =
			new String[] {
			      "<include name=\"${facetsrc}/utils/*.java\" unless=\"no_unittests\"/>",
                              "<include name=\"${facetsrc}/utils/concurrent/*.java\"/>",
			      "<include name=\"${facetsrc}/*.java\"/>",
			      "<include name=\"${facetsrc}/feature/*.java\"/>",
			      //
			      // Note that the following seems rather hackish but it turns out that they have 
			      // to be connected to the specific features.
			      //
		              "<include name=\"src/generated/edu/wustl/doc/facet/Event*/**.java\" if=\"use_feature_enable_corba\" />",
		              "<include name=\"${facetsrc}/Event*/*.java\" if=\"use_feature_disable_corba\" />",
			      "<exclude name=\"${facetsrc}/feature_*/*IntroAspect.java\" if=\"use_feature_enable_corba\" />",
                              "<exclude name=\"${facetsrc}/utils/EventChannelCorbaTestCase.java\" if=\"use_feature_disable_corba\" />",
                              "<exclude name=\"${facetsrc}/utils/EventChannelTestCase.java\" if=\"use_feature_enable_corba\" />"};
		
		generatePatternSet (pw,
				    "facet.srcfiles",
				    "${facetsrc}",
				    "*.java",
				    main_src_includes);
		
		pw.println ("</target>");

		generateAllDepTargets (pw);
	}

	/**
	 * Main driver function to build feature dependence information.
	 */
	public static void main (String args[])
	{
		String filename = "featuredeps.xml";

		if (args.length == 1)
			filename = args[0];
		
		try {
			FeatureRegistry ar = new FeatureRegistry ();
			PrintWriter pw = new PrintWriter(new FileWriter(filename));
			FeatureDepBuilder fdb = new FeatureDepBuilder(ar);

			fdb.generateFeatureDeps (pw);
			pw.close ();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
