/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.poa.DefaultServant;

public final class Client
{
    // do not instantiate
    private Client()
    {
    }

    public static void main( String[] args )
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, null );
        IFileSystem fileSys = null;
        org.omg.CORBA.Object obj = null;
        try
        {
            java.io.FileInputStream file = new java.io.FileInputStream( "ObjectId" );
            java.io.BufferedReader myInput = new java.io.BufferedReader(
                  new java.io.InputStreamReader( file ) );
            String stringTarget = myInput.readLine();
            obj = orb.string_to_object( stringTarget );
        }
        catch ( java.io.IOException ex )
        {
            System.out.println( "Impossible d'importer la reference du serveur" );
            System.exit( 0 );
        }

        fileSys = IFileSystemHelper.narrow( obj );
        try
        {
            System.out.println( "Open file : Test.txt" );
            IFileDescriptor fd = fileSys.open( "Test.txt", 0 );
            if ( fd == null )
            {
                System.out.println( "Couldn't retrieve file descriptor (Test) from server!" );
                System.out.println( "Make sure that you start the example from the directory"
                      + " where the files Test.txt and OtherTest.txt are located." );
                System.exit( 1 );
            }
            String content = new String( fd.read( 100 ) );
            System.out.println( "File data : " );
            System.out.println( content );
            System.out.println( "" );
            System.out.println( "Open another file : OtherTest.txt" );
            IFileDescriptor fd2 = fileSys.open( "OtherTest.txt", 0 );
            if ( fd == null )
            {
                System.out.println( "Couldn't retrieve file descriptor (OtherTest) from server!" );
                System.out.println( "Make sure that you start the example from the directory"
                      + " where the files Test.txt and OtherTest.txt are located." );
                System.exit( 1 );
            }
            content = new String( fd2.read( 150 ) );
            System.out.println( "File data : " );
            System.out.println( content );
            System.out.println( "" );

        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            System.out.println( "A CORBA system exception has been intercepted" );
        }
    }
}

