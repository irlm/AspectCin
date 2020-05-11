/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test;

import java.util.Properties;

import junit.framework.TestCase;

import org.openorb.util.HexPrintStream;

/**
 * Skeleton orb test case to use when testing an orb component. The pre_init and
 * post init functions deal with creating and destroying orb instances for the
 * client and server ends.
 *
 * @author Chris Wood
 */
public abstract class ORBTestCase
    extends TestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public ORBTestCase( String name )
    {
        super( name );
    }

    /**
     * This method is called prior to calling run and basically starts
     * up a server and client orb, and spawns a thread for the server orb
     * to run with.
     */
    protected void setUp()
    {
        setUp( null );
    }

    /**
     * Override setUp and call this method with alternative properties to
     * startup an orb with alternative parameters.
     *
     * @param props The properties for this test case.
     */
    protected void setUp( Properties props )
    {
        if ( props == null )
        {
            m_props = new Properties();
        }
        else
        {
            m_props = props;
        }
        m_props.setProperty( "openorb.useStaticThreadGroup", "true" );
        String[] args = new String[ 0 ];
        m_serverORB = org.omg.CORBA.ORB.init( args, m_props );
        final java.util.Vector v = new java.util.Vector();
        m_serverThread = new Thread( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        v.add( new Object() );
                        m_serverORB.run();
                    }
                    catch ( java.lang.Exception ex )
                    {
                        System.out.println( "THIS IS REALLY STRANGE!!!" );
                        ex.printStackTrace();
                    }
                }
            } );

        m_serverThread.start();
        try
        {
            while ( v.size() == 0 )
            {
                Thread.sleep( 100 );
            }
        }
        catch ( java.lang.InterruptedException ex )
        {
             // shouldn't happen
        }
    }


    /**
     * This method is called after calling run. It shuts down the server and
     * client orbs.
     */
    protected void tearDown()
    {
        m_serverORB.shutdown( true );
        m_serverORB = null;

        try
        {
            m_serverThread.join( 20000 );
        }
        catch ( InterruptedException ex )
        {
            // catch to make javac happy
        }

        assertTrue( "ORBTestCase.postExecute: Unable to stop server orb",
              !m_serverThread.isAlive() );

        m_props = null;
    }

    /**
     * Restarts the server side orb.
     *
     * @return The new ORB instance.
     */
    protected org.omg.CORBA.ORB restartORB()
    {
        m_serverORB.shutdown( true );

        try
        {
            m_serverThread.join( 20000 );
        }
        catch ( InterruptedException ex )
        {
            // catch to make javac happy
        }

        assertTrue( "ORBTestCase.restartORB: Unable to stop server orb",
              !m_serverThread.isAlive() );

        String[] args = new String[ 0 ];

        m_serverORB = org.omg.CORBA.ORB.init( args, m_props );

        m_serverThread = new Thread( new Runnable()
                                    {
                                        public void run()
                                        {
                                            m_serverORB.run();
                                        }
                                    }
                                  );
        m_serverThread.start();

        return m_serverORB;
    }

    /**
     * Get the server side orb.
     *
     * @return The server's ORB instance.
     */
    public org.omg.CORBA.ORB getORB()
    {
        return m_serverORB;
    }

    /**
     * Sets local invoke policy on target.
     *
     * @param obj The object for which to set the forceMarshal policy.
     * @return The object with the forceMarshal policy activated.
     *
     * @throws org.omg.CORBA.PolicyError When an error occurs.
     */
    public org.omg.CORBA.Object forceMarshal( org.omg.CORBA.Object obj )
        throws org.omg.CORBA.PolicyError
    {
        if ( m_forcePolicy == null )
        {
            m_forcePolicy = m_serverORB.create_policy(
                  org.openorb.orb.policy.FORCE_MARSHAL_POLICY_ID.value, m_serverORB.create_any() );
        }
        return obj._set_policy_override( new org.omg.CORBA.Policy[] { m_forcePolicy },
              org.omg.CORBA.SetOverrideType.ADD_OVERRIDE );
    }

    /**
     * Write buffer as hex to given stream.
     *
     * @param stream The stream to write the buffer to.
     * @param buf The buffer to write to the stream
     *
     * @throws java.io.IOException When an error occurs.
     */
    public static void writeVerboseHex( java.io.OutputStream stream, byte[] buf )
        throws java.io.IOException
    {
        HexPrintStream hps = new HexPrintStream( stream, HexPrintStream.FORMAT_MIXED );

        hps.write( buf );
        hps.flush();
    }

    /**
     * Show the message upon entering the test.
     *
     * @param test The name of the test case.
     * @return The start time.
     */
    public long enteringTest( String test )
    {
        m_startTime = System.currentTimeMillis();
        System.out.println( ">Entering test '" + test + "'" );
        return m_startTime;
    }

    /**
     * Show the message upon test exit.
     *
     * @param test The name of the test case.
     * @return The execution time in msec.
     */
    public long exitingTest( String test )
    {
        final long duration = System.currentTimeMillis() - m_startTime;
        System.out.println( "<Exiting  test '" + test + "' (" + duration + "ms)" );
        return duration;
    }

    private long m_startTime;
    private org.omg.CORBA.Policy m_forcePolicy;
    private org.omg.CORBA.ORB m_serverORB;
    private Thread m_serverThread;
    private Properties m_props;
}

