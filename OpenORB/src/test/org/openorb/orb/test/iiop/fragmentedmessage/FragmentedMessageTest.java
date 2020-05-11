/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.fragmentedmessage;

import junit.framework.TestSuite;

import org.omg.PortableServer.POA;
import org.openorb.orb.test.ORBTestCase;

import org.omg.CORBA.ORB;

import java.util.Properties;

/**
 * Tests marshaling and unmarshaling of various iiop types.
 *
 * @author Michael Macaluso
 */
public class FragmentedMessageTest
    extends ORBTestCase
{
    private AttributeManager m_serverReference;
    private AttributeManager m_clientReference;

    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public FragmentedMessageTest( String name )
    {
        super( name );
    }

    /**
     * Return the activated server reference.
     *
     * @return The server reference.
     */
    public AttributeManager getReference()
    {
       return m_serverReference;
    }

    /**
     * Set up the test case.
     */
    protected void setUp()
    {
        Properties aProperties = new Properties();
        aProperties.put( "org.omg.CORBA.ORBClass", "org.openorb.orb.core.ORB" );
        aProperties.put( "org.omg.CORBA.ORBSingletonClass", "org.openorb.orb.core.ORBSingleton" );

        super.setUp( aProperties );

        try
        {
            ORB anORB = getORB();
            POA rootPOA = ( POA ) anORB.resolve_initial_references( "RootPOA" );
            m_serverReference = ( new AttributeManagerImpl( rootPOA ) )._this( anORB );
            rootPOA.the_POAManager().activate();
            m_clientReference = AttributeManagerHelper.narrow( forceMarshal( m_serverReference ) );
        }
        catch ( org.omg.CORBA.UserException ex )
        {
            fail( "exception during setup:" + ex.toString() );
        }
    }

    /**
     * Simple struct echo.
     */
    public void testGetAttributeDefinitions()
    {
        System.out.println( "Test: " + this.getClass().getName()
              + ".testGetAttributeDefinitions" );
        AttributeDefinition[] anAttributeDefinitionArray =
              m_clientReference.getAttributeDefinitions();
        assertEquals( "The number of attribute definitions does not match!",
              anAttributeDefinitionArray.length,
              m_clientReference.getNumberOfAttributeDefintions() );
    }

    /**
     * The entry point of the test case.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        if ( args.length > 0 && args[ 0 ].equals( "server" ) )
        {
            FragmentedMessageTest test = new FragmentedMessageTest( "standalone" );
            test.setUp();
            String reference = test.getORB().object_to_string( test.getReference() );
            try
            {
                java.io.FileOutputStream file = new java.io.FileOutputStream( "ObjectId" );
                java.io.PrintStream pfile = new java.io.PrintStream( file );
                pfile.println( reference );
                file.close();
            }
            catch ( java.io.IOException ex )
            {
                System.out.println( "File error" );
            }
            test.getORB().run();
        }
        else if ( args.length > 0 && args[ 0 ].equals( "client" ) )
        {
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, null );
            org.omg.CORBA.Object obj = null;
            try
            {
                java.io.FileInputStream file = new java.io.FileInputStream( "ObjectId" );
                java.io.InputStreamReader myInput = new java.io.InputStreamReader( file );
                java.io.BufferedReader reader = new java.io.BufferedReader( myInput );
                String ref = reader.readLine();
                obj = orb.string_to_object( ref );
            }
            catch ( java.io.IOException ex )
            {
                ex.printStackTrace();
            }

            try
            {
               AttributeManager mgr = AttributeManagerHelper.narrow( obj );
               System.out.println( "Test: AttributeManager.getAttributeDefinitions" );
               AttributeDefinition[] anAttributeDefinitionArray =
                     mgr.getAttributeDefinitions();
            }
            catch ( Exception ex )
            {
                System.out.println( "An unknown exception occured!" );
                ex.printStackTrace();
            }
        }
        else
        {
            System.out.println( "Executing the " + FragmentedMessageTest.class.getName() + "..." );
            junit.textui.TestRunner.run( new TestSuite( FragmentedMessageTest.class ) );
        }
    }
}

