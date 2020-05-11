/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.obv.custom;

public final class Client
{
    // do not instantiate
    private Client()
    {
    }

    public static void main( String[] args )
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, null );
        ValueExchange value = null;
        org.omg.CORBA.Object obj = null;
        try
        {
            java.io.FileInputStream file = new java.io.FileInputStream( "ObjectId" );
            java.io.InputStreamReader input = new java.io.InputStreamReader( file );
            java.io.BufferedReader reader = new java.io.BufferedReader( input );
            String ref = reader.readLine();
            obj = orb.string_to_object( ref );
        }
        catch ( java.io.IOException ex )
        {
            System.out.println( "File error" );
            System.exit( 0 );
        }

        value = ValueExchangeHelper.narrow( obj );
        try
        {
            ValueExampleImpl valueType = new ValueExampleImpl();
            valueType.name_state = "Example : hello";
            valueType.number_state = 100;
            value.sendValueExample( valueType );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            System.out.println( "A CORBA System exception has been intercepted" );
            System.out.println( "" );
        }
    }
}

