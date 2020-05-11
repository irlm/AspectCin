/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.fragmentedmessage;

/**
 * This is the implementation of the custom valuetype ObjectId.
 *
 * @author Michael Macaluso
 */
public final class ObjectIdImpl
    extends ObjectId
    implements Comparable
{
    public static final int NULL_HASHCODE = -1;

    public ObjectIdImpl()
    {
    }

    public ObjectIdImpl( String id )
    {
        objectid = id;
    }

    public void marshal( org.omg.CORBA.DataOutputStream os )
    {
        boolean isNull = ( null == objectid );

        os.write_boolean( isNull );

        if ( !isNull )
        {
            os.write_wstring( objectid );
        }
    }

    public void unmarshal( org.omg.CORBA.DataInputStream is )
    {
        boolean isNull = is.read_boolean();

        if ( isNull )
        {
            objectid = null;
        }
        else
        {
            objectid = is.read_wstring();
        }
    }

    public boolean equals( Object anObject )
    {
        if ( null == anObject )
        {
            return false;
        }

        Class aClass = anObject.getClass();

        if ( aClass == ObjectIdImpl.class )
        {
            return equals( ( ObjectIdImpl ) anObject );
        }
        else if ( aClass == String.class )
        {
            return equals( ( String ) anObject );
        }

        return false;
    }

    public boolean equals( ObjectIdImpl anObject )
    {
        if ( null == anObject )
        {
            return false;
        }

        return equals( anObject.objectid );
    }

    public boolean equals( String anObject )
    {
        if ( null == anObject )
        {
            return ( objectid == null );
        }

        return anObject.equals( objectid );
    }

    public int hashCode()
    {
        if ( null == objectid )
        {
            return NULL_HASHCODE;
        }
        else
        {
            return objectid.hashCode();
        }
    }

    public int compare( Object anObject )
    {
        return compareTo( anObject );
    }

    public int compareTo( Object anObject )
    {
        if ( null == anObject )
        {
            return -1;
        }

        Class aClass = anObject.getClass();

        if ( aClass == ObjectIdImpl.class )
        {
            return compareTo( ( ObjectIdImpl ) anObject );
        }
        else if ( aClass == String.class )
        {
            return compareTo( ( String ) anObject );
        }

        return -1;
    }

    public int compareTo( ObjectIdImpl anObject )
    {
        if ( null == anObject )
        {
            return -1;
        }

        return compareTo( anObject.objectid );
    }

    public int compareTo( String anObject )
    {
        if ( null == anObject )
        {
            if ( null == objectid )
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            if ( null == objectid )
            {
                return 1;
            }
            else
            {
                return objectid.compareTo( anObject );
            }
        }
    }

    public String toString()
    {
        return objectid;
    }
}
