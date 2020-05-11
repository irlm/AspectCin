/*
 * $Id: SimpleDigraph.java,v 1.2 2002/11/21 00:32:22 ravip Exp $
 */

package edu.wustl.doc.facet.feature;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of a data structure to hold directed graphs.
 * This version borrows heavily from the digraph implementation the
 * <a href="http://www.arl.wustl.edu/~jst/cs/541/">CS541</a> source
 * examples.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @version 1.0
 */
public class SimpleDigraph {

	/* Data structure to hold each vertex */
	class DigraphVertex {
		int firstInEdge = 0;
		int firstOutEdge = 0;

		Object vertexData;

		DigraphVertex(Object vData)
		{
			vertexData = vData;
		}
	}

	/* Data structure to hold each edge */
	class DigraphEdge {
		/* The head and tail vertices. (head <--- tail) */
		int head;
		int tail;

		Object edgeData;

		/* The next edge incident to head */
		int hnext;

		/* The next edge incident to tail */
		int tnext;
	}

	/**
	 * The number of vertices in the graph.
	 */
	public int n = 0;

	/**
	 * The number of edges in the graph.
	 */
	public int m = 0;

	private List vertices_;
	private List edges_;

	/**
	 * Creates a new <code>SimpleDigraph</code> instance and specify
	 * the initial size of the data structure.
	 *
	 * @param initVertices preallocate space for vertices
	 * @param initEdges preallocate space for edges
	 */
	public SimpleDigraph(int initVertices, int initEdges)
	{
		vertices_ = new ArrayList(initVertices);
		edges_ = new ArrayList(initEdges);

		// We skip over element 0.
		vertices_.add(null);
		edges_.add(null);
	}

	/**
	 * Creates a new <code>SimpleDigraph</code> instance using
	 * the default size for the vertex and edge lists.
	 */
	public SimpleDigraph()
	{
		this(16, 16);
	}

	/**
	 * Return the number of the first edge out of the specified vertex.
	 *
	 * @param vertex the number of the vertex
	 * @return an edge number or 0 if no such edges
	 */
	public int firstOut(int vertex)
	{
		DigraphVertex v = (DigraphVertex) vertices_.get(vertex);

		return v.firstOutEdge;
	}

	/**
	 * Return the number of the first edge into the specified vertex.
	 *
	 * @param vertex the number of the vertex
	 * @return an edge number or 0 if no such edges
	 */
	public int firstIn(int vertex)
	{
		DigraphVertex v = (DigraphVertex) vertices_.get(vertex);

		return v.firstInEdge;
	}

	/**
	 * Return the number of the next edge out of the tail of the
	 * specified edge.
	 *
	 * @param edge the number of the edge
	 * @return an edge number or 0 if no such edges
	 */
	public int nextOut(int edge)
	{
		DigraphEdge e = (DigraphEdge) edges_.get(edge);

		return e.tnext;
	}

	/**
	 * Return the number of the next edge into the head of the
	 * specified edge.
	 *
	 * @param edge the number of the edge
	 * @return an edge number or 0 if no such edges
	 */
	public int nextIn(int edge)
	{
		DigraphEdge e = (DigraphEdge) edges_.get(edge);

		return e.hnext;
	}

	/**
	 * Return the vertex at the head (arrow head part) of the specified edge.
	 *
	 * @param edge the number of the edge
	 * @return a vertex or 0 if no such edge
	 */
	public int head(int edge)
	{
		DigraphEdge e = (DigraphEdge) edges_.get(edge);

		return e.head;
	}

	/**
	 * Return the vertex at the tail of the specified edge.
	 *
	 * @param edge the number of the edge
	 * @return a vertex or 0 if no such edge
	 */
	public int tail(int edge)
	{
		DigraphEdge e = (DigraphEdge) edges_.get(edge);

		return e.tail;
	}

	/**
	 * Join vertices u and v together.
	 *
	 * @param u the "from" vertex
	 * @param v the "to" vertex
	 * @param edgeData optional data for the edge
	 * @return the new edge's number
	 */
	public int createEdge(int u, int v, Object edgeData)
	{
		DigraphVertex dgu = (DigraphVertex) vertices_.get(u);
		DigraphVertex dgv = (DigraphVertex) vertices_.get(v);
		DigraphEdge e = new DigraphEdge();

		this.m++;

		e.tail = u;
		e.head = v;
		e.edgeData = edgeData;

		e.tnext = dgu.firstOutEdge;
		dgu.firstOutEdge = this.m;
		e.hnext = dgv.firstInEdge;
		dgv.firstInEdge = this.m;

		edges_.add(this.m, e);

		return this.m;
	}

	/**
	 * Create a new vertex and store a reference to vertexData in it.
	 *
	 * @param vertexData optional data to store in the vertex
	 * @return the new vertex's number
	 */
	public int createVertex(Object vertexData)
	{
		this.n++;
		vertices_.add(this.n, new DigraphVertex(vertexData));
		return this.n;
	}

	/**
	 * Return previously stored vertex data.
	 *
	 * @param vertex the vertex number
	 * @return the vertex data or null if nothing was stored.
	 */
	public Object getVertexData(int vertex)
	{
		DigraphVertex v = (DigraphVertex) vertices_.get(vertex);

		return v.vertexData;
	}

	/**
	 * Update the data associated with a vertex.
	 *
	 * @param vertex the vertex number
	 * @param data the new vertex data
	 */
	public void setVertexData(int vertex, Object data)
	{
		DigraphVertex v = (DigraphVertex) vertices_.get(vertex);

		v.vertexData = data;
	}

	/**
	 * Return previously stored edge data.
	 *
	 * @param edge the edge number
	 * @return the edge data or null if nothing was stored.
	 */
	public Object getEdgeData(int edge)
	{
		DigraphEdge e = (DigraphEdge) edges_.get(edge);
		return e.edgeData;
	}

	/**
	 * Update the data associated with a edge.
	 *
	 * @param edge the vertex number
	 * @param data the new vertex data
	 */
	public void setEdgeData(int edge, Object data)
	{
		DigraphEdge e = (DigraphEdge) edges_.get(edge);
		e.edgeData = data;
	}

	/**
	 * Find a vertex in the graph by searching for its associated data.
	 * Note that this is a O(n) operation internally.
	 *
	 * @param data the vertex data to find
	 * @return the vertex that contains the data or 0 if not found
	 */
	public int findVertexByData(Object data)
	{
		for (int i = 1; i <= this.n; i++) {
			DigraphVertex v = (DigraphVertex) vertices_.get(i);
			if (v.vertexData.equals(data))
				return i;
		}

		return 0;
	}

	/**
	 * Find a vertex in the graph by searching for its associated data
	 * using a Finder instance.
	 * Note that this is a O(n) operation internally.
	 *
	 * @param Finder an instance of a class that implements the Finder
	 *               interface.
	 * @return the vertex that contains the data or 0 if not found
	 */
	public int findVertexByFinder(Finder finder)
	{
		for (int i = 1; i <= this.n; i++) {
			DigraphVertex v = (DigraphVertex) vertices_.get(i);
			if (finder.check(v.vertexData))
				return i;
		}

		return 0;
	}

	/**
	 * Tests if the specified edge exists
	 *
	 * @param from the edge's tail vertex
	 * @param to the edge's head vertex
	 * @return true if it exists
	 */
	public boolean hasEdge(int from, int to)
	{
		return findEdge(from, to) != 0;
	}

	/**
	 * Returns the first edge that connects the from vertex
	 * to the to vertex.
	 *
	 * @param from the edge's tail vertex
	 * @param to the edge's head vertex
	 * @return edge number or 0 if no edge found
	 */
	public int findEdge(int from, int to)
	{
		for (int edge = firstOut(from);
		     edge != 0;
		     edge = nextOut(edge))
			if (head(edge) == to)
				return edge;

		return 0;
	}

	/**
	 * Return the in degree of the specified vertex.
	 *
	 * @param vertex
	 * @return its indegree
	 */
	public int inDegree(int vertex)
	{
		int degree = 0;
		int edge;

		for (edge = firstIn(vertex);
		     edge != 0;
		     edge = nextIn(edge))
			degree++;

		return degree;
	}

	/**
	 * Return the out degree of the specified vertex.
	 *
	 * @param vertex
	 * @return its indegree
	 */
	public int outDegree(int vertex)
	{
		int degree = 0;
		int edge;

		for (edge = firstOut(vertex);
		     edge != 0;
		     edge = nextOut(edge))
			degree++;

		return degree;
	}

	/**
	 * Create a vertex induced subgraph of this graph.
	 *
	 * @param vertices an array of vertices
	 * @return the new <code>SimpleDigraph</code>
	 */
	public SimpleDigraph createVertexInducedSubgraph(int[] vertices)
	{
		int[] vertexMap = new int[this.n + 1];

		SimpleDigraph subgraph = new SimpleDigraph(vertices.length,
							   vertices.length);

		for (int i = 0; i < vertices.length; i++) {
			vertexMap[vertices[i]] = subgraph.createVertex(
								       getVertexData(vertices[i]));
		}

		for (int edge = 1; edge <= this.m; edge++) {
			int edgehead = vertexMap[this.head(edge)];
			int edgetail = vertexMap[this.tail(edge)];

			if (edgehead != 0 && edgetail != 0) {
				subgraph.createEdge(edgetail,
						    edgehead,
						    getEdgeData(edge));
			}
		}

		return subgraph;
	}
}
