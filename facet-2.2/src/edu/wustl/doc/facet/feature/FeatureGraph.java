/*
 * $Id: FeatureGraph.java,v 1.2 2002/11/08 00:08:33 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <code>FeatureGraph</code> parses a dependency graph and outputs a
 * DOT file that can be used to generate a pretty picture of all of
 * the feature interdependencies.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @version 1.0
 */
public class FeatureGraph {
    FeatureRegistry aspectRegistry_;

    public FeatureGraph(FeatureRegistry ar) {
        aspectRegistry_ = ar;
    }

    public void generateDotOutput(PrintWriter pw) {
        pw.println("digraph G {");
        pw.println(" size=\"10,8\"");
        pw.println(" orientation=landscape");

        aspectRegistry_.generateDotContents(pw);

        pw.println("}");
    }

    public static void main(String[] args) {
        FeatureRegistry ar = new FeatureRegistry();

        if (args.length != 1) {
            System.out.println("FeatureGraph <output filename>");
            System.exit(1);
        }

        try {
            PrintWriter pw =
                new PrintWriter(new FileWriter(args[0]));

            FeatureGraph fg = new FeatureGraph(ar);
            fg.generateDotOutput(pw);

            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
