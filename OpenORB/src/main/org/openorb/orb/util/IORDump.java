/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import org.openorb.util.ORBUtils;

/**
 * Used to examine the contents of an IOR.
 *
 * @author Chris Wood
 */
public final class IORDump
{
    private static final String IORDUMP_TOOL = "IOR Dump Tool";

    // do not instantiate
    private IORDump()
    {
    }

    /**
     * Print the usage infotmation for this tool.
     */
    private static void printUsage()
    {
        System.out.println( "Usage: java org.openorb.orb.util.IORDump [Options]" );
        System.out.println( "Options:" );
        System.out.println( "--------" );
        System.out.println( "  -bind" );
        System.out.println( "              Giving it a chance to be redirected." );
        System.out.println( "  -f <filename>|<IOR>" );
        System.out.println( "              File containing IORs or explicit IOR." );
    }

    private static String readString( FileReader fr )
        throws IOException
    {
        int c;
        StringBuffer sb = new StringBuffer( 256 );
        while ( ( c = fr.read() ) != -1 )
        {
            if ( c == ( char ) 0x0D || c == ( char ) 0x0A )
            {
                return sb.toString();
            }
            else
            {
                sb.append( ( char ) c );
            }
        }
        return sb.toString();
    }

    /**
     * The main entry point for the IORDump tool.
     *
     * @param args The command line parameters for this tool.
     */
    public static void main( String [] args )
    {
        System.out.println( IORDUMP_TOOL + ", " + ORBUtils.COPYRIGHT );

        boolean bind = false;
        ArrayList aList = new ArrayList();
        for ( int i = 0; i < args.length; i++ )
        {
            if ( args[ i ].equals( "-bind" ) || args[ i ].equals( "--bind" ) )
            {
                bind = true;
            }
            else if ( args[ i ].equals( "-help" ) || args[ i ].equals( "--help" ) )
            {
                printUsage();
                System.exit( 0 );
            }
            else if ( args[ i ].startsWith( "-ORB" ) )
            {
                if ( args.length < i + 1 && !args[ i + 1 ].startsWith( "-" ) )
                {
                    i++;
                }
                else if ( args[ i ].startsWith( "-" ) )
                {
                    printUsage();
                    System.exit( 1 );
                }
            }
            else if ( args[ i ].equals( "-f" ) )
            {
                if ( i < args.length )
                {
                    i++;
                    try
                    {
                        FileReader fr = new FileReader( new File( args[ i ] ) );
                        while ( fr.ready() )
                        {
                            String ior = readString( fr );
                            if ( ior.startsWith( "IOR:" ) )
                            {
                                aList.add( ior );
                            }
                        }

                        fr.close();
                    }
                    catch ( Exception e )
                    {
                        System.err.println( "Exception: " + e.toString() );
                        System.exit( 2 );
                    }
                }
                else
                {
                    printUsage();
                    System.exit( 1 );
                }
            }
            else if ( args[ i ].startsWith( "IOR:" ) )
            {
                aList.add( args[ i ] );
            }
        }

        if ( aList.size() == 0 )
        {
            printUsage();
            System.exit( 1 );
        }

        try
        {
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, null );
            Iterator it = aList.iterator();
            while ( it.hasNext() )
            {
                String next = ( String ) it.next();
                org.omg.CORBA.Object obj;
                obj = orb.string_to_object( next );
                if ( bind )
                {
                    if ( obj._non_existent() )
                    {
                        System.err.println( "Target is non_existent." );
                    }
                }
                System.out.println( obj );
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            System.err.println( "SystemException: " + ex.toString() );
            System.exit( 2 );
        }
    }
}

