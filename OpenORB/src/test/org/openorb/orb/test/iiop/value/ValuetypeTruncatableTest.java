/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

import org.omg.PortableServer.POA;

import org.openorb.orb.test.ORBTestCase;

import junit.framework.TestSuite;

/**
 * Tests marshaling and unmarshaling of truncatable valuetypes.
 *
 * @author Richard G Clark
 */
public class ValuetypeTruncatableTest extends ORBTestCase
{
    private org.omg.CORBA_2_3.ORB m_orb;
    private NodeEchoer m_cltRef;
    private org.omg.CORBA.Any m_any;

    public ValuetypeTruncatableTest( String name )
    {
        super( name );
    }

    protected void setUp()
    {
        super.setUp();

        try
        {
            // find the root poa
            org.omg.CORBA.ORB orb = getORB();
            m_orb = ( org.omg.CORBA_2_3.ORB ) orb;

            POA rootPOA = ( POA ) orb.resolve_initial_references( "RootPOA" );

            rootPOA.the_POAManager().activate();

            NodeEchoer svr_ref = ( new NodeEchoerImpl( rootPOA ) )._this( orb );

            m_cltRef = NodeEchoerHelper.narrow( forceMarshal( svr_ref ) );

            m_any = m_orb.create_any();
        }
        catch ( org.omg.CORBA.UserException ex )
        {
            fail( "exception during setup:" + ex.toString() );
        }
    }

    private Leaf leaf()
    {
        return new LeafImpl();
    }

    private PairNode pair( final Node lhs, final Node rhs )
    {
        final PairNode pair = new PairNodeImpl();
        pair.lhs = lhs;
        pair.rhs = rhs;
        return pair;
    }

    private LinkedPairNode linkedPair( final Node lhs, final Node rhs, final Node link )
    {
        final LinkedPairNode node = new LinkedPairNodeImpl();
        node.lhs = lhs;
        node.rhs = rhs;
        node.link = link;
        return node;
    }

    private SequenceNode seqNode( final Node[] children )
    {
        final SequenceNode node = new SequenceNodeImpl();
        node.children = children;
        return node;
    }

    public void testTrunc()
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testTrunc" );
        final Node n1 = leaf();
        final Node n2 = pair( leaf(), leaf() );
        final Node n3 = linkedPair( n1, n1, n1 );
        final Node n4 = linkedPair( n1, n2, pair( pair( n2, n1 ), n2 ) );
        final Node node = seqNode( new Node[] {n3} );
        m_cltRef.echoNode( n3 );
    }

    public static void main( final String[] args )
    {
        System.out.println( "Executing the " + ValuetypeTruncatableTest.class.getName() + "..." );
        junit.textui.TestRunner.run( new TestSuite( ValuetypeTruncatableTest.class ) );
    }

    private static final class NodeEchoerImpl extends NodeEchoerPOA
    {
        private final POA m_poa;

        NodeEchoerImpl( POA poa )
        {
            m_poa = poa;
        }

        public POA _default_POA()
        {
            return m_poa;
        }

        public Node echoNode( final Node node )
        {
            return node;
        }
    }
}

