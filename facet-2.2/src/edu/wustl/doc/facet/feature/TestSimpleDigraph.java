/*
 * $Id: TestSimpleDigraph.java,v 1.4 2003/08/06 19:38:11 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

import junit.framework.*;
import edu.wustl.doc.facet.*;

/**
 * Unit tests for the SimpleDigraph class.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @version 1.0
 */
public class TestSimpleDigraph extends TestCase {
    protected SimpleDigraph dg;
    protected int[] v = new int[4];
    protected int[] e = new int[6];

    public TestSimpleDigraph(String name) {
        super(name);
    }

    protected void setUp() {
        dg = new SimpleDigraph();

        v[0] = dg.createVertex(new Integer(0));
        v[1] = dg.createVertex(new Integer(1));
        v[2] = dg.createVertex(new Integer(2));
        v[3] = dg.createVertex(new Integer(3));

        e[0] = dg.createEdge(v[3], v[2], new Integer(0));
        e[1] = dg.createEdge(v[1], v[1], new Integer(1));
        e[2] = dg.createEdge(v[1], v[2], new Integer(2));
        e[3] = dg.createEdge(v[1], v[3], new Integer(3));
        e[4] = dg.createEdge(v[1], v[0], new Integer(4));
        e[5] = dg.createEdge(v[2], v[3], new Integer(5));
    }

    public void testGetVertexData() {
        assertEquals(new Integer(0), dg.getVertexData(v[0]));
        assertEquals(new Integer(1), dg.getVertexData(v[1]));
        assertEquals(new Integer(2), dg.getVertexData(v[2]));
        assertEquals(new Integer(3), dg.getVertexData(v[3]));
    }

    public void testGetEdgeData() {
        assertEquals(new Integer(0), dg.getEdgeData(e[0]));
        assertEquals(new Integer(1), dg.getEdgeData(e[1]));
        assertEquals(new Integer(2), dg.getEdgeData(e[2]));
        assertEquals(new Integer(3), dg.getEdgeData(e[3]));
        assertEquals(new Integer(4), dg.getEdgeData(e[4]));
        assertEquals(new Integer(5), dg.getEdgeData(e[5]));
    }

    public void testFindVertexByData() {
        assertEquals(v[0], dg.findVertexByData(new Integer(0)));
        assertEquals(v[1], dg.findVertexByData(new Integer(1)));
        assertEquals(v[2], dg.findVertexByData(new Integer(2)));
        assertEquals(v[3], dg.findVertexByData(new Integer(3)));
    }

    class IntegerFinder implements Finder {
        private int value_;

        IntegerFinder(int value) {
            value_ = value;
        }

        public boolean check(Object o) {
            int x = ((Integer) o).intValue();
            return x == value_;
        }
    }

    public void testFindVertexByFinder() {
        assertEquals(v[0], dg.findVertexByFinder(new IntegerFinder(0)));
        assertEquals(v[1], dg.findVertexByFinder(new IntegerFinder(1)));
        assertEquals(v[2], dg.findVertexByFinder(new IntegerFinder(2)));
        assertEquals(v[3], dg.findVertexByFinder(new IntegerFinder(3)));
    }

    public void testBadFindVertexByData() {
        assertEquals(0, dg.findVertexByData(new Integer(200)));
    }

    public void testSetVertexData() {
        dg.setVertexData(v[0], new Integer(42));
        assertEquals(new Integer(42), dg.getVertexData(v[0]));
    }

    public void testInDegreeCalc() {
        assertEquals(1, dg.inDegree(v[0]));
        assertEquals(1, dg.inDegree(v[1]));
        assertEquals(2, dg.inDegree(v[2]));
        assertEquals(2, dg.inDegree(v[3]));
    }

    public void testOutDegreeCalc() {
        assertEquals(0, dg.outDegree(v[0]));
        assertEquals(4, dg.outDegree(v[1]));
        assertEquals(1, dg.outDegree(v[2]));
        assertEquals(1, dg.outDegree(v[3]));
    }

    public void testEdgeConnections() {
        assertEquals(v[2], dg.head(e[0]));
        assertEquals(v[3], dg.tail(e[0]));

        assertEquals(v[1], dg.head(e[1]));
        assertEquals(v[1], dg.tail(e[1]));

        assertEquals(v[2], dg.head(e[2]));
        assertEquals(v[1], dg.tail(e[2]));

        assertEquals(v[3], dg.head(e[3]));
        assertEquals(v[1], dg.tail(e[3]));

        assertEquals(v[0], dg.head(e[4]));
        assertEquals(v[1], dg.tail(e[4]));

        assertEquals(v[3], dg.head(e[5]));
        assertEquals(v[2], dg.tail(e[5]));
    }

    public void testVertexConnections() {
        assertEquals(0, dg.firstOut(v[0]));
        assertEquals(e[4], dg.firstIn(v[0]));

        assertEquals(e[1], dg.firstIn(v[1]));
        int[] edgeok = new int[6];
        edgeok[0] = 1;
        edgeok[1] = 0;
        edgeok[2] = 0;
        edgeok[3] = 0;
        edgeok[4] = 0;
        edgeok[5] = 1;
        for (int edge = dg.firstOut(v[1]);
             edge != 0;
             edge = dg.nextOut(edge)) {
            int i;
            for (i = 0; i < 6; i++) {
                if (edge == e[i]) {
                    edgeok[i]++;
                    assertEquals(1, edgeok[i]);
                    break;
                }
            }
            assertTrue(i != 6);
        }
        for (int i = 0; i < 6; i++) {
            assertEquals(1, edgeok[i]);
        }

        edgeok[0] = 0;
        edgeok[1] = 1;
        edgeok[2] = 0;
        edgeok[3] = 1;
        edgeok[4] = 1;
        edgeok[5] = 1;
        for (int edge = dg.firstIn(v[2]);
             edge != 0;
             edge = dg.nextIn(edge)) {
            int i;
            for (i = 0; i < 6; i++) {
                if (edge == e[i]) {
                    edgeok[i]++;
                    assertEquals(1, edgeok[i]);
                    break;
                }
            }
            assertTrue(i != 6);
        }
        for (int i = 0; i < 6; i++) {
            assertEquals(1, edgeok[i]);
        }
        assertEquals(e[5], dg.firstOut(v[2]));
        assertEquals(0, dg.nextOut(e[5]));

        edgeok[0] = 1;
        edgeok[1] = 1;
        edgeok[2] = 1;
        edgeok[3] = 0;
        edgeok[4] = 1;
        edgeok[5] = 0;
        for (int edge = dg.firstIn(v[3]);
             edge != 0;
             edge = dg.nextIn(edge)) {
            int i;
            for (i = 0; i < 6; i++) {
                if (edge == e[i]) {
                    edgeok[i]++;
                    assertEquals(1, edgeok[i]);
                    break;
                }
            }
            assertTrue(i != 6);
        }
        for (int i = 0; i < 6; i++) {
            assertEquals(1, edgeok[i]);
        }
        assertEquals(e[0], dg.firstOut(v[3]));
        assertEquals(0, dg.nextOut(e[0]));
    }

    public void testVertexInducedSubgraph() {
        int[] vertexList = new int[3];

        vertexList[0] = v[1];
        vertexList[1] = v[0];
        vertexList[2] = v[2];
        SimpleDigraph induced = dg.createVertexInducedSubgraph(vertexList);

        assertEquals(3, induced.n);
        assertEquals(3, induced.m);

        int[] newv = new int[3];

        newv[0] = induced.findVertexByFinder(new IntegerFinder(0));
        newv[1] = induced.findVertexByFinder(new IntegerFinder(1));
        newv[2] = induced.findVertexByFinder(new IntegerFinder(2));

        assertTrue(induced.hasEdge(newv[1], newv[0]));
        assertTrue(induced.hasEdge(newv[1], newv[1]));
        assertTrue(induced.hasEdge(newv[1], newv[2]));
    }

    static aspect AddTestSimpleDigraph {
        // NOTE: This aspect should extend TestSuiteAdder, but can't
        // since it breaks standalone feature graph generation.
        // Something needs to be moved.  This is a temporary hack.
        private pointcut addTestSuitesCut(TestSuite suite) :
            execution(void AllTests.addTestSuites(TestSuite)) && args(suite);

        before (TestSuite suite) : addTestSuitesCut(suite) {
            suite.addTestSuite(TestSimpleDigraph.class);
        }
    }
}
